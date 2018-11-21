package lee.james.earthquakemapper;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final Integer PICK_START_DATE = 0;
    public static final Integer PICk_END_DATE = 1;

    private static final String LOG_TAG = DatePickerFragment.class.getSimpleName();
    private int flag = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(getActivity(), this, year, month, day);

        EarthquakeDatabaseHelper earthquakeDatabase = new EarthquakeDatabaseHelper(getActivity());

        datePicker.getDatePicker().setMinDate(earthquakeDatabase.getOldest().getDate().getTime());
        datePicker.getDatePicker().setMaxDate(earthquakeDatabase.getLatest().getDate().getTime());

        return datePicker;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (this.flag == DatePickerFragment.PICK_START_DATE) {
            Log.d(LOG_TAG, String.format("%s %d/%d/%d", "Start date: ", year, month, day));
        } else {
            Log.d(LOG_TAG, String.format("%s %d/%d/%d", "End date: ", year, month, day));
        }
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }
}
