package com.serveterdogan.javamaps.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.serveterdogan.javamaps.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlaceDao {

    @Query("SELECT * FROM Place")
    // bütün placekicker bir list halinde döndürecek o yüzden başına void eklemedik
   Flowable <List<Place>> getAll();

    @Insert
    Completable insert(Place place);

    @Delete
    Completable delete(Place place);


}
