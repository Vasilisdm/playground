package com.vgdm

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.vgdm.Application")

val config = ConfigFactory
    .parseResources("app.conf")
    .resolve()

fun main() {
    logger.debug("Starting application...")

    embeddedServer(Netty, port = config.getInt("httpPort"), module = Application::module).start(wait = true)
}

fun Application.createKtorApplication() {
    routing {
        get("/") {
            call.respondText("Hello, world!")
        }
    }
}

fun Application.module() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    createKtorApplication()
}