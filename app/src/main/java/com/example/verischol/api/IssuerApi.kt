package com.example.verischol.api

import retrofit2.http.Body
import retrofit2.http.POST

data class IssueRequest(
    val subject: Map<String, Any>
)

data class IssueResponse(
    val vc: Map<String, Any>
)

interface IssuerApi {

    @POST("/issue")
    suspend fun issueVC(@Body request: IssueRequest): IssueResponse
}
