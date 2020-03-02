package org.mozilla.app.push.model.fcm

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FcmOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.mozilla.app.push.model.pubsub.WorkerRequest
import org.mozilla.app.push.util.logger
import org.mozilla.app.user.data.UserRepository
import java.util.stream.Stream

open class WorkerRequestResponse(
    val mozMessageId: String,
    val mozClientId: String,
    val mozBatchId: String,
    val result: String
)

class Error(mozMessageId: String, mozClientId: String, mozBatchId: String, error: String) :
    WorkerRequestResponse(mozMessageId, mozClientId, mozBatchId, error)

class Success(mozMessageId: String, mozClientId: String, mozBatchId: String, fcmMessageId: String) :
    WorkerRequestResponse(mozMessageId, mozClientId, mozBatchId, fcmMessageId)

fun WorkerRequest.notifyFcm(): Stream<WorkerRequestResponse>? {
    logger().info("[worker][notifyFcm]--start--")
    return this.mozClientIds.parallelStream().map { mozClientId ->

        try {
            val fcmOptions = FcmOptions.builder().setAnalyticsLabel(this.mozMessageId).build()
            val androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setCollapseKey(this.mozMessageId).build()
            // when running integration test, this will return null so the result will still be logged.
            val fcmToken = UserRepository.getFcmToken(mozClientId)
            if (fcmToken == null) {
                return@map Error(
                    mozMessageId = this.mozMessageId,
                    mozClientId = mozClientId,
                    mozBatchId = this.mozMsgBatch,
                    error = "No token for such user"
                )
            }
            val messageBuilder = Message.builder()
                .setToken(fcmToken)
                .putData("body", this.body)
                .putData("title", this.title)
                .putData("app_id", this.appId)
                .putData("destination", this.destination)
                .putData("display_type", this.displayType)
                .putData("display_timestamp", this.displayTimestamp.toString())
                .putData("push_id", this.pushId)
                .putData("message_id", this.mozMessageId)
                .putData("sender", this.sender)
                .setAndroidConfig(androidConfig)
                .setFcmOptions(fcmOptions)

            this.imageUrl?.let {
                messageBuilder.putData("image_url", it)
            }

            this.pushOpenUrl?.let {
                messageBuilder.putData("push_open_url", it)
            }

            this.pushCommand?.let {
                messageBuilder.putData("push_command", it)
            }
            this.pushDeepLink?.let {
                messageBuilder.putData("push_deep_link", it)
            }

            val result = FirebaseMessaging.getInstance().send(messageBuilder.build())

            val fcmMessageId = result.split("/").last() // throw NoSuchElementException if the list is empty
            return@map Success(
                mozMessageId = this.mozMessageId,
                mozClientId = mozClientId,
                mozBatchId = this.mozMsgBatch,
                fcmMessageId = fcmMessageId
            )
        } catch (e: Exception) {
            // FirebaseMessagingException & IllegalArgumentException
            val errorMessage = e.message ?: "FirebaseMessagingException"
            logger().error("[worker][notifyFcm][error]$errorMessage")
            return@map Error(
                mozMessageId = this.mozMessageId,
                mozClientId = mozClientId,
                mozBatchId = this.mozMsgBatch,
                error = errorMessage
            )
        } finally {
            logger().info("[worker][notifyFcm]--end--")
        }
    }
}