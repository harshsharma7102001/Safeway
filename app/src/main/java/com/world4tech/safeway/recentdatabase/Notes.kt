package com.world4tech.homework.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "Data")
class Notes(@ColumnInfo(name="location")var loc:String,
            @ColumnInfo(name="adress")var address:String,
            @ColumnInfo(name="latitide")var lat:String,
            @ColumnInfo(name="longitude")var lon:String,
            @PrimaryKey(autoGenerate = true)var id: Int =0)