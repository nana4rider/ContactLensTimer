package net.nana4.contactlenstimer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DateFormat saveDateFormat;
    private DateFormat viewDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 日付フォーマット
        saveDateFormat = new SimpleDateFormat(getString(R.string.save_date_format));
        viewDateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        boolean lensSeparately = prefs.getBoolean("lends_separately", false);

        try {
            // 右目の保存設定を表示
            Calendar rightEyeCalendar = Calendar.getInstance();
            String rightEyeDate = prefs.getString("right_use_start_date", null);
            if (rightEyeDate != null) {
                rightEyeCalendar.setTime(saveDateFormat.parse(rightEyeDate));
                setTextUseDate(R.id.textViewRightUseStartDate, rightEyeCalendar);
            }

            // 左目の保存設定を表示
            if (lensSeparately) {
                Calendar leftEyeCalendar = Calendar.getInstance();
                String leftEyeDate = prefs.getString("left_use_start_date", null);
                if (leftEyeDate != null) {
                    leftEyeCalendar.setTime(saveDateFormat.parse(leftEyeDate));
                    setTextUseDate(R.id.textViewLeftUseStartDate, leftEyeCalendar);
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "保存されている日付が不正", e);
        }

        TextView textViewRightEye = (TextView) findViewById(R.id.textViewRightEye);
        RelativeLayout layoutLeftEye = (RelativeLayout) findViewById(R.id.layoutLeftEye);

        // 左右別々の状態に応じて画面構成を変更
        if (lensSeparately) {
            // 右目の表示を 右目 に切り替え
            textViewRightEye.setText(R.string.right_eye);
            // 左目のレイアウトを表示
            layoutLeftEye.setVisibility(View.VISIBLE);
        } else {
            // 右目の表示を 右目/左目 に切り替え
            textViewRightEye.setText(R.string.both_eyes);
            // 左目のレイアウトを非表示
            layoutLeftEye.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onClickUseStartDate(final View v) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Preferenceに保存するキーを求める
        final String saveKey;
        if (v.getId() == R.id.textViewRightUseStartDate) {
            saveKey = "right_use_start_date";
        } else if (v.getId() == R.id.textViewLeftUseStartDate) {
            saveKey = "left_use_start_date";
        } else {
            throw new IllegalStateException();
        }

        // 現在の時刻
        Calendar nowCalendar = Calendar.getInstance();
        int year = nowCalendar.get(Calendar.YEAR);
        int monthOfYear = nowCalendar.get(Calendar.MONTH);
        int dayOfMonth = nowCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view,
                                  int year, int monthOfYear, int dayOfMonth) {
                Calendar selectCalendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);

                // 開始日を保存
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(saveKey, saveDateFormat.format(selectCalendar.getTime()));
                editor.commit();

                // 画面に反映
                setTextUseDate(v.getId(), selectCalendar);
            }
        }, year, monthOfYear, dayOfMonth);

        datePicker.show();
    }

    /**
     * 開始日と終了日をTextViewに設定します
     *
     * @param startViewId
     * @param baseCalendar
     */
    private void setTextUseDate(int startViewId, Calendar baseCalendar) {
        // 開始日を設定
        TextView startDateView = (TextView) findViewById(startViewId);
        startDateView.setText(viewDateFormat.format(baseCalendar.getTime()));

        // 開始日/終了日の対応
        int endViewId;
        if (startViewId == R.id.textViewRightUseStartDate) {
            endViewId = R.id.textViewRightUseEndDate;
        } else if (startViewId == R.id.textViewLeftUseStartDate) {
            endViewId = R.id.textViewLeftUseEndDate;
        } else {
            throw new IllegalStateException();
        }

        // 終了日を設定
        TextView endDateView = (TextView) findViewById(endViewId);
        addUseDate(baseCalendar);
        endDateView.setText(viewDateFormat.format(baseCalendar.getTime()));
    }

    /**
     * コンタクトレンズの利用日数を加算します
     *
     * @param baseCalendar
     */
    private void addUseDate(Calendar baseCalendar) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String lendsType = prefs.getString("lends_type", "2w");

        switch (lendsType) {
            case "2w":
                baseCalendar.add(Calendar.DAY_OF_MONTH, 14);
                break;
            case "1m":
                baseCalendar.add(Calendar.MONTH, 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
