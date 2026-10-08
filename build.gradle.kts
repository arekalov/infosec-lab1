import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort

plugins {
	kotlin("jvm") version "2.4.21"
	kotlin("plugin.spring") version "2.4.21"
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.4.21"
	id("com.github.spotbugs") version "6.5.12"
	// в 13.0.0 без ключа NVD уходит пустой apiKey, и NVD отклоняет запросы
	id("org.owasp.dependencycheck") version "12.2.2"
}

group = "ru.itmo.infosec"
version = "0.0.1-SNAPSHOT"
description = "Secure REST API lab 1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

// закрывают уязвимости версий из BOM Spring Boot
extra["tomcat.version"] = "11.0.26"
extra["jackson-bom.version"] = "3.1.7"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.owasp.encoder:encoder:1.4.0")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	runtimeOnly("com.h2database:h2")
	spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

configurations.named("runtimeClasspath") {
	resolutionStrategy.activateDependencyLocking()
}

spotbugs {
	toolVersion = "4.10.4"
	effort = Effort.MAX
	reportLevel = Confidence.LOW
	excludeFilter = file("config/spotbugs-exclude.xml")
}

tasks.spotbugsMain {
	reports.create("html") { required = true }
	reports.create("text") { required = true }
}

tasks.spotbugsTest {
	enabled = false
}

dependencyCheck {
	scanConfigurations = listOf("runtimeClasspath")
	// Spring Boot добавляет в цепочку runtimeClasspath конфигурацию testAndDevelopmentOnly,
	// и без этого флага плагин считает её тестовой и ничего не сканирует
	skipTestGroups = false
	failBuildOnCVSS = 7.0f
	formats = listOf("HTML", "JSON")
	outputDirectory = layout.buildDirectory.dir("reports/dependency-check")
	nvd {
		datafeedUrl = "https://nvd.nist.gov/feeds/json/cve/2.0/nvdcve-2.0-{0}.json.gz"
		providers.environmentVariable("NVD_API_KEY").orNull
			?.takeIf { it.isNotBlank() }
			?.let { apiKey = it }
	}
	analyzers {
		assemblyEnabled = false
		msbuildEnabled = false
		nuspecEnabled = false
		ossIndex { enabled = false }
		retirejs { enabled = false }
		nodeAudit { enabled = false }
		nodePackage { enabled = false }
	}
}
