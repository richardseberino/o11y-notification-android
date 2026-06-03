package com.seberino.dynatraceproblemsapp.data.remote

import com.seberino.dynatraceproblemsapp.data.model.ManagementZoneResponse
import com.seberino.dynatraceproblemsapp.data.model.Problem
import com.seberino.dynatraceproblemsapp.data.model.ProblemsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface DynatraceApiService {
    @GET
    suspend fun getProblems(
        @Url url: String,
        @Header("Authorization") token: String,
        @Query("problemSelector") problemSelector: String? = null,
        @Query("from") from: String? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("nextPageKey") nextPageKey: String? = null
    ): ProblemsResponse

    @GET
    suspend fun getProblemDetails(
        @Url url: String,
        @Header("Authorization") token: String,
        @Query("fields") fields: String = "+evidenceDetails"
    ): Problem

    @GET
    suspend fun getManagementZones(
        @Url url: String,
        @Header("Authorization") token: String
    ): ManagementZoneResponse
}
