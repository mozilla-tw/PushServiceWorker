package org.mozilla.app.push.util

import java.sql.Connection
import java.sql.SQLException

@Throws(SQLException::class)
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
        logger().error("[worker][transaction][$logging][error]$e")
        throw e
    }
}