plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm")
}

group = "dev.ocean.valblock"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")

    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    
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
        archiveBaseName.set("ValBlock")
        archiveClassifier.set("")
        
        relocate("org.apache.commons.lang3", "dev.ocean.valblock.libs.commons")
        relocate("com.google.gson", "dev.ocean.valblock.libs.gson")
        relocate("com.comphenix.protocol", "dev.ocean.valblock.libs.protocol")
    }
    
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        
        val props = mapOf(
            "version" to project.version,
            "name" to project.name,
            "main" to "${project.group}.Plugin"
        )
        
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}