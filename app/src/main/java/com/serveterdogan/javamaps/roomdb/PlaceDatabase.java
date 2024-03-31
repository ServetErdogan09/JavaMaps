package com.serveterdogan.javamaps.roomdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.serveterdogan.javamaps.model.Place;

@Database(entities = {Place.class},version = 1)
public abstract class PlaceDatabase extends RoomDatabase {


    // geriye PlaceDao döndürecek
    public abstract PlaceDao placeDao();


}
