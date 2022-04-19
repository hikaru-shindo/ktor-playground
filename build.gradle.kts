import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.owasp.dependencycheck.reporting.ReportGenerator.Format

plugins {
    application
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("org.jmailen.kotlinter") version "3.10.0"
    id("org.owasp.dependencycheck") version "7.0.4.1"
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
    implementation("io.ktor:ktor-server-core:1.6.8")
    implementation("io.ktor:ktor-metrics:1.6.8")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.8")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.5")
    implementation("io.ktor:ktor-serialization:1.6.8")
    implementation("io.ktor:ktor-server-netty:1.6.8") {
        exclude("org.eclipse.jetty.alpn", "alpn-api") // HTTP/2 is not needed
    }
    implementation("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.1.1")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.1")

    testImplementation("io.ktor:ktor-server-tests:1.6.8")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.6.21")
    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("dev.forkhandles:fabrikate4k:2.1.1.0")

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
        freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}
