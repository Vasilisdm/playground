package com.vgdm

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 4207, module = Application::module).start(wait = true)
}

fun Application.createKtorApplication() {
    routing {
        get("/") {
            call.respondText("Hello, world!")
        }
    }
}

fun Application.module() {
    createKtorApplication()
}