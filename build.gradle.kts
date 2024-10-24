import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    application
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("org.owasp.dependencycheck") version "8.4.3"
    id("jacoco")
}

group = "com.example"
version = System.getProperty("version") ?: "0.0.1-SNAPSHOT"
application {
    mainClass.set("com.example.ApplicationKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

ktor {
    fatJar {
        archiveFileName.set("ktor-playground-$version.jar")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-server-metrics:2.3.12")
    implementation("io.ktor:ktor-server-call-id:2.3.12")
    implementation("io.ktor:ktor-server-forwarded-header:2.3.12")
    implementation("io.ktor:ktor-server-status-pages:2.3.12")
    implementation("io.ktor:ktor-server-call-logging:2.3.12")
    implementation("io.ktor:ktor-server-cors:2.3.12")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.3.12")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.6")
    implementation("io.ktor:ktor-server-netty:2.3.12") {
        exclude("org.eclipse.jetty.alpn", "alpn-api") // HTTP/2 is not needed
    }
    implementation("ch.qos.logback:logback-classic:1.5.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:8.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    testImplementation("io.ktor:ktor-server-test-host:2.3.12")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.12")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.21")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
    testImplementation("dev.forkhandles:fabrikate4k:2.20.0.0")
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)

        reports {
            html.required.set(true)
            csv.required.set(false)
            xml.required.set(true)
        }
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.6".toBigDecimal()
                }
            }
        }
    }
}

jacoco {
    // fix an issue with new JRE versions
    // see: https://github.com/gradle/gradle/issues/15038
    toolVersion = "0.8.12"
}

ktlint {
    verbose.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.JSON)
        reporter(ReporterType.HTML)
        reporter(ReporterType.PLAIN)
    }
}

dependencyCheck {
    suppressionFile = "$projectDir/.owaspignore.xml"
    autoUpdate = true
    failOnError = true
    cveValidForHours = 24
    failBuildOnCVSS = 7f // Medium and up
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}
