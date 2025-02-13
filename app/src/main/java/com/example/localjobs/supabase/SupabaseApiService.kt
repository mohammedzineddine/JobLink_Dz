package com.example.localjobs.supabase

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SupabaseApiService {
    @Multipart
    @Headers("Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdkeWVnZWpxY2ZpdGt2ZG11cHpmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQ1NTMwNjIsImV4cCI6MjA1MDEyOTA2Mn0.z7GLRrDnW-u7X1kAgGxob75TdcckzyAFMhoPkUK_Zy0")
    @POST("storage/v1/object/public/jobs/{fileName}")
    fun uploadImage(
        @Path("fileName") fileName: String,
        @Part image: MultipartBody.Part
    ): Call<Void>
}
