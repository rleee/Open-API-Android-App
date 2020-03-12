package com.codingwithmitch.openapi.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "account_properties")
data class AccountPropertiesModel (

    @SerializedName("pk") // Retrofit
    @Expose // Retrofit
    @PrimaryKey(autoGenerate = false) // Room
    @ColumnInfo(name = "pk") // Room
    var pk: Int,

    @SerializedName("email") // Retrofit
    @Expose // Retrofit
    @ColumnInfo(name = "email") // Room
    var email: String,

    @SerializedName("username") // Retrofit
    @Expose // Retrofit
    @ColumnInfo(name = "username") // Room
    var username: String
)