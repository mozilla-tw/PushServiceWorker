package org.mozilla.app.push.util

import java.sql.Connection
import java.sql.SQLException

fun <T> Connection.transaction(logging: String = "SQLTransaction", action: () -> T): T? {
    return try {
        this.use {

            this.autoCommit = false

            val result = action()

            this.commit()

            this.autoCommit = true

            return@use result
        }
    } catch (e: SQLException) {
        e.printStackTrace()
        logger().error("[worker][$logging][error]$e")
        return null
    }
}