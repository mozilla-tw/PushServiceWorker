package org.mozilla.app.push.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

private val DB_USER = System.getenv("DB_USER")
private val DB_PASS = System.getenv("DB_PASS")
private val DB_NAME = System.getenv("DB_NAME")
private val CLOUD_SQL_CONNECTION_NAME = System.getenv("CLOUD_SQL_CONNECTION_NAME")

fun initDB(): com.zaxxer.hikari.HikariDataSource {

    val config = HikariConfig()
    config.jdbcUrl = java.lang.String.format("jdbc:postgresql:///%s", DB_NAME)
    config.username = DB_USER
    config.password = DB_PASS
    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
    config.addDataSourceProperty("cloudSqlInstance", CLOUD_SQL_CONNECTION_NAME)
    config.maximumPoolSize = 5
    config.minimumIdle = 5
    config.connectionTimeout = 10000; // 10 seconds
    config.idleTimeout = 600000; // 10 minutes
    config.maxLifetime = 1800000; // 30 minutes

    return HikariDataSource(config)
}
