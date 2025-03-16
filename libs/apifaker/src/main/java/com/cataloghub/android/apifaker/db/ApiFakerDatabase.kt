package com.cataloghub.android.apifaker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cataloghub.android.apifaker.models.Request
import com.cataloghub.android.apifaker.models.Response

@Database(
    entities = [
        Request::class,
        Response::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    EndpointTypeConverter::class,
    QueryParameterConverter::class
)
internal abstract class ApiFakerDatabase : RoomDatabase() {
    companion object {
        fun buildDb(applicationContext: Context) = Room
            .databaseBuilder(
                context = applicationContext,
                klass = ApiFakerDatabase::class.java,
                name = "api-faker-db"
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    abstract val endpointDao: EndpointDao
}
