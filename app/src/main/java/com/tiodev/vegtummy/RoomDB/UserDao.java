package com.tiodev.vegtummy.RoomDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.tiodev.vegtummy.RoomDB.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM recipe")
    List<User> getAll();

    @Query("SELECT * FROM recipe WHERE uid = :id LIMIT 1")
    User getUserById(int id);

    @Query("SELECT * FROM recipe WHERE uid IN (:uids)")
    List<User> getUsersByIds(List<Integer> uids);

}
