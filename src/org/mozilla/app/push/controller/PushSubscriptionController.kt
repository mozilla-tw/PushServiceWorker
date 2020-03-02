package org.mozilla.app.push.controller

import com.google.gson.Gson
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.mozilla.app.push.model.fcm.notifyFcm
import org.mozilla.app.push.model.pubsub.PubSubPayload
import org.mozilla.app.push.model.pubsub.WorkerRequest
import java.util.Base64

fun Routing.pushSubscription(pool: HikariDataSource) {
    post("/") {
        val pushLogRepository = PushLogRepository(pool)

        val receiveText = call.receiveText()

        application.log.info("[worker][receiveText]$receiveText")

        // todo: add log start handle
        val workId = getWorkId(receiveText)

        application.log.info("[worker][workId]$workId")

        // get data from worker table using workId
        // log error in each lambda
        pushLogRepository.handleWorkId(workId) { dataByWorkId: String? ->
            WorkerRequest.fromJson(dataByWorkId ?: "")?.notifyFcm()?.forEach { response ->
                pushLogRepository.log(response)
            }
        }
        call.respondText("Completed", contentType = ContentType.Text.Plain)
    }
}

private fun getWorkId(receiveText: String): String {
    val body = Gson().fromJson(receiveText, PubSubPayload::class.java)
    val data = body.pubSubMessage.data
    var messageId = body.pubSubMessage.messageId // what can I do with this ID?
    val decoder = Base64.getDecoder()
    val pubSubMessage = decoder.decode(data)
    val stringPubsubMessage = String(pubSubMessage)
    return stringPubsubMessage
}
