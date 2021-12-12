plugins {
    java
}

group = "eu.beegames"
version = "1.3.2"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
    /*maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }*/
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("fr.xephi:authme:5.4.0")
    //compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.processResources {
    doFirst {
        expand(Pair("version", version))
    }
}
