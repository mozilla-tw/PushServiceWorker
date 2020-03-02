package org.mozilla.app.user.util

import java.lang.RuntimeException

class FirestoreException : RuntimeException {

    constructor(msg: String? = null, cause: Throwable?) : super(msg, cause)
    constructor(msg: String) : super(msg)
}
