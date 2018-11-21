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

    private static final String LOG_TAG = DatePickerFragment.class.getSimpleName();

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
        Log.d(LOG_TAG, String.format("%s %d/%d/%d", "The user selected the date", year, month, day));
    }
}
