plugins {
    java
    kotlin("jvm") version "2.3.21"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "dev.boecker.cherrycave"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://maven.boecker.dev/releases")
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")

    compileOnly("dev.boecker.cherrycave:slpf:1.0.0")

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