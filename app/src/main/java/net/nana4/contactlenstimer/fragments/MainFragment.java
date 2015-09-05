package net.nana4.contactlenstimer.fragments;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import net.nana4.contactlenstimer.R;
import net.nana4.contactlenstimer.utils.ContactLendsTimerUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Shunichiro AKI on 2015/09/03.
 */
public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private DateFormat viewDateFormat;

    private List<TextView> startDateViewList;
    private List<TextView> endDateViewList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        boolean lensSeparately = prefs.getBoolean("lends_separately", false);

        int[] startDateIds = new int[]{R.id.textViewRightUseStartDate, R.id.textViewLeftUseStartDate};
        int[] endDateIds = new int[]{R.id.textViewRightUseEndDate, R.id.textViewLeftUseEndDate};

        // 左右別々のチェック有無で画面を選択する
        if (lensSeparately) {
            rootView = inflater.inflate(R.layout.fragment_main_separately, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
        }

        startDateViewList = new ArrayList<>();
        endDateViewList = new ArrayList<>();

        for (int idx = 0; idx < startDateIds.length; idx++) {
            TextView startDateView = (TextView) rootView.findViewById(startDateIds[idx]);

            if (startDateView != null) {
                startDateViewList.add(startDateView);
                endDateViewList.add((TextView) rootView.findViewById(endDateIds[idx]));
            }
        }

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 日付フォーマット
        viewDateFormat = android.text.format.DateFormat.getLongDateFormat(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();

        View.OnClickListener onClickStartDate = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

                // Preferenceに保存するキーを求める
                final String saveKey = ContactLendsTimerUtils.getUseStartDateKey(view);

                // 現在の時刻
                Calendar nowCalendar = Calendar.getInstance();
                int year = nowCalendar.get(Calendar.YEAR);
                int monthOfYear = nowCalendar.get(Calendar.MONTH);
                int dayOfMonth = nowCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker,
                                          int year, int monthOfYear, int dayOfMonth) {
                        // 二重呼び出しを防止
                        if (!datePicker.isShown()) {
                            return;
                        }

                        Calendar selectCalendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);

                        // 開始日を保存
                        SharedPreferences.Editor editor = prefs.edit();
                        String saveDate = ContactLendsTimerUtils.formatSaveDate(getActivity(), selectCalendar.getTime());
                        editor.putString(saveKey, saveDate);
                        editor.commit();

                        // 画面に反映
                        setTextUseDate((TextView) view, selectCalendar);

                        // 通知を更新
                        ContactLendsTimerUtils.updateTimer(getActivity());
                    }
                }, year, monthOfYear, dayOfMonth);

                datePicker.show();
            }
        };

        for (TextView startDateView : startDateViewList) {
            startDateView.setOnClickListener(onClickStartDate);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        for (TextView startDateView : startDateViewList) {
            Calendar calendar = Calendar.getInstance();
            String date = prefs.getString(ContactLendsTimerUtils.getUseStartDateKey(startDateView), null);

            if (date != null) {
                calendar.setTime(ContactLendsTimerUtils.parseSaveDate(getActivity(), date));
                setTextUseDate(startDateView, calendar);
            }
        }
    }

    /**
     * 開始日と終了日をTextViewに設定します
     *
     * @param startDateView
     * @param baseCalendar
     */
    private void setTextUseDate(TextView startDateView, Calendar baseCalendar) {
        TextView endDateView = endDateViewList.get(startDateViewList.indexOf(startDateView));

        // 開始日を設定
        startDateView.setText(viewDateFormat.format(baseCalendar.getTime()));

        // 終了日を設定
        ContactLendsTimerUtils.addUseDate(getActivity(), baseCalendar);
        endDateView.setText(viewDateFormat.format(baseCalendar.getTime()));
    }

}
