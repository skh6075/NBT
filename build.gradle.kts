import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.3.0"
    `maven-publish`
}

group = "org.kotrock.nbt"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://nexus.hforest.org/repository/maven-public/")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.0")
}

kotlin {
    jvmToolchain(24)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")

    relocate("kotlinx.io", "org.kotrock.nbt.shaded.kotlinx.io")

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["shadow"])
        }
    }
    repositories {
        maven {
            url = uri("https://nexus.hforest.org/repository/maven-snapshots/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}