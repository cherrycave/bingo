plugins {
    java
    kotlin("jvm") version "2.3.21"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "dev.boecker.cherrycave"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")

    implementation("xyz.xenondevs.invui:invui:2.1.1")
    implementation("xyz.xenondevs.invui:invui-kotlin:2.1.1")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    runServer {
        minecraftVersion("26.1.2")
    }
}