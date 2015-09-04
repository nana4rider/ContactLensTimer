package net.nana4.contactlenstimer.fragments;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import net.nana4.contactlenstimer.R;
import net.nana4.contactlenstimer.utils.ContactLendsTimerUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Shunichiro AKI on 2015/09/03.
 */
public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private DateFormat saveDateFormat;
    private DateFormat viewDateFormat;

    private TextView rightStartDateView;
    private TextView rightEndDateView;
    private TextView leftStartDateView;
    private TextView leftEndDateView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        boolean lensSeparately = prefs.getBoolean("lends_separately", false);

        View view;
        if (lensSeparately) {
            view = inflater.inflate(R.layout.fragment_main_separately, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_main, container, false);
        }

        rightStartDateView = (TextView) view.findViewById(R.id.textViewRightUseStartDate);
        rightEndDateView = (TextView) view.findViewById(R.id.textViewRightUseEndDate);
        leftStartDateView = (TextView) view.findViewById(R.id.textViewLeftUseStartDate);
        leftEndDateView = (TextView) view.findViewById(R.id.textViewLeftUseEndDate);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 日付フォーマット
        saveDateFormat = new SimpleDateFormat(getString(R.string.save_date_format));
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
                final String saveKey;
                if (view == rightStartDateView) {
                    saveKey = "right_use_start_date";
                } else if (view == leftStartDateView) {
                    saveKey = "left_use_start_date";
                } else {
                    throw new IllegalStateException();
                }

                // 現在の時刻
                Calendar nowCalendar = Calendar.getInstance();
                int year = nowCalendar.get(Calendar.YEAR);
                int monthOfYear = nowCalendar.get(Calendar.MONTH);
                int dayOfMonth = nowCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datePicker,
                                          int year, int monthOfYear, int dayOfMonth) {
                        // 二重呼び出しを防止
                        if (!view.isShown()) {
                            return;
                        }

                        Calendar selectCalendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);

                        // 開始日を保存
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(saveKey, saveDateFormat.format(selectCalendar.getTime()));
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

        if (rightStartDateView != null) {
            rightStartDateView.setOnClickListener(onClickStartDate);
        }
        if (leftStartDateView != null) {
            leftStartDateView.setOnClickListener(onClickStartDate);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        try {
            // 右目の保存設定を表示
            if (rightStartDateView != null) {
                Calendar calendar = Calendar.getInstance();
                String date = prefs.getString("right_use_start_date", null);

                if (date != null) {
                    calendar.setTime(saveDateFormat.parse(date));
                    setTextUseDate(rightStartDateView, calendar);
                }
            }

            // 左目の保存設定を表示
            if (leftStartDateView != null) {
                Calendar calendar = Calendar.getInstance();
                String date = prefs.getString("left_use_start_date", null);

                if (date != null) {
                    calendar.setTime(saveDateFormat.parse(date));
                    setTextUseDate(leftStartDateView, calendar);
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "invalid date format", e);
        }
    }

    /**
     * 開始日と終了日をTextViewに設定します
     *
     * @param startDateView
     * @param baseCalendar
     */
    private void setTextUseDate(TextView startDateView, Calendar baseCalendar) {
        // 開始日/終了日の対応
        TextView endDateView;
        if (startDateView == rightStartDateView) {
            endDateView = rightEndDateView;
        } else if (startDateView == leftStartDateView) {
            endDateView = leftEndDateView;
        } else {
            throw new IllegalStateException();
        }

        // 開始日を設定
        startDateView.setText(viewDateFormat.format(baseCalendar.getTime()));
        // 終了日を設定
        ContactLendsTimerUtils.addUseDate(getActivity(), baseCalendar);
        endDateView.setText(viewDateFormat.format(baseCalendar.getTime()));
    }

}
