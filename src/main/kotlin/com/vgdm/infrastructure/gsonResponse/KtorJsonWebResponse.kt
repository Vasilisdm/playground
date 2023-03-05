package com.vgdm.infrastructure.gsonResponse

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.charsets.Charsets

class KtorJsonWebResponse(
    val body: Any?,
    override val status: HttpStatusCode = HttpStatusCode.OK
) : OutgoingContent.ByteArrayContent() {

    override val contentType: ContentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)

    override fun bytes(): ByteArray = Gson().toJson(body).toByteArray(Charsets.UTF_8)
}