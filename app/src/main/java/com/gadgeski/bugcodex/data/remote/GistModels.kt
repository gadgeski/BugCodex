package com.gadgeski.bugcodex.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GistRequest(
    val description: String,
    val public: Boolean = false,
    val files: Map<String, GistFileContent>,
)

@JsonClass(generateAdapter = true)
data class GistFileContent(
    val content: String,
)

@JsonClass(generateAdapter = true)
data class GistResponse(
    val id: String,
    // ★ Fix: @param: を追加して、アノテーションの適用先を明示（警告解消）
    @param:Json(name = "html_url") val htmlUrl: String,
    val description: String?,
)
