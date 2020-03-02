package org.mozilla.app.user.util

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.UncheckedExecutionException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.concurrent.CancellationException
import kotlin.math.ceil

const val BATCH_VOLUME = 500

fun DocumentSnapshot.areFieldsPresent(fieldNames: List<String>): Boolean {
    fieldNames.forEach { fieldName ->
        this.get(fieldName) ?: return false
    }
    return true
}

fun DocumentSnapshot.checkAbsentFields(fieldNames: List<String>): List<String> {
    return fieldNames.filter { this.get(it) == null }
}

/** Extensions for read/write from/to Firestore */

fun Query.getResultsUnchecked(): List<QueryDocumentSnapshot> {
    return get().getUnchecked().documents
}

fun DocumentReference.getUnchecked(): DocumentSnapshot {
    return get().getUnchecked()
}

/** Extensions for Collection/Document navigation */

val DocumentReference.parentCollection
    get() = this.parent

val CollectionReference.parentDocument: DocumentReference?
    get() = this.parent

/** Extensions for handling and re-throwing exceptions thrown by Firestore */
fun <T> ApiFuture<T>.getUnchecked(): T {
    return try {
        Futures.getUnchecked(this)
    } catch (e: CancellationException) {
        throw FirestoreException(cause = e)
    } catch (e: UncheckedExecutionException) {
        throw FirestoreException(cause = e)
    }
}

@Throws(DateTimeParseException::class)
fun stringToLocalDateTime(localDateTimeString: String): LocalDateTime {
    return LocalDateTime.parse(localDateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

fun <T> getBatchIteration(list: List<T>): Int {
    return ceil(list.size / BATCH_VOLUME.toFloat()).toInt()
}
