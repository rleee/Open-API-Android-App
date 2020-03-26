package com.codingwithmitch.openapi.di


import android.app.Application
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AppDatabase
import com.codingwithmitch.openapi.persistence.AppDatabase.Companion.DATABASE_NAME
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule{

    /**
     * CREATE DATABASE
     * @param:  application context
     *
     * @Sub-param: application context
     * @Sub-param: AppDatabase class (room)
     * @Sub-param: DATABASE_NAME (constant)
     *
     * @return: AppDatabase
     */
    @Singleton
    @Provides
    fun provideAppDb(app: Application): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration() // get correct db version if schema changed
            .build()
    }

    /**
     * ACCESS AUTH DAO
     * (Account_pk, Token)
     *
     * @param:  AppDatabase
     *
     * @return: AuthTokenDao
     */
    @Singleton
    @Provides
    fun provideAuthTokenDao(db: AppDatabase): AuthTokenDao {
        return db.getAuthTokenDao()
    }

    /**
     * ACCESS ACCOUNT PROPERTIES DAO
     * (user data: primaryKey, email, username)
     *
     * @param:  AppDatabase
     *
     * @return: AuthTokenDao
     */
    @Singleton
    @Provides
    fun provideAccountPropertiesDao(db: AppDatabase): AccountPropertiesDao {
        return db.getAccountPropertiesDao()
    }

    /**
     * CREATE GLIDE DEFAULT PICTURE
     *
     * resource image file: R.drawable.default_image
     *
     * @return: RequestOptions
     */
    @Singleton
    @Provides
    fun provideRequestOptions(): RequestOptions {
        return RequestOptions
            .placeholderOf(R.drawable.default_image)
            .error(R.drawable.default_image)
    }

    /**
     * SET GLIDE DEFAULT PICTURE
     *
     * @param:  application context
     * @param:  RequestOptions (default image)
     *
     * @return: RequestManager
     */
    @Singleton
    @Provides
    fun provideGlideInstance(application: Application, requestOptions: RequestOptions): RequestManager {
        return Glide.with(application)
            .setDefaultRequestOptions(requestOptions)
    }

}