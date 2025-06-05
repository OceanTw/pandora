plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta15"
    kotlin("jvm")
}

group = "dev.ocean.pandora"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")

    maven("https://repo.codemc.io/repository/maven-releases/")

    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.8.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    
    // Additional utilities
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(kotlin("stdlib-jdk8"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    
    shadowJar {
        archiveBaseName.set("Pandora")
        archiveClassifier.set("")
        
        relocate("org.apache.commons.lang3", "dev.ocean.pandora.libs.commons")
        relocate("com.google.gson", "dev.ocean.pandora.libs.gson")
    }
    
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        
        val props = mapOf(
            "version" to project.version,
            "name" to project.name,
            "main" to "${project.group}.Plugin"
        )
        
        inputs.properties(props)
    }
}