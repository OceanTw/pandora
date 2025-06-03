plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.ocean.valblock"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    
    // Additional utilities
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.google.code.gson:gson:2.10.1")
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