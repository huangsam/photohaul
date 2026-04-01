plugins {
    id("java")
    id("checkstyle")
    id("application")
    id("jacoco")
    alias(libs.plugins.task.tree)
}

group = "io.huangsam"
version = "1.5.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    implementation(libs.bundles.aws.all)
    implementation(libs.bundles.google.all)
    implementation(libs.dropbox.sdk)
    implementation(libs.logback.classic)
    implementation(libs.metadata.extractor)
    implementation(libs.slf4j.api)
    implementation(libs.sshj)
    testCompileOnly(libs.jetbrains.annotations)
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
    // -Xmx1g: Increase max heap size to 1GB for memory-intensive operations
    // -XX:+UseG1GC: Use G1 garbage collector for better performance with large heaps
    applicationDefaultJvmArgs = listOf("-Xmx1g", "-XX:+UseG1GC")
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
