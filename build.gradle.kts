import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.owasp.dependencycheck.reporting.ReportGenerator.Format

plugins {
    application
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("org.jmailen.kotlinter") version "3.10.0"
    id("org.owasp.dependencycheck") version "7.1.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("jacoco")
}

group = "com.example"
version = System.getProperty("version") ?: "0.0.1-SNAPSHOT"
application {
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation:2.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.3")
    implementation("io.ktor:ktor-server-metrics:2.0.3")
    implementation("io.ktor:ktor-server-call-id:2.0.3")
    implementation("io.ktor:ktor-server-forwarded-header:2.0.3")
    implementation("io.ktor:ktor-server-status-pages:2.0.3")
    implementation("io.ktor:ktor-server-call-logging:2.0.3")
    implementation("io.ktor:ktor-server-cors:2.0.3")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.0.3")
    implementation("io.micrometer:micrometer-registry-prometheus:1.9.2")
    implementation("io.ktor:ktor-server-netty:2.0.3") {
        exclude("org.eclipse.jetty.alpn", "alpn-api") // HTTP/2 is not needed
    }
    implementation("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.2")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.3")

    testImplementation("io.ktor:ktor-server-test-host:2.0.3")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.0.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.7.10")
    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("dev.forkhandles:fabrikate4k:2.2.0.0")

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
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to application.mainClass))
        }
    }

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
