application {
    mainClass.set("com.vgdm.ApplicationKt")
}

plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "com.vgdm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.2.3")
    implementation("io.ktor:ktor-server-host-common-jvm:2.2.3")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.2.3")
    testImplementation(kotlin("test"))

    implementation("io.ktor:ktor-server-core:2.2.3")
    implementation("io.ktor:ktor-server-netty:2.2.3")

    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("org.slf4j:slf4j-api:2.0.3")

    implementation("io.ktor:ktor-server-status-pages:2.1.2")

    implementation("com.typesafe:config:1.4.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}