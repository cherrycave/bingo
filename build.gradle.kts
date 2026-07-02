plugins {
    java
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.4.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "dev.boecker.cherrycave"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://maven.boecker.dev/releases")
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")

    compileOnly("dev.boecker.cherrycave:slpf:1.0.0")

    implementation("xyz.xenondevs.invui:invui:2.1.1")
    implementation("xyz.xenondevs.invui:invui-kotlin:2.1.1")

    implementation(platform("io.ktor:ktor-bom:3.5.1"))
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-client-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    implementation("net.megavex:scoreboard-library-api:2.8.0")
    runtimeOnly("net.megavex:scoreboard-library-implementation:2.8.0")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    runServer {
        minecraftVersion("26.1.2")
    }

}