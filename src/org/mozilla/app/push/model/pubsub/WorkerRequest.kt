package org.mozilla.app.push.model.pubsub

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.mozilla.app.push.util.logger
import java.util.UUID

data class WorkerRequest(
    val mozClientIds: MutableList<String>,
    val title: String,
    val body: String,
    val destination: String,
    val displayType: String,
    val displayTimestamp: Long,
    val mozMessageId: String,
    val mozMsgBatch: String,
    val appId: String,
    val imageUrl: String?,
    val sender: String,
    val pushId: String = UUID.randomUUID().toString(),
    val createdTimestamp: Long = System.currentTimeMillis(),
    val pushCommand: String?,
    val pushOpenUrl: String?,
    val pushDeepLink: String?
) {
    companion object {
        private val mapper = GsonBuilder().disableHtmlEscaping().create()

        fun fromJson(data: String): WorkerRequest? {
            return try {
                mapper.fromJson(data, WorkerRequest::class.java)
            } catch (jsonSyntaxException: JsonSyntaxException) {
                logger().error("[worker][WorkerRequest][fromJson]${jsonSyntaxException.message}")
                null
            }
        }
    }
}
