package net.nana4.contactlenstimer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import net.nana4.contactlenstimer.R;
import net.nana4.contactlenstimer.utils.ContactLendsTimerUtils;
import net.nana4.timepreference.TimePreference;

import java.util.Calendar;

public class SettingsFragment extends PreferenceFragment {
    private ListPreference lendsType;
    private CheckBoxPreference lendsSeparately;
    private SwitchPreference notification;
    private TimePreference notificationTime;
    private SwitchPreference repeatTimer;

    private boolean updateTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        lendsType = (ListPreference) findPreference("lends_type");
        lendsSeparately = (CheckBoxPreference) findPreference("lends_separately");
        notification = (SwitchPreference) findPreference("notification");
        notificationTime = (TimePreference) findPreference("notification_time");
        repeatTimer = (SwitchPreference) findPreference("repeat_timer");

        // レンズ種類変更時
        lendsType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                if (value == null) {
                    return false;
                }

                // レンズ種類をsummaryに表示
                int listIndex = lendsType.findIndexOfValue((String) value);
                preference.setSummary(lendsType.getEntries()[listIndex]);

                updateTimer = true;

                return true;
            }
        });

        // 左右別々変更時
        lendsSeparately.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateTimer = true;

                return true;
            }
        });

        // 通知設定変更時
        notification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                boolean enable = (Boolean) value;
                // 通知設定が無効な場合、通知時間/繰り返しの選択を無効にする
                notificationTime.setEnabled(enable);
                repeatTimer.setEnabled(enable);

                if (notificationTime.getSummary() == null) {
                    Calendar calendar = Calendar.getInstance();
                    notificationTime.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                }

                updateTimer = true;

                return true;
            }
        });

        // 通知時間変更時
        notificationTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                updateTimer = true;

                return true;
            }
        });

        // 繰り返し設定変更時
        repeatTimer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateTimer = true;

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

        updateTimer = false;
    }

    @Override
    public void onPause() {
        super.onPause();

        // 通知を更新
        if (updateTimer) {
            ContactLendsTimerUtils.updateTimer(getActivity());
        }
    }
}
