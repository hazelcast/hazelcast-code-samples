
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Import task types

var kotlinVersion = "2.1.21"
plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    application
}

group = "org.hazelcast"
version = "1.0"

val javaVersion = JavaVersion.VERSION_21
val hazelcastVersion = "5.5.0"

application {
    mainClass.set("org.hazelcast.jetpayments.MainKt")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
    maven {
        url = uri("https://repository.hazelcast.com/release/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    testImplementation(kotlin("test"))

    /*
     * Core kotlin libs. We'll need these on all Hazelcast members when using Jet.
     */
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    /*
     * Hazelcast.
     */
    implementation("com.hazelcast:hazelcast:$hazelcastVersion")
    implementation("com.hazelcast.jet:hazelcast-jet-kafka:$hazelcastVersion")

    /*
     * Kafka
     */
    implementation("com.google.cloud.hosted.kafka:managed-kafka-auth-login-handler:1.0.5")
    implementation("io.confluent:kafka-schema-registry-client:7.6.1")
    implementation("org.slf4j:slf4j-nop:2.0.16") // Quiets an annoying message SLF4J: Failed to load class org.slf4j.impl.StaticLoggerBinder
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:+DebugNonSafepoints",
        "-XX:-Inline",
        "-XX:-OptimizeStringConcat",
        "-Dkotlin.compiler.execution.strategy=in-process"
    )
}

tasks.withType<Test> {
    dependsOn("compileTestKotlin")
}

tasks.withType<KotlinCompile>().configureEach {
    // Check if it's the test compilation task
    if (name.contains("test", ignoreCase = true)) {
        // Use the new `compilerOptions` block for debugging flags
        compilerOptions.apply {
            freeCompilerArgs.addAll(
                listOf(
                    "-Xdebug", // Disable compiler optimizations
                )
            )
        }
    } else {
        compilerOptions.apply {
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xno-param-assertions",
                "-Xno-call-assertions",
                "-Xno-receiver-assertions"
            )
            allWarningsAsErrors.set(true)
            javaParameters.set(true)
            verbose.set(true)
            progressiveMode.set(true)
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
    }
}
