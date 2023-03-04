package com.vgdm

import com.typesafe.config.ConfigFactory
import com.vgdm.infrastructure.WebAppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import kotlin.reflect.full.declaredMemberProperties

private val logger = LoggerFactory.getLogger("com.vgdm.Application")

fun main() {
    logger.debug("Starting application...")

    val environment = System.getenv("KAPP_ENV") ?: "local"
    val config = appConfiguration(environment)

    val secretsRegex = "password|secret|key"
        .toRegex(RegexOption.IGNORE_CASE)

    val configRepresentation = WebAppConfig::class.declaredMemberProperties
        .sortedBy { it.name }
        .map {
            if (secretsRegex.containsMatchIn(it.name)) {
                "${it.name} = ${it.get(config).toString().take(2)}*****"
            } else {
                "${it.name} = ${it.get(config)}"
            } }
        .joinToString(separator = "\n")

    logger.debug("Current configuration: $configRepresentation")

    embeddedServer(Netty, port = config.httpPort, module = Application::module).start(wait = true)
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

private fun appConfiguration(environment: String): WebAppConfig =
    ConfigFactory
        .parseResources("app-${environment}.conf")
        .withFallback(ConfigFactory.parseResources("app.conf"))
        .resolve()
        .let {
            WebAppConfig(
                httpPort = it.getInt("httpPort"),
                dbPassword = it.getInt("dbPassword")
            )
        }