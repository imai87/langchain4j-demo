plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "br.com.demo"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// Módulos "core" do LangChain4j são versionados como 1.x.y;
// starters Spring e módulos mais novos seguem 1.x.y-betaNN.
val langchain4jVersion = "1.22.0"
val langchain4jBetaVersion = "1.22.0-beta32"

dependencies {
    // Web (Spring Boot 4: starter-webmvc substitui o antigo starter-web)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // LangChain4j + Spring Boot 4
    // 1) Integração com o provedor (API compatível com OpenAI: OpenAI, Azure via gateway, Ollama, vLLM...)
    implementation("dev.langchain4j:langchain4j-open-ai-spring-boot4-starter:$langchain4jBetaVersion")
    // 1b) Integração com a Anthropic (Claude) — protocolo próprio, não compatível com OpenAI
    // Exclui o cliente HTTP "jdk" transitivo: já temos o cliente Spring (via starter open-ai)
    // e o LangChain4j não aceita dois clientes HTTP concorrentes no classpath.
    implementation("dev.langchain4j:langchain4j-anthropic-spring-boot4-starter:$langchain4jBetaVersion") {
        exclude(group = "dev.langchain4j", module = "langchain4j-http-client-jdk")
    }
    // 2) AI Services declarativos (@AiService), memória, tools, RAG
    implementation("dev.langchain4j:langchain4j-spring-boot4-starter:$langchain4jBetaVersion")
    // 3) Modelo de embeddings que roda dentro da JVM (ONNX) — RAG sem depender de API externa
    implementation("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:$langchain4jBetaVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    // Mantém os nomes dos parâmetros em runtime (útil para @Tool / @P)
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
