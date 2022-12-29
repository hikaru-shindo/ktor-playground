import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.owasp.dependencycheck.reporting.ReportGenerator.Format

plugins {
    application
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("io.ktor.plugin") version "2.2.1"
    id("org.jmailen.kotlinter") version "3.13.0"
    id("org.owasp.dependencycheck") version "7.4.3"
    id("jacoco")
}

group = "com.example"
version = System.getProperty("version") ?: "0.0.1-SNAPSHOT"
application {
    mainClass.set("com.example.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("ktor-playground-${version}.jar")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation:2.2.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.1")
    implementation("io.ktor:ktor-server-metrics:2.2.1")
    implementation("io.ktor:ktor-server-call-id:2.2.1")
    implementation("io.ktor:ktor-server-forwarded-header:2.2.1")
    implementation("io.ktor:ktor-server-status-pages:2.2.1")
    implementation("io.ktor:ktor-server-call-logging:2.2.1")
    implementation("io.ktor:ktor-server-cors:2.2.1")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.2.1")
    implementation("io.micrometer:micrometer-registry-prometheus:1.10.2")
    implementation("io.ktor:ktor-server-netty:2.2.1") {
        exclude("org.eclipse.jetty.alpn", "alpn-api") // HTTP/2 is not needed
    }
    implementation("ch.qos.logback:logback-classic:1.4.5")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.2")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    testImplementation("io.ktor:ktor-server-test-host:2.2.1")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.2.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.8.0")
    testImplementation("io.mockk:mockk:1.13.3")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("dev.forkhandles:fabrikate4k:2.3.0.0")

    dependencyCheck {
        // contains vulnerable dependencies which should be ignored for good reasons, like false positives
        suppressionFile = "$projectDir/.owaspignore.xml"
        autoUpdate = true
        failOnError = true
        cveValidForHours = 8
        failBuildOnCVSS = 0f
        format = Format.ALL
    }
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
    toolVersion = "0.8.8"
}

kotlinter {
    ignoreFailures = false
    reporters = arrayOf("checkstyle", "plain")
    experimentalRules = false
    disabledRules = arrayOf("no-wildcard-imports")
}

dependencyCheck {
    suppressionFile = ".owaspignore.xml"
    cveValidForHours = 24
    failBuildOnCVSS = 7f // Medium and up
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}
