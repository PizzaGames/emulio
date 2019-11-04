package com.github.emulio.scrapers.thegamesdb.client.service

import mu.KotlinLogging
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RestRequestService(
        val baseUrl: String
) {

    val logger = KotlinLogging.logger { }

    companion object {
        val ContentType = "Content-Type"
        val Accept = "Accept"
        val JsonMediaType = "application/json"
        val FormDataMediaType = "multipart/form-data"
        val FormUrlEncMediaType = "application/x-www-form-urlencoded"
        val XmlMediaType = "application/xml"

        @JvmStatic
        val client by lazy {
            builder.build()
        }

        @JvmStatic
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
    }

    fun request(requestConfig: RequestConfig, body: Any?) {
        logger.debug { "requestConfig: $requestConfig; body: $body" }
        val url = buildUrl(requestConfig)
        val headers = safeHeaders(requestConfig)
        val request = buildRequest(headers, requestConfig, url, body)

        val response = client.newCall(request).execute()

        println(request)


    }

    private fun buildRequest(headers: Map<String, String>, request: RequestConfig, url: HttpUrl, body: Any?): Request {
        val contentType = headers[ContentType] ?: JsonMediaType

        var requestBuilder: Request.Builder = when (request.method) {
            RequestMethod.DELETE -> Request.Builder().url(url).delete()
            RequestMethod.GET -> Request.Builder().url(url)
            RequestMethod.HEAD -> Request.Builder().url(url).head()
            RequestMethod.PATCH -> Request.Builder().url(url).patch(requestBody(body, contentType))
            RequestMethod.PUT -> Request.Builder().url(url).put(requestBody(body, contentType))
            RequestMethod.POST -> Request.Builder().url(url).post(requestBody(body, contentType))
            RequestMethod.OPTIONS -> Request.Builder().url(url).method("OPTIONS", null)
        }

        headers.forEach { header -> requestBuilder = requestBuilder.addHeader(header.key, header.value) }

        val request = requestBuilder.build()
        return request
    }

    private fun requestBody(content: Any?, mediaType: String): RequestBody {

        return when {
            content is File -> {
                requestBodyFile(content, mediaType)
                }
            (mediaType == FormDataMediaType || mediaType == FormUrlEncMediaType) && content is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                requestBodyFormData(content as Map<String, String>)
            }
            mediaType == JsonMediaType -> requestBodyJson(content)
            mediaType == XmlMediaType -> TODO("xml not currently supported.")
            else -> TODO("requestBody currently only supports JSON, File and FormData body.")
        }
    }

    private fun requestBodyJson(content: Any?): RequestBody {
        return "{}".toRequestBody(JsonMediaType.toMediaTypeOrNull())
    }

    private fun requestBodyFormData(content: Map<String, String>): FormBody {
        var builder = FormBody.Builder()
        content.forEach { (key, value) ->
            builder = builder.add(key, value)
        }
        return builder.build()
    }

    private fun requestBodyFile(content: File, mediaType: String): RequestBody {
        return content.asRequestBody(mediaType.toMediaTypeOrNull())
    }

//    private inline fun <reified T: Any?> responseBody(body: ResponseBody?, mediaType: String? = JsonMediaType): T? {
//        if (body == null) {
//            return null
//        }
//        val bodyContent = body.string()
//        if (bodyContent.isEmpty()) {
//            return null
//        }
//        return when(mediaType) {
//            JsonMediaType -> Moshi.Builder().add(object {
//                @ToJson
//                fun toJson(uuid: UUID) = uuid.toString()
//                @FromJson
//                fun fromJson(s: String) = UUID.fromString(s)
//            })
//                    .add(ByteArrayAdapter())
//                    .build().adapter(T::class.java).fromJson(bodyContent)
//            else ->  TODO("responseBody currently only supports JSON body.")
//        }
//    }

    private fun safeHeaders(request: RequestConfig): Map<String, String> {
        return request.headers.let {
            val mutableHeaders = it.toMutableMap()

            // TODO: support multiple contentType options here.
            mutableHeaders[ContentType] = (it[ContentType] ?: JsonMediaType).substringBefore(";").toLowerCase()
            mutableHeaders[Accept] = it[Accept] ?: JsonMediaType
            mutableHeaders
        }
    }

    private fun buildUrl(request: RequestConfig): HttpUrl {
        val httpUrl = httpUrl()
        val urlBuilder = httpUrl.newBuilder().addPathSegment(request.path.trimStart('/'))

        request.query.entries.forEach { (key, value) ->
            value.forEach { urlBuilder.addQueryParameter(key, it) }
        }

        val url = urlBuilder.build()
        return url
    }

    private fun httpUrl() = baseUrl.toHttpUrlOrNull() ?: throw IllegalStateException("baseUrl is invalid.")

}

enum class RequestMethod { GET, DELETE, HEAD, OPTIONS, PATCH, POST, PUT }

data class RequestConfig(
    val method: RequestMethod,
    val path: String,
    val headers: Map<String, String> = mapOf(),
    val query: Map<String, List<String>> = mapOf()
)