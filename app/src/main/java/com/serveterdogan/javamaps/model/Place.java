package com.serveterdogan.javamaps.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity //  room database bu sınıfı entity sınıfı olarak kullanacağımızı sölüyoruz
public class Place implements Serializable {

    @PrimaryKey(autoGenerate = true) // otomatik bizim için id lerini oluşturacak
    public int id;

    @ColumnInfo(name = "name")
    public String name ;
    @ColumnInfo(name = "latitude")
    public double latitude;
    @ColumnInfo(name = "longitude")
    public double longitude;

    public Place(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
