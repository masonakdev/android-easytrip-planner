package dev.masonak.easytripplanner.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import dev.masonak.easytripplanner.data.entity.Excursion;
import dev.masonak.easytripplanner.data.entity.Vacation;
import dev.masonak.easytripplanner.data.entity.dao.ExcursionDao;
import dev.masonak.easytripplanner.data.entity.dao.VacationDao;

@Database(entities = {Vacation.class, Excursion.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract VacationDao vacationDao();

    public abstract ExcursionDao excursionDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "vacation_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}