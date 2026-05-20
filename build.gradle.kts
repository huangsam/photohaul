plugins {
    id("java")
    id("checkstyle")
    id("application")
    id("jacoco")
    alias(libs.plugins.task.tree)
}

group = "io.huangsam"
version = "1.6.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jspecify)
    implementation(libs.bundles.aws.all)
    implementation(libs.bundles.google.all)
    implementation(libs.dropbox.sdk)
    implementation(libs.logback.classic)
    implementation(libs.metadata.extractor)
    implementation(libs.slf4j.api)
    implementation(libs.sshj)
    testImplementation(libs.jspecify)
    testImplementation(libs.bundles.mockito.all)
    testImplementation(libs.junit.jupiter)
    testImplementation(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.launcher)
}

// Enable deprecation warnings to catch usage of deprecated APIs during development
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

application {
    mainClass.set("io.huangsam.photohaul.Main")
    // JVM performance tuning for photo processing workloads
    // -Xms2g: Increase min heap size to 2GB for memory-intensive operations
    // -Xmx4g: Increase max heap size to 4GB for memory-intensive operations
    // -XX:+UseZGC: Use ZGC for sub-millisecond pause times when loading large image buffers
    // -XX:+ZGenerational: Enable generational ZGC for improved throughput (stable since Java 21)
    applicationDefaultJvmArgs = listOf("-Xms2g", "-Xmx4g", "-XX:+UseZGC", "-XX:+ZGenerational")
}

// Forward only the JVM system property -Dphotohaul.config to the app's JVM when provided.
// Default behavior (no -D) relies on Settings to use "config.properties".
tasks.named<JavaExec>("run") {
    val override = System.getProperty("photohaul.config")
    if (!override.isNullOrBlank()) {
        systemProperty("photohaul.config", override)
        logger.lifecycle("[run] photohaul.config = $override")
    } else {
        logger.lifecycle("[run] photohaul.config not provided; using default config.properties")
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestReport {
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
    // Exclude Main.class from coverage reports since it only contains the main method
    // and is not directly unit tested
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            exclude("io/huangsam/**/Main.class")
        }
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    // Set a minimum coverage threshold of 70% to ensure good test coverage
    violationRules {
        rule {
            limit {
                minimum = "0.7".toBigDecimal()
            }
        }
    }
}
