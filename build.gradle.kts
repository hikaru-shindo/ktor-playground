import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
    kotlin("plugin.serialization") version "2.1.10"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    id("org.owasp.dependencycheck") version "12.1.0"
    id("jacoco")
}

group = "com.example"
version = System.getProperty("version") ?: "0.0.1-SNAPSHOT"

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation:3.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
    implementation("io.ktor:ktor-server-metrics:3.1.1")
    implementation("io.ktor:ktor-server-call-id:3.1.1")
    implementation("io.ktor:ktor-server-forwarded-header:3.1.1")
    implementation("io.ktor:ktor-server-status-pages:3.1.1")
    implementation("io.ktor:ktor-server-call-logging:3.1.1")
    implementation("io.ktor:ktor-server-cors:3.1.1")
    implementation("io.ktor:ktor-server-metrics-micrometer:3.1.1")
    implementation("io.micrometer:micrometer-registry-prometheus:1.14.4")
    implementation("io.ktor:ktor-server-netty:3.1.1") {
        exclude("org.eclipse.jetty.alpn", "alpn-api") // HTTP/2 is not needed
    }
    implementation("ch.qos.logback:logback-classic:1.5.16")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:8.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")

    testImplementation("io.ktor:ktor-server-test-host:3.1.1")
    testImplementation("io.ktor:ktor-client-content-negotiation:3.1.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.10")
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
    testImplementation("dev.forkhandles:fabrikate4k:2.20.0.0")
}

ktor {
    fatJar {
        archiveFileName.set("ktor-playground-$version.jar")
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

    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

dependencyCheck {
    suppressionFile = "$projectDir/.owaspignore.xml"
    autoUpdate = true
    failOnError = true
    nvd.validForHours = 24
    failBuildOnCVSS = 7f // Medium and up
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
