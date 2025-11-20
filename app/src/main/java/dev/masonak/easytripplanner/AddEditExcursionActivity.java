package dev.masonak.easytripplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dev.masonak.easytripplanner.data.AppDatabase;
import dev.masonak.easytripplanner.data.entity.Excursion;
import dev.masonak.easytripplanner.data.entity.Vacation;

public class AddEditExcursionActivity extends AppCompatActivity {

    private EditText editTextExcursionTitle;
    private EditText editTextExcursionDate;
    private Button buttonSaveExcursion;
    private Button buttonDeleteExcursion;
    private Button buttonSetExcursionAlert;

    private AppDatabase db;
    private int vacationId;
    private int excursionId;
    private Excursion currentExcursion;

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_excursion);

        editTextExcursionTitle = findViewById(R.id.editTextExcursionTitle);
        editTextExcursionDate = findViewById(R.id.editTextExcursionDate);
        buttonSaveExcursion = findViewById(R.id.buttonSaveExcursion);
        buttonDeleteExcursion = findViewById(R.id.buttonDeleteExcursion);
        buttonSetExcursionAlert = findViewById(R.id.buttonSetExcursionAlert);

        db = AppDatabase.getInstance(getApplicationContext());

        vacationId = getIntent().getIntExtra("vacation_id", -1);
        excursionId = getIntent().getIntExtra("excursion_id", -1);

        if (vacationId == -1) {
            Toast.makeText(this, "Vacation not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (excursionId == -1) {
            buttonDeleteExcursion.setVisibility(View.GONE);
            buttonSetExcursionAlert.setVisibility(View.GONE);
        } else {
            loadExcursionFromDatabase();
        }

        buttonSaveExcursion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExcursion();
            }
        });

        buttonDeleteExcursion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteExcursion();
            }
        });

        buttonSetExcursionAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleExcursionAlert();
            }
        });
    }

    private void loadExcursionFromDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                currentExcursion = db.excursionDao().getExcursionById(excursionId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currentExcursion != null) {
                            editTextExcursionTitle.setText(currentExcursion.getTitle());
                            editTextExcursionDate.setText(currentExcursion.getDate());
                            buttonDeleteExcursion.setVisibility(View.VISIBLE);
                            buttonSetExcursionAlert.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }

    private void saveExcursion() {
        final String title = editTextExcursionTitle.getText().toString().trim();
        final String date = editTextExcursionDate.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter an excursion title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please enter an excursion date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDateFormat(date)) {
            Toast.makeText(this, "Excursion date must be in the format YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            final Vacation vacation = db.vacationDao().getVacationById(vacationId);
            if (vacation == null || TextUtils.isEmpty(vacation.getStartDate()) || TextUtils.isEmpty(vacation.getEndDate())
                    || !isValidDateFormat(vacation.getStartDate()) || !isValidDateFormat(vacation.getEndDate())) {
                runOnUiThread(() -> Toast.makeText(AddEditExcursionActivity.this, "Set valid vacation dates before adding excursions", Toast.LENGTH_LONG).show());
                return;
            }

            Date excursionDate = parseDate(date);
            Date vacationStart = parseDate(vacation.getStartDate());
            Date vacationEnd = parseDate(vacation.getEndDate());

            if (excursionDate == null || vacationStart == null || vacationEnd == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddEditExcursionActivity.this, "Set valid dates before adding excursions", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }

            if (excursionDate.before(vacationStart) || excursionDate.after(vacationEnd)) {
                runOnUiThread(() -> Toast.makeText(AddEditExcursionActivity.this, "Excursion date must be between vacation start and end dates", Toast.LENGTH_LONG).show());
                return;
            }

            if (excursionId == -1) {
                currentExcursion = new Excursion();
                currentExcursion.setVacationId(vacationId);
            } else {
                if (currentExcursion == null) {
                    currentExcursion = new Excursion();
                    currentExcursion.setId(excursionId);
                    currentExcursion.setVacationId(vacationId);
                }
            }

            currentExcursion.setTitle(title);
            currentExcursion.setDate(date);

            if (excursionId == -1) {
                db.excursionDao().insert(currentExcursion);
            } else {
                db.excursionDao().update(currentExcursion);
            }

            runOnUiThread(() -> finish());
        }).start();
    }

    private void deleteExcursion() {
        if (currentExcursion == null) {
            return;
        }

        new Thread(() -> {
            db.excursionDao().delete(currentExcursion);
            runOnUiThread(() -> {
                Toast.makeText(AddEditExcursionActivity.this, "Excursion deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private boolean isValidDateFormat(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private Date parseDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US);
        sdf.setLenient(false);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private void scheduleExcursionAlert() {
        if (excursionId == -1) {
            Toast.makeText(this, "Save the excursion before setting an alert", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateStr = editTextExcursionDate.getText().toString().trim();
        if (!isValidDateFormat(dateStr)) {
            Toast.makeText(this, "Please enter a valid date in the format YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date = parseDate(dateStr);
        if (date == null) {
            Toast.makeText(this, "Please enter a valid date in the format YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long triggerAtMillis = calendar.getTimeInMillis();

        String title = editTextExcursionTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            title = "Excursion";
        }
        String message = "Excursion today: " + title;

        Intent intent = new Intent(this, dev.masonak.easytripplanner.ui.vacation.VacationAlertReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        int requestCode = 1000 + excursionId;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Toast.makeText(this, "Excursion alert set", Toast.LENGTH_SHORT).show();
        }
    }

}