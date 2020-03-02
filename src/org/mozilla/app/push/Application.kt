package org.mozilla.app.push

import com.google.firebase.FirebaseApp
import io.ktor.application.Application
import io.ktor.application.ApplicationStopping
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.routing.routing
import org.mozilla.app.push.controller.pushSubscription
import org.mozilla.app.push.db.initDB
import org.mozilla.app.push.util.logger

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    fun initFirebase() {
        logger().info("[worker][initFirebase]")
        FirebaseApp.initializeApp()
    }

    fun registerCleanUp(pool: com.zaxxer.hikari.HikariDataSource) {
        environment.monitor.subscribe(ApplicationStopping) {
            logger().info("[worker][monitor][stopping]")
            pool.close()
        }
    }

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    initFirebase()

    initDB().also { pool ->

        registerCleanUp(pool)

        routing {
            this.pushSubscription(pool)
        }
    }
}