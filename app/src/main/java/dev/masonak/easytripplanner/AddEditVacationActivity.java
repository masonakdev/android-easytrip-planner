package dev.masonak.easytripplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dev.masonak.easytripplanner.data.AppDatabase;
import dev.masonak.easytripplanner.data.entity.Excursion;
import dev.masonak.easytripplanner.data.entity.Vacation;
import dev.masonak.easytripplanner.ui.excursion.ExcursionListAdapter;
import dev.masonak.easytripplanner.ui.vacation.VacationAlertReceiver;

public class AddEditVacationActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextAccommodation;
    private EditText editTextStartDate;
    private EditText editTextEndDate;
    private Button buttonSaveVacation;
    private Button buttonDeleteVacation;
    private Button buttonStartAlert;
    private Button buttonEndAlert;
    private Button buttonShareVacation;
    private Button buttonAddExcursion;
    private LinearLayout layoutExcursionsSection;
    private RecyclerView recyclerViewExcursions;

    private AppDatabase db;
    private Vacation currentVacation;
    private int vacationId;

    private ExcursionListAdapter excursionListAdapter;
    private List<Excursion> excursionsForVacation = new ArrayList<>();

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_vacation);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextAccommodation = findViewById(R.id.editTextAccommodation);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        buttonSaveVacation = findViewById(R.id.buttonSaveVacation);
        buttonDeleteVacation = findViewById(R.id.buttonDeleteVacation);
        buttonStartAlert = findViewById(R.id.buttonStartAlert);
        buttonEndAlert = findViewById(R.id.buttonEndAlert);
        buttonShareVacation = findViewById(R.id.buttonShareVacation);
        buttonAddExcursion = findViewById(R.id.buttonAddExcursion);
        layoutExcursionsSection = findViewById(R.id.layoutExcursionsSection);
        recyclerViewExcursions = findViewById(R.id.recyclerViewExcursions);

        db = AppDatabase.getInstance(getApplicationContext());

        vacationId = getIntent().getIntExtra("vacation_id", -1);

        recyclerViewExcursions.setLayoutManager(new LinearLayoutManager(this));
        excursionListAdapter = new ExcursionListAdapter(excursionsForVacation, new ExcursionListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Excursion excursion) {
                Intent intent = new Intent(AddEditVacationActivity.this, AddEditExcursionActivity.class);
                intent.putExtra("vacation_id", vacationId);
                intent.putExtra("excursion_id", excursion.getId());
                startActivity(intent);
            }
        });
        recyclerViewExcursions.setAdapter(excursionListAdapter);

        if (vacationId == -1) {
            buttonDeleteVacation.setVisibility(View.GONE);
            buttonStartAlert.setVisibility(View.GONE);
            buttonEndAlert.setVisibility(View.GONE);
            layoutExcursionsSection.setVisibility(View.GONE);
        } else {
            loadVacationFromDatabase();
        }

        buttonSaveVacation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVacation();
            }
        });

        buttonDeleteVacation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteVacationWithValidation();
            }
        });

        buttonStartAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleVacationAlert(true);
            }
        });

        buttonEndAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleVacationAlert(false);
            }
        });

        buttonShareVacation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareVacationDetails();
            }
        });

        buttonAddExcursion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vacationId == -1) {
                    Toast.makeText(AddEditVacationActivity.this, "Save the vacation before adding excursions", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(AddEditVacationActivity.this, AddEditExcursionActivity.class);
                    intent.putExtra("vacation_id", vacationId);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vacationId != -1) {
            loadExcursionsFromDatabase();
        }
    }

    private void loadVacationFromDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                currentVacation = db.vacationDao().getVacationById(vacationId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currentVacation != null) {
                            editTextTitle.setText(currentVacation.getTitle());
                            editTextAccommodation.setText(currentVacation.getAccommodation());
                            editTextStartDate.setText(currentVacation.getStartDate());
                            editTextEndDate.setText(currentVacation.getEndDate());
                            buttonDeleteVacation.setVisibility(View.VISIBLE);
                            buttonStartAlert.setVisibility(View.VISIBLE);
                            buttonEndAlert.setVisibility(View.VISIBLE);
                            layoutExcursionsSection.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }

    private void loadExcursionsFromDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Excursion> excursions = db.excursionDao().getExcursionsForVacation(vacationId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        excursionsForVacation = excursions;
                        excursionListAdapter.setExcursions(excursionsForVacation);
                    }
                });
            }
        }).start();
    }

    private void saveVacation() {
        final String title = editTextTitle.getText().toString().trim();
        final String accommodation = editTextAccommodation.getText().toString().trim();
        final String startDate = editTextStartDate.getText().toString().trim();
        final String endDate = editTextEndDate.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a vacation title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
            Toast.makeText(this, "Please enter both start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDateFormat(startDate) || !isValidDateFormat(endDate)) {
            Toast.makeText(this, "Dates must be in the format YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEndDateAfterStartDate(startDate, endDate)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (vacationId == -1) {
                    currentVacation = new Vacation();
                } else {
                    if (currentVacation == null) {
                        currentVacation = new Vacation();
                        currentVacation.setId(vacationId);
                    }
                }

                currentVacation.setTitle(title);
                currentVacation.setAccommodation(accommodation);
                currentVacation.setStartDate(startDate);
                currentVacation.setEndDate(endDate);

                if (vacationId == -1) {
                    long newId = db.vacationDao().insert(currentVacation);
                    vacationId = (int) newId;
                } else {
                    db.vacationDao().update(currentVacation);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (vacationId != -1) {
                            buttonDeleteVacation.setVisibility(View.VISIBLE);
                            buttonStartAlert.setVisibility(View.VISIBLE);
                            buttonEndAlert.setVisibility(View.VISIBLE);
                            layoutExcursionsSection.setVisibility(View.VISIBLE);
                        }
                        finish();
                    }
                });
            }
        }).start();
    }

    private void deleteVacationWithValidation() {
        if (currentVacation == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                int excursionCount = db.excursionDao().getExcursionCountForVacation(currentVacation.getId());
                if (excursionCount > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddEditVacationActivity.this, "Cannot delete a vacation that has excursions. Delete its excursions first.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    db.vacationDao().delete(currentVacation);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddEditVacationActivity.this, "Vacation deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }
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

    private boolean isEndDateAfterStartDate(String startDateStr, String endDateStr) {
        Date startDate = parseDate(startDateStr);
        Date endDate = parseDate(endDateStr);
        if (startDate == null || endDate == null) {
            return false;
        }
        return endDate.after(startDate);
    }

    private void scheduleVacationAlert(boolean isStart) {
        if (vacationId == -1) {
            Toast.makeText(this, "Save the vacation before setting alerts", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateStr = isStart
                ? editTextStartDate.getText().toString().trim()
                : editTextEndDate.getText().toString().trim();

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

        String title = editTextTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            title = "Vacation";
        }

        String typeText = isStart ? "starting" : "ending";
        String message = "Vacation " + typeText + ": " + title;

        Intent intent = new Intent(this, VacationAlertReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        int requestCode = vacationId * 10 + (isStart ? 1 : 2);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            String toastText = isStart ? "Start date alert set" : "End date alert set";
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareVacationDetails() {
        String title = editTextTitle.getText().toString().trim();
        String accommodation = editTextAccommodation.getText().toString().trim();
        String startDate = editTextStartDate.getText().toString().trim();
        String endDate = editTextEndDate.getText().toString().trim();

        StringBuilder builder = new StringBuilder();

        if (!TextUtils.isEmpty(title)) {
            builder.append("Vacation: ").append(title).append("\n");
        } else {
            builder.append("Vacation: (no title)\n");
        }

        if (!TextUtils.isEmpty(accommodation)) {
            builder.append("Accommodation: ").append(accommodation).append("\n");
        }

        if (!TextUtils.isEmpty(startDate)) {
            builder.append("Start date: ").append(startDate).append("\n");
        }

        if (!TextUtils.isEmpty(endDate)) {
            builder.append("End date: ").append(endDate).append("\n");
        }

        if (vacationId != -1 && excursionsForVacation != null) {
            builder.append("\nExcursions:\n");
            if (excursionsForVacation.isEmpty()) {
                builder.append("None\n");
            } else {
                for (Excursion excursion : excursionsForVacation) {
                    String excursionTitle = excursion.getTitle();
                    String excursionDate = excursion.getDate();
                    if (TextUtils.isEmpty(excursionTitle) && TextUtils.isEmpty(excursionDate)) {
                        continue;
                    }
                    builder.append("- ");
                    if (!TextUtils.isEmpty(excursionTitle)) {
                        builder.append(excursionTitle);
                    }
                    if (!TextUtils.isEmpty(excursionDate)) {
                        if (!TextUtils.isEmpty(excursionTitle)) {
                            builder.append(" on ");
                        }
                        builder.append(excursionDate);
                    }
                    builder.append("\n");
                }
            }
        }

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share vacation details");
        startActivity(shareIntent);
    }

}