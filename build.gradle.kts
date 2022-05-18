import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val mockk_version: String by project
val githubUsername: String by project
val githubPassword: String by project

plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("org.jmailen.kotlinter") version "3.10.0"
    id("maven-publish")
}

group = "no.nav.helsearbeidsgiver"
version = "0.1.2"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}



tasks {
    test {
        useJUnitPlatform()
    }
}

repositories {
    mavenCentral()
    maven {
        credentials {
            username = githubUsername
            password = githubPassword
        }
        setUrl("https://maven.pkg.github.com/navikt/helsearbeidsgiver-tokenprovider")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-json:$ktor_version")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    implementation("com.nimbusds:nimbus-jose-jwt:9.22")
    implementation("no.nav.security:token-client-core:2.0.15")
    implementation("no.nav.helsearbeidsgiver:helsearbeidsgiver-tokenprovider:0.1.2")
    testImplementation("io.mockk:mockk:$mockk_version")
}
