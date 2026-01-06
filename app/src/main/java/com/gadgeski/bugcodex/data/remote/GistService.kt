package com.gadgeski.bugcodex.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface GistService {
    @POST("gists")
    suspend fun createGist(
        @Header("Authorization") authHeader: String,
        @Body request: GistRequest,
    ): GistResponse

    // ★ Added: 既存のGistを更新するAPI
    @PATCH("gists/{id}")
    suspend fun updateGist(
        @Header("Authorization") authHeader: String,
        @Path("id") gistId: String,
        @Body request: GistRequest,
    ): GistResponse
}
