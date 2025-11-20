package dev.masonak.easytripplanner.data.entity.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.masonak.easytripplanner.data.entity.Excursion;

@Dao
public interface ExcursionDao {

    @Query("SELECT COUNT(*) FROM excursions WHERE vacation_id = :vacationId")
    int getExcursionCountForVacation(int vacationId);

    @Query("SELECT * FROM excursions WHERE vacation_id = :vacationId")
    List<Excursion> getExcursionsForVacation(int vacationId);

    @Query("SELECT * FROM excursions WHERE id = :id LIMIT 1")
    Excursion getExcursionById(int id);

    @Insert
    long insert(Excursion excursion);

    @Update
    void update(Excursion excursion);

    @Delete
    void delete(Excursion excursion);

}