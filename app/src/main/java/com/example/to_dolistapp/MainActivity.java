package com.example.to_dolistapp;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private List<Task> taskList;
    private TaskAdapter taskAdapter;
    private EditText editTextTask;
    private TaskDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new TaskDBHelper(this);

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this, dbHelper);


        RecyclerView recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        editTextTask = findViewById(R.id.editTextTask);
        Button buttonAdd = findViewById(R.id.buttonAdd);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = editTextTask.getText().toString().trim();
                if (!taskName.isEmpty()) {
                    Task task = new Task(-1, taskName, false);
                    addTaskToDatabase(task);
                    taskList.add(task);
                    taskAdapter.notifyItemInserted(taskList.size() - 1);
                    editTextTask.setText("");
                }
            }
        });

        loadTasksFromDatabase();
    }

    private List<Task> loadTasksFromDatabase() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                TaskDBHelper.TABLE_TASKS,
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<Task> tasks = new ArrayList<>(); // Create a new list to hold tasks

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(TaskDBHelper.COLUMN_ID));
            String taskName = cursor.getString(cursor.getColumnIndex(TaskDBHelper.COLUMN_TASK_NAME));
            int isCompleted = cursor.getInt(cursor.getColumnIndex(TaskDBHelper.COLUMN_IS_COMPLETED));

            Task task = new Task(id, taskName, isCompleted == 1);
            tasks.add(task);
            taskList.add(task);
        }

        cursor.close();
        return tasks; // Return the list of tasks
    }


    private void addTaskToDatabase(Task task) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskDBHelper.COLUMN_TASK_NAME, task.getTaskName());
        values.put(TaskDBHelper.COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        long id = database.insert(TaskDBHelper.TABLE_TASKS, null, values);
        task.setId(id);
    }



    // Add this method to update a task in the database
    public void updateTaskInDatabase(Task task) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskDBHelper.COLUMN_TASK_NAME, task.getTaskName());
        values.put(TaskDBHelper.COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        database.update(
                TaskDBHelper.TABLE_TASKS,
                values,
                TaskDBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(task.getId())}
        );
    }
    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.record_task) {
                    // Handle Record Task option
                    showRecordTaskDialog();
                    return true;
                } else if (id == R.id.task_lists) {
                    // Handle Task Lists option
                    showTaskListDialog();
                    return true;
                }else if (id == R.id.settings) {
                    setAlarmTune(); // Call setAlarmTune() when "settings" is selected
                    return true;} else if (id == R.id.help) {
                    // Handle Help option
                    return true;
                }
                return false;
            }
        });

        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private void showRecordTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Record Task");

        View view = LayoutInflater.from(this).inflate(R.layout.record_task_dialog, null);
        builder.setView(view);

        final EditText taskEditText = view.findViewById(R.id.taskEditText);
        final CheckBox reminderCheckbox = view.findViewById(R.id.reminderCheckbox);
        final Button setDateButton = view.findViewById(R.id.setDateButton);

        setDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(); // Make sure this method is correctly implemented
            }
        });

        reminderCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showTimePickerDialog(); // Make sure this method is correctly implemented
                }
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskName = taskEditText.getText().toString();
                boolean hasReminder = reminderCheckbox.isChecked();
                Calendar calendar = Calendar.getInstance();
                calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
                String alarmTuneUri = getAlarmTuneUri().toString(); // Convert Uri to String
                saveTaskData(taskName, selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, hasReminder, alarmTuneUri);
                setAlarm(calendar, taskName, alarmTuneUri); // Make sure this method is correctly implemented
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }



    private void setAlarm(Calendar calendar, String taskName, String alarmTuneUri) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("taskName", taskName);
        intent.putExtra("alarmTuneUri", alarmTuneUri); // Pass the alarm tune URI
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // Set the alarm 5 minutes before the selected time
        long alarmTime = calendar.getTimeInMillis() - (5 * 60 * 1000); // 5 minutes in milliseconds

        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }






    private void saveTaskData(String taskName, int year, int month, int day, int hour, int minute, boolean hasReminder, String alarmTuneUri) {
        Task task = new Task(-1, taskName, false);
        addTaskToDatabase(task);
        if (hasReminder) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, minute);
            setAlarm(calendar, taskName, alarmTuneUri);
        }
    }








    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                    }
                }, hourOfDay, minute, false);
        timePickerDialog.show();
    }


    // Add this method to delete a task from the database
    public void deleteTaskFromDatabase(long taskId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(
                TaskDBHelper.TABLE_TASKS,
                TaskDBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(taskId)}
        );
    }
    private static final int PICK_AUDIO_REQUEST_CODE = 123;

    public void setAlarmTune() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedAudioUri = data.getData();
                saveAlarmTune(selectedAudioUri);
                Toast.makeText(this, "Alarm tune set successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAlarmTune(Uri selectedAudioUri) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("alarm_tune_uri", selectedAudioUri.toString());
        editor.apply();
    }

    private Uri getAlarmTuneUri() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String uriString = preferences.getString("alarm_tune_uri", null);
        if (uriString != null) {
            return Uri.parse(uriString);
        }
        return null;
    }

    private void showTaskListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Task Lists");

        // Inflate a layout containing a RecyclerView to display tasks
        View view = LayoutInflater.from(this).inflate(R.layout.task_list_dialog, null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTaskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create a list of tasks (you should fetch it from the database)
        List<Task> tasks = loadTasksFromDatabase(); // Implement this method

        // Create an adapter for the RecyclerView
        TaskListAdapter adapter = new TaskListAdapter(tasks);
        recyclerView.setAdapter(adapter);

        builder.setView(view);

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

}