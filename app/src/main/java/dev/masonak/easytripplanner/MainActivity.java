package dev.masonak.easytripplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.masonak.easytripplanner.data.AppDatabase;
import dev.masonak.easytripplanner.data.entity.Vacation;
import dev.masonak.easytripplanner.ui.vacation.VacationListAdapter;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private VacationListAdapter vacationListAdapter;
    private RecyclerView recyclerViewVacations;
    private Button buttonAddVacation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(getApplicationContext());

        recyclerViewVacations = findViewById(R.id.recyclerViewVacations);
        buttonAddVacation = findViewById(R.id.buttonAddVacation);

        recyclerViewVacations.setLayoutManager(new LinearLayoutManager(this));

        vacationListAdapter = new VacationListAdapter(new ArrayList<Vacation>(), new VacationListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Vacation vacation) {
                Intent intent = new Intent(MainActivity.this, AddEditVacationActivity.class);
                intent.putExtra("vacation_id", vacation.getId());
                startActivity(intent);
            }
        });

        recyclerViewVacations.setAdapter(vacationListAdapter);

        buttonAddVacation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditVacationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVacationsFromDatabase();
    }

    private void loadVacationsFromDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Vacation> vacations = db.vacationDao().getAllVacations();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        vacationListAdapter.setVacations(vacations);
                    }
                });
            }
        }).start();
    }

}