package org.mozilla.app.push.controller

import org.mozilla.app.push.model.fcm.Success
import org.mozilla.app.push.model.fcm.WorkerRequestResponse
import org.mozilla.app.push.util.logger
import org.mozilla.app.push.util.transaction
import java.sql.SQLException
import javax.sql.DataSource

class PushLogRepository constructor(private val dataSource: DataSource) {

    companion object {
        private const val TABLE_NAME_WORKER = "worker"
        private const val TABLE_NAME_PUSH = "push"
        private const val SQL_PUSH_SUCCESS = "INSERT INTO $TABLE_NAME_PUSH ( " +
                "moz_msg_id, moz_client_id, moz_msg_batch, fcm_msg_id, update_ts )\n" +
                "VALUES ( '%s', '%s', '%s', '%s', current_timestamp) "

        private const val SQL_PUSH_ERROR = "INSERT INTO $TABLE_NAME_PUSH ( " +
                "moz_msg_id, moz_client_id, moz_msg_batch, error, update_ts )\n" +
                "VALUES ( '%s', '%s', '%s', '%s', current_timestamp) "

        // use for update
        private const val SQL_SELECT_WORKER_BY_ID = "SELECT data FROM $TABLE_NAME_WORKER WHERE " +
                "id = %s AND task = 'push_worker' AND status = 'new' FOR UPDATE"

        private const val SQL_UPDATE_WORKER_BY_ID = "UPDATE $TABLE_NAME_WORKER SET " +
                "status = '%s' , update_ts = current_timestamp WHERE id = %s"
    }

    // throws null if any error occurs
    fun <T> handleWorkId(workId: String, handleWork: (String?) -> T): T? {
        try {
            return handleWorkIdInternal(workId, handleWork)
        } catch (e: SQLException) {
            // this may occurs often. e.g. the work has done
            logger().error("[worker][handleWorkId][error][$workId]${e.message}")
            return null
        }
    }

    private fun <T> handleWorkIdInternal(workId: String, handleWork: (String?) -> T): T? {
        val connection = dataSource.connection
        return connection.transaction("getDataById") {
            val sqlWorkerSelect = String.format(SQL_SELECT_WORKER_BY_ID, workId)
            val resultSet = connection.prepareStatement(sqlWorkerSelect).executeQuery()
            resultSet.next()
            val data = resultSet.getString("data")
            logger().info("[worker][handleWorkId][data]$workId")

            val sqlWorkerWip = String.format(SQL_UPDATE_WORKER_BY_ID, "wip", workId)
            connection.prepareStatement(sqlWorkerWip).executeUpdate()
            logger().info("[worker][handleWorkId][wip]$workId")

            val result = handleWork(data)
            logger().info("[worker][handleWorkId][handleWork]$workId")

            val sqlWorkerDone = String.format(SQL_UPDATE_WORKER_BY_ID, "done", workId)
            connection.prepareStatement(sqlWorkerDone).executeUpdate()
            logger().info("[worker][handleWorkId][done]$workId")

            return@transaction result
        }
    }

    fun log(response: WorkerRequestResponse) {
        return try {

            when (response) {
                is Success -> SQL_PUSH_SUCCESS
                else -> SQL_PUSH_ERROR
            }.let { baseSQL: String ->
                String.format(
                    baseSQL,
                    response.mozMessageId,
                    response.mozClientId,
                    response.mozBatchId,
                    response.result
                )
            }.let { sql: String ->
                dataSource.connection.use {
                    logger().info("[worker][log][mozMessageId]${response.mozClientId}")
                    val executeUpdate = it.prepareStatement(sql).executeUpdate()
                    logger().info("[worker][log][executeUpdate]$executeUpdate")
                }
            }
        } catch (e: SQLException) {

            logger().error("[worker][log][error]$e")
        }
    }
}
