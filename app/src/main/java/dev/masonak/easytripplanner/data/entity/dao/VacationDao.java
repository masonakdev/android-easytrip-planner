package dev.masonak.easytripplanner.data.entity.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.masonak.easytripplanner.data.entity.Vacation;

@Dao
public interface VacationDao {

    @Query("SELECT * FROM vacations ORDER BY start_date")
    List<Vacation> getAllVacations();

    @Query("SELECT * FROM vacations WHERE id = :id LIMIT 1")
    Vacation getVacationById(int id);

    @Insert
    long insert(Vacation vacation);

    @Update
    void update(Vacation vacation);

    @Delete
    void delete(Vacation vacation);

}