package net.nana4.contactlenstimer.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import net.nana4.contactlenstimer.views.notifications.AlarmBroadcastReceiver;
import net.nana4.contactlenstimer.R;

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

    /**
     * 通知を登録・削除します。
     *
     * @param context
     */
    public static void updateTimer(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // 前回登録した通知を削除
        cancelAlarm(context, REQUEST_CODE_RIGHT_EYE);
        cancelAlarm(context, REQUEST_CODE_LEFT_EYE);

        if (!prefs.getBoolean("notification", false)) {
            // 通知設定がオフ
            return;
        }

        DateFormat saveTimeFormat = TimePreference.formatter();

        String notificationTime = prefs.getString("notification_time", null);

        Calendar timeCalendar = Calendar.getInstance();

        // HH:MM
        try {
            timeCalendar.setTime(saveTimeFormat.parse(notificationTime));
        } catch (ParseException e) {
            // TODO: メッセージ
            Log.e(TAG, "invalid date format", e);

            return;
        }

        StringBuilder toastText = new StringBuilder();

        // 通知を登録
        setAlarm(context, prefs.getString("right_use_start_date", null), REQUEST_CODE_RIGHT_EYE, timeCalendar);
        setAlarm(context, prefs.getString("left_use_start_date", null), REQUEST_CODE_LEFT_EYE, timeCalendar);
    }

    private static String getRegisterText(Context context, Date date) {
        StringBuilder toastText = new StringBuilder();

        toastText.append(android.text.format.DateFormat.getLongDateFormat(context).format(date)).append(' ');
        toastText.append(android.text.format.DateFormat.getTimeFormat(context).format(date));

        return toastText.toString();
    }

    private static void cancelAlarm(Context context, int requestCode) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(pendingIntent);
    }

    private static void setAlarm(Context context, String useStartDate, int requestCode, Calendar timeCalendar) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (useStartDate == null) {
            // 使用開始日が未登録
            return;
        }

        DateFormat saveDateFormat = new SimpleDateFormat(context.getString(R.string.save_date_format));
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(saveDateFormat.parse(useStartDate));
        } catch (ParseException e) {
            // TODO: メッセージ
            Log.e(TAG, "invalid date format", e);

            return;
        }

        // 通知時間を追加
        calendar.add(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.add(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        // 交換日数を追加
        addUseDate(context, calendar);

        if (calendar.compareTo(Calendar.getInstance()) < 1) {
            // 既に交換日を過ぎている場合
            // TODO:
            // return;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra("message", calendar.getTime().toString());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        // TODO:
        Toast.makeText(context, "設定しました", Toast.LENGTH_SHORT).show();
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
