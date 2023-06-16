package com.capstone.sanggoroe.data.api

import com.capstone.sanggoroe.model.RecommendResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("recJob")
    fun getRecommendations(
        @Query("skills") skills: String
    ): Call<RecommendResponse>
}