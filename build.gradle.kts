import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("org.jmailen.kotlinter") version "3.6.0"
    id("org.owasp.dependencycheck") version "6.3.2"
    id("com.github.johnrengelman.shadow") version "7.1.0"
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
    implementation("io.ktor:ktor-server-core:1.6.4")
    implementation("io.ktor:ktor-metrics:1.6.4")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.4")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.4")
    implementation("io.ktor:ktor-serialization:1.6.4")
    implementation("io.ktor:ktor-server-netty:1.6.4") {
        exclude("org.eclipse.jetty.alpn", "alpn-api") // HTTP/2 is not needed
    }
    implementation("ch.qos.logback:logback-classic:1.2.6")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")

    testImplementation("io.ktor:ktor-server-tests:1.6.4")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.5.31")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("io.github.daviddenton:databob.kotlin:1.9.0") {
        exclude("org.funktionale", "funktionale-all") // excluded because dep was jcenter only, spring is not open to public
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
    indentSize = 4
    experimentalRules = false
    disabledRules = arrayOf("no-wildcard-imports")
}

dependencyCheck {
    suppressionFile = ".owaspignore.xml"
    cveValidForHours = 24
    failBuildOnCVSS = 7f // Medium and up
}
