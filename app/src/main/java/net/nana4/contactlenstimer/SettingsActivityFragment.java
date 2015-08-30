package net.nana4.contactlenstimer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import org.bostonandroid.timepreference.TimePreference;

public class SettingsActivityFragment extends PreferenceFragment {
    private ListPreference lendsType;
    private SwitchPreference lendsSeparately;
    private SwitchPreference notification;
    private TimePreference notificationTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        lendsType = (ListPreference) findPreference("lends_type");
        lendsSeparately = (SwitchPreference) findPreference("lends_separately");
        notification = (SwitchPreference) findPreference("notification");
        notificationTime = (TimePreference) findPreference("notification_time");

        // レンズ種類変更時
        lendsType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                if (value == null) {
                    return false;
                }

                // レンズ種類をsummaryに表示
                int listIndex = lendsType.findIndexOfValue((String) value);
                preference.setSummary(lendsType.getEntries()[listIndex]);
                return true;
            }
        });

        // 左右別々変更時
        lendsSeparately.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                if ((Boolean) value) {
                    // 左右別々に設定した場合、右目の日付を左目にコピーする。
                    String rightUseStartDate = prefs.getString("right_use_start_date", null);

                    if (rightUseStartDate != null) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("left_use_start_date", rightUseStartDate);
                        editor.commit();
                    }
                } else {
                    // 左右別々に設定しない場合、左目の日付を削除する
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("left_use_start_date");
                    editor.commit();
                }

                return true;
            }
        });

        // 通知設定変更時
        notification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                // 通知設定が無効な場合、通知時間の選択を無効にする
                notificationTime.setEnabled((Boolean) value);

                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // レンズ種類をsummaryに表示
        int listIndex = lendsType.findIndexOfValue(lendsType.getValue());
        lendsType.setSummary(lendsType.getEntries()[listIndex]);

        // 通知設定が無効な場合、通知時間の選択を無効にする
        notificationTime.setEnabled(notification.isChecked());
    }

}
