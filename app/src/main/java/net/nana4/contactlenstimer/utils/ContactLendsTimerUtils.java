package net.nana4.contactlenstimer.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import net.nana4.contactlenstimer.R;
import net.nana4.contactlenstimer.views.notifications.AlarmBroadcastReceiver;

import org.bostonandroid.timepreference.TimePreference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ContactLendsTimerUtils {
    private static final String TAG = ContactLendsTimerUtils.class.getSimpleName();

    private static final int REQUEST_CODE_RIGHT_EYE = 0;
    private static final int REQUEST_CODE_LEFT_EYE = 1;

    private static final int MAX_TIMER_COUNT = 2;

    public static String getUseStartDateKey(View view) {
        switch (view.getId()) {
            case R.id.textViewRightUseStartDate:
                return "right_use_start_date";
            case R.id.textViewLeftUseStartDate:
                return "left_use_start_date";
            default:
                throw new IllegalArgumentException(view.toString());
        }
    }

    private static DateFormat getSaveDateFormatter(Context context) {
        return new SimpleDateFormat("yyyy.MM.dd");
    }

    /**
     * Preferenceに保存する日付文字列を取得します。
     *
     * @param context
     * @param date
     * @return
     */
    public static String formatSaveDate(Context context, Date date) {
        return getSaveDateFormatter(context).format(date);
    }

    /**
     * Preferenceに保存した日付文字列を解析します。
     *
     * @param context
     * @param date
     * @return
     */
    public static Date parseSaveDate(Context context, String date) {
        try {
            return getSaveDateFormatter(context).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 通知を登録・削除します。
     *
     * @param context
     */
    public static void updateTimer(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // 前回登録した通知を削除
        for (int requestCode = 0; requestCode < MAX_TIMER_COUNT; requestCode++) {
            cancelAlarm(context, requestCode);
        }

        if (!prefs.getBoolean("notification", false)) {
            // 通知設定がオフ
            return;
        }

        Calendar timeCalendar = Calendar.getInstance();
        DateFormat saveTimeFormat = TimePreference.formatter();
        String notificationTime = prefs.getString("notification_time", null);

        // HH:MM
        try {
            timeCalendar.setTime(saveTimeFormat.parse(notificationTime));
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }

        boolean lensSeparately = prefs.getBoolean("lends_separately", false);

        // 通知を登録
        boolean result;
        if (lensSeparately) {
            setAlarm(context, context.getString(R.string.right_eye), "right_use_start_date", 0, timeCalendar);
            setAlarm(context, context.getString(R.string.left_eye), "left_use_start_date", 1, timeCalendar);
        } else {
            setAlarm(context, context.getString(R.string.both_eyes), "right_use_start_date", 0, timeCalendar);
        }
    }

    private static void cancelAlarm(Context context, int requestCode) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(pendingIntent);
    }

    private static boolean setAlarm(Context context, String eye, String prefKey, int requestCode, Calendar timeCalendar) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String useStartDate = prefs.getString(prefKey, null);

        if (useStartDate == null) {
            // 使用開始日が未登録
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseSaveDate(context, useStartDate));

        // 通知時間を追加
        calendar.add(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.add(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        // 交換日数を追加
        addUseDate(context, calendar);

        if (calendar.compareTo(Calendar.getInstance()) < 1) {
            // 既に交換日を過ぎている場合
            Toast.makeText(context, String.format(context.getString(R.string.passed_message), eye), Toast.LENGTH_SHORT).show();

            return false;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra("message", String.format(context.getString(R.string.exchange_message), eye));
        intent.putExtra("prefKey", prefKey);
        intent.putExtra("requestCode", requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        return true;
    }

    /**
     * コンタクトレンズの利用日数を加算します
     *
     * @param context
     * @param baseCalendar
     */
    public static void addUseDate(Context context, Calendar baseCalendar) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String lendsType = prefs.getString("lends_type", "2w");

        switch (lendsType) {
            case "2w":
                baseCalendar.add(Calendar.DAY_OF_MONTH, 14);
                break;
            case "1m":
                baseCalendar.add(Calendar.MONTH, 1);
        }
    }

}
