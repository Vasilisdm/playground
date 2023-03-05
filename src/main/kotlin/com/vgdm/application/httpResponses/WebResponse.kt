package com.vgdm.application.httpResponses

sealed class WebResponse {
    abstract val statusCode: Int
    abstract val headers: Map<String, List<String>>

    abstract fun copyResponse(
        statusCode: Int,
        headers: Map<String, List<String>>
    ): WebResponse

    fun headers(): Map<String, List<String>> =
        headers
            .map { it.key.lowercase() to it.value }
            .fold(mapOf()) { headersList, (headerKey, headerValue) ->
                headersList.plus(
                    Pair(headerKey, headersList.getOrDefault(headerKey, listOf()).plus(headerValue))
                )
            }

    fun header(headerName: String, headerValue: String) =
        header(headerName, listOf(headerValue))

    private fun header(headerName: String, headerValue: List<String>) =
        copyResponse(
            statusCode,
            headers.plus(
                Pair(
                    headerName,
                    headers.getOrDefault(headerName, listOf())
                        .plus(headerValue)
                )
            )
        )
}

data class TextResponse(
    val body: String,
    override val statusCode: Int = 200,
    override val headers: Map<String, List<String>> = mapOf()
) : WebResponse() {
    override fun copyResponse(statusCode: Int, headers: Map<String, List<String>>): WebResponse =
        copy(body = body, statusCode = statusCode, headers = headers)
}

data class JsonResponse(
    val body: Any?,
    override val statusCode: Int = 200,
    override val headers: Map<String, List<String>> = mapOf()
) : WebResponse() {
    override fun copyResponse(statusCode: Int, headers: Map<String, List<String>>): WebResponse =
        copy(body = body, statusCode = statusCode, headers = headers)
}
