package lee.james.earthquakemapper;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final Integer PICK_START_DATE = 0;
    public static final Integer PICk_END_DATE = 1;

    private static final String LOG_TAG = DatePickerFragment.class.getSimpleName();
    private int flag = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();

        final Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(activity, this, year, month, day);

        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(getActivity());

        if (activity.startDate == null || this.flag == DatePickerFragment.PICK_START_DATE) {
            datePicker.getDatePicker().setMinDate(earthquakeDatabase.getOldest().getDate().getTime());
        } else {
            datePicker.getDatePicker().setMinDate(activity.startDate.getTime());
        }

        if (activity.endDate == null) {
            datePicker.getDatePicker().setMaxDate(earthquakeDatabase.getLatest().getDate().getTime());
        } else {
            datePicker.getDatePicker().setMaxDate(activity.endDate.getTime());
        }

        return datePicker;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        MainActivity activity = (MainActivity) getActivity();

        if (this.flag == DatePickerFragment.PICK_START_DATE) {
            activity.startDate = new Date(year - 1900, month, day);
        } else {
            activity.endDate = new Date(year - 1900, month, day);
        }
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }
}
