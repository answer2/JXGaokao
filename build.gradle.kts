plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
}

group = "dev.answer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.17.2")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    implementation("com.squareup.okhttp3:okhttp:5.1.0")

    implementation("io.github.evanrupert:excelkt:1.0.2")
    // 支持旧格式 .xls
    implementation("org.apache.poi:poi:5.2.3")
    // 支持新格式 .xlsx
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    implementation("org.apache.logging.log4j:log4j-api:2.18.0")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    implementation("org.json:json:20240303")
    implementation(fileTree("libs") { include("*.jar") })

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}