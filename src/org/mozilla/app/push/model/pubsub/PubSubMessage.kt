package org.mozilla.app.push.model.pubsub

import com.google.gson.annotations.SerializedName

data class PubSubMessage(

    @SerializedName("data") val data: String,
    @SerializedName("messageId") val messageId: String,
    @SerializedName("message_id") val message_id: String,
    @SerializedName("publishTime") val publishTime: String,
    @SerializedName("publish_time") val publish_time: String
)