package com.seberino.dynatraceproblemsapp.data

import android.content.Context
import androidx.room.Room
import com.seberino.dynatraceproblemsapp.data.local.AppDatabase
import com.seberino.dynatraceproblemsapp.data.remote.DynatraceApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Graph {
    lateinit var database: AppDatabase
        private set

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://localhost/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DynatraceApiService::class.java)
    }

    val repository by lazy {
        DynatraceRepository(
            instanceDao = database.instanceDao(),
            apiService = apiService
        )
    }

    fun provide(context: Context) {
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dynatrace_problems.db"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }
}
