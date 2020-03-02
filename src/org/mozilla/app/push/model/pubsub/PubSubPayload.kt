package org.mozilla.app.push.model.pubsub

import com.google.gson.annotations.SerializedName

data class PubSubPayload(

    @SerializedName("message") val pubSubMessage: PubSubMessage,
    @SerializedName("subscription") val subscription: String
)