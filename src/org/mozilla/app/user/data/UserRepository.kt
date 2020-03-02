package org.mozilla.app.user.data

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.mozilla.app.user.util.getResultsUnchecked

open class UserRepository {

    companion object {
        private const val COLLECTION_USER_TOKEN = "user_token"
        var firestore: Firestore = FirestoreClient.getFirestore()
        private var userToken = firestore.collection(COLLECTION_USER_TOKEN)

        fun getFcmToken(telemetryClientId: String): String? {
            val resultsUnchecked =
                userToken.whereEqualTo("telemetry_client_id", telemetryClientId).getResultsUnchecked()
            if (resultsUnchecked.isNotEmpty()) {
                return resultsUnchecked[0].getString("fcm_token")
            }
            return null
        }
    }
}
