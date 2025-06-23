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

    // It might also be useful to have a method to fetch multiple users by a list of IDs
    // For example: @Query("SELECT * FROM recipe WHERE uid IN (:userIds)")
    // List<User> loadAllByIds(List<Integer> userIds);
    // For now, getUserById will be called in a loop in FavoritesFragment.
    // If performance becomes an issue with many favorites, loadAllByIds would be better.
}
