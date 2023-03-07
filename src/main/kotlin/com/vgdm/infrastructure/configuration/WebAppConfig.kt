package com.vgdm.infrastructure.configuration

data class WebAppConfig(
    val httpPort: Int,
    val dbUser: String,
    val dbPassword: String,
    val dbUrl: String
)