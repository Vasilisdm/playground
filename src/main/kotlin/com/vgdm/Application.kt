package com.vgdm

import com.typesafe.config.ConfigFactory
import com.vgdm.application.httpResponses.JsonWebResponse
import com.vgdm.application.httpResponses.TextWebResponse
import com.vgdm.application.httpResponses.WebResponse
import com.vgdm.infrastructure.configuration.WebAppConfig
import com.vgdm.infrastructure.gsonResponse.KtorJsonWebResponse
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import javax.sql.DataSource
import kotlin.reflect.full.declaredMemberProperties

private val logger = LoggerFactory.getLogger("com.vgdm.Application")

fun main() {
    logger.debug("Starting application...")

    val environment = System.getenv("KAPP_ENV") ?: "local"
    val config = appConfiguration(environment)

    val dataSource = createAndMigrateDataSource(config)
    dataSource.connection.use { connection ->
        connection.createStatement()
            .use { statement ->
                statement.executeQuery("SELECT 1")
            }
    }

    val secretsRegex = "password|secret|key"
        .toRegex(RegexOption.IGNORE_CASE)
    val configRepresentation = WebAppConfig::class.declaredMemberProperties
        .sortedBy { it.name }
        .map {
            if (secretsRegex.containsMatchIn(it.name)) {
                "${it.name} = ${it.get(config).toString().take(2)}*****"
            } else {
                "${it.name} = ${it.get(config)}"
            }
        }
        .joinToString(separator = "\n")
    logger.debug("Configuration loaded successfully: $configRepresentation")

    embeddedServer(Netty, port = config.httpPort, module = Application::module).start(wait = true)
}

fun Application.createKtorApplication() {
    routing {
        get("/", webResponse {
            TextWebResponse("Hello, world!")
        })

        get("/json_test", webResponse {
            JsonWebResponse(mapOf("foo" to "bar"))
        })
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

fun migrateDataSource(dataSource: DataSource) {
    Flyway.configure()
        .dataSource(dataSource)
        .locations("db/migration")
        .table("flyway_schema_history")
        .load()
        .migrate() }

fun createAndMigrateDataSource(config: WebAppConfig) =
    createDataSource(config).also(::migrateDataSource)

fun webResponse(
    handler: suspend PipelineContext<Unit, ApplicationCall>.() -> WebResponse
): PipelineInterceptor<Unit, ApplicationCall> {
    return {
        val webResponseInstance = this.handler()
        for ((name, values) in webResponseInstance.headers) {
            for (value in values) {
                call.response.header(name, value)
            }
        }

        val statusCode = HttpStatusCode.fromValue(webResponseInstance.statusCode)

        when (webResponseInstance) {
            is TextWebResponse -> {
                call.respondText(text = webResponseInstance.body, status = statusCode)
            }

            is JsonWebResponse -> {
                call.respond(
                    KtorJsonWebResponse(
                        body = webResponseInstance.body,
                        status = statusCode
                    )
                )
            }
        }
    }
}

fun createDataSource(config: WebAppConfig): HikariDataSource = HikariDataSource().apply {
    jdbcUrl = config.dbUrl
    username = config.dbUser
    password = config.dbPassword
}

private fun appConfiguration(environment: String): WebAppConfig =
    ConfigFactory
        .parseResources("app-${environment}.conf")
        .withFallback(ConfigFactory.parseResources("app.conf"))
        .resolve()
        .let {
            WebAppConfig(
                httpPort = it.getInt("httpPort"),
                dbUser = it.getString("dbUser"),
                dbPassword = it.getString("dbPassword"),
                dbUrl = it.getString("dbUrl")
            )
        }