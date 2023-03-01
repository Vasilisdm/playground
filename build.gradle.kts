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
    testImplementation(kotlin("test"))

    implementation("io.ktor:ktor-server-core:2.2.3")
    implementation("io.ktor:ktor-server-netty:2.2.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}