package com.codingwithmitch.openapi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codingwithmitch.openapi.models.AccountPropertiesModel
import com.codingwithmitch.openapi.models.AuthTokenModel

@Database(entities = [AuthTokenModel::class, AccountPropertiesModel::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun  getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    companion object {
        const val DATABASE_NAME = "app_db"
    }
}