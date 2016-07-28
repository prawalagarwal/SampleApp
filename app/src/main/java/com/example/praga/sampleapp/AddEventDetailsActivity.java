package com.example.praga.sampleapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

public class AddEventDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView eventName;
    private TextView eventDescription;
    private TextView eventLocation;
    private int year, month, day;
    private int hour, min;
    private Calendar calendar;
    private Button dateButton;
    private Button timeButton;
    private String format = "";
    private Spinner hoursSpinner;
    private String numberOfHours;
    private Button saveButton;
    private Button cancelButton;
    private String eventNameString;
    private String eventDescriptionString;
    private String eventLocationString;
    private String eventDateString;
    private String eventTimeString;
    private String eventHoursString;

    private MobileServiceClient mClient;
    private MobileServiceTable<EventV5> mEventTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ngo_add_event_page);

        eventName = (TextView)findViewById(R.id.add_event_event_name);
        eventDescription = (TextView)findViewById(R.id.add_event_event_description);
        eventLocation = (TextView)findViewById(R.id.add_event_event_location);
        dateButton = (Button)findViewById(R.id.add_event_date_button);
        timeButton = (Button)findViewById(R.id.add_event_time_button);
        hoursSpinner = (Spinner)findViewById(R.id.add_event_hours);
        saveButton = (Button)findViewById(R.id.add_event_accept);
        cancelButton = (Button)findViewById(R.id.add_event_reject);
        hoursSpinner.setOnItemSelectedListener(this);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);

        showDate(year, month+1, day);
        showTime(hour, min);

        try {
            mClient = new MobileServiceClient(
                    "https://abpriyad.azurewebsites.net",
                    this);

            mEventTable = mClient.getTable(EventV5.class);
        } catch (MalformedURLException e) {
            //createAndShowDialog(new Exception("Please verify if device is having internet connectivity"), "Error");
        } catch (Exception e){
            //createAndShowDialog(e, "Error");
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.num_hours_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hoursSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventNameString = eventName.getText().toString();
                eventDescriptionString = eventDescription.getText().toString();
                eventLocationString = eventLocation.getText().toString();
                eventDateString = dateButton.getText().toString();
                eventTimeString = timeButton.getText().toString();
                eventHoursString = hoursSpinner.getSelectedItem().toString();

                if(eventNameString.isEmpty() || eventDescriptionString.isEmpty() || eventLocationString.isEmpty() || eventDateString.isEmpty() || eventTimeString.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Some Fields are empty. Please fill all the above fields.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Saving Event Details. Please Wait...", Toast.LENGTH_SHORT).show();

                    SaveEventDataToTable();

                    Toast.makeText(getApplicationContext(), "Saved!.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });


    }

    private void SaveEventDataToTable() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    EventV5 event = new EventV5();
                    event.EventName = eventNameString;
                    event.Location = eventLocationString;
                    event.StartDateTime = eventDateString + " " + eventTimeString;
                    event.Category = "Social";
                    event.NgoId = "cdacb36bf91945eaacbff4e2fc48409c";
                    event.Duration = eventHoursString;

                    mEventTable.insert(event).get();
                }
                catch (Exception e)
                {
                    //createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
    }

    @SuppressWarnings("deprecation")
    public void setTime(View view) {
        showDialog(888);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        else if(id == 888) {
            return new TimePickerDialog(this, myTimeListener, hour, min, false);
        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker arg0, int hourOfDay, int minute) {
            showTime(hourOfDay, minute);
        }
    };

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            showDate(arg1, arg2+1, arg3);
        }
    };

    private void showTime(int hourOfDay, int minute) {
        if (hourOfDay == 0) {
            hourOfDay += 12;
            format = "AM";
        }
        else if (hourOfDay == 12) {
            format = "PM";
        } else if (hourOfDay > 12) {
            hourOfDay -= 12;
            format = "PM";
        } else {
            format = "AM";
        }
        timeButton.setText(new StringBuilder().append(hourOfDay).append(" : ").append(minute)
                .append(" ").append(format));
    }

    private void showDate(int year, int month, int day) {
        dateButton.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        String numberOfHours = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    /*private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }*/
}
