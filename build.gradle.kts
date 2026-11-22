plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.team"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        // Javalin usa los módulos ee10 de Jetty, que Spring Boot 4 no gestiona (Boot usa ee11).
        // Se alinean con la versión de Jetty de Spring Boot 4.1.2 para no mezclar versiones de Jetty.
        mavenBom("org.eclipse.jetty.ee10:jetty-ee10-bom:12.1.14")
    }
}

dependencies {
    // spring
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // api rest
    implementation("io.javalin:javalin:7.2.2")

    // database (la versión del driver de MariaDB la gestiona Spring Boot)
    implementation("org.jdbi:jdbi3-core:3.55.0")
    implementation("org.jdbi:jdbi3-sqlobject:3.55.0")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    // reportes PDF (2.0.5 es la última versión de OpenPDF compatible con Java 17; desde la 2.1.0 exige Java 21)
    implementation("com.github.librepdf:openpdf:2.0.5")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // testing
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
