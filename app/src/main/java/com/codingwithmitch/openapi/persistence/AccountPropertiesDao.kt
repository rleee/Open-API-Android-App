package com.codingwithmitch.openapi.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingwithmitch.openapi.models.AccountPropertiesModel

@Dao
interface AccountPropertiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // if there is duplicate / the same object exists it'll get replaced
    fun insertAndReplace(accountPropertiesModel: AccountPropertiesModel): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE) // if data exists, dont insert / just ignore it
    fun  insertAndIgnore(accountPropertiesModel: AccountPropertiesModel): Long

    @Query("SELECT * FROM account_properties WHERE pk = :pk")
    fun searchByPk(pk: Int): AccountPropertiesModel?

    @Query("SELECT * FROM account_properties WHERE email = :email")
    fun searchByEmail(email: String): AccountPropertiesModel?
}