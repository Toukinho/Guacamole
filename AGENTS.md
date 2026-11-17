# Repository Guidelines

## Project Structure & Module Organization

The active application is a Java 25 Spring Boot backend in `backend/Spring/`, built with Gradle. Production code lives in `src/main/java/com/guacamole/backend/`; keep new components beneath that package so Spring can discover them. Configuration belongs in `src/main/resources/application.properties`. Tests mirror the production package structure under `src/test/java/`.

The backend includes Spring MVC, Bean Validation, Spring Data JPA, and the MySQL runtime driver. `frontend/` currently contains only a placeholder file; it has no application or build tooling yet. `backend/text` is also a placeholder.

## Build, Test, and Development Commands

Run these commands from `backend/Spring/` with JDK 25 installed. Use the committed Gradle wrapper rather than a system Gradle installation:

- `.\gradlew.bat bootRun` starts the backend for local development.
- `.\gradlew.bat build` compiles, runs tests, and packages the application.
- `.\gradlew.bat test` runs the test suite using JUnit Platform.
- `.\gradlew.bat bootJar` creates the executable application JAR under `build/libs/`.

On Linux or macOS, replace `.\gradlew.bat` with `./gradlew`. Database configuration is not yet supplied; JPA startup and context tests may require a reachable database and datasource settings.

## Coding Style & Naming Conventions

Follow the existing Java and Gradle files: tab indentation, opening braces on the declaration line, and semicolons in Java. Use `UpperCamelCase` for classes, `lowerCamelCase` for methods and fields, and lowercase package names. Organize new code by cohesive responsibility within `com.guacamole.backend`. No formatter or lint tool is currently configured; keep formatting consistent with neighboring code.

## Testing Guidelines

Tests use JUnit Jupiter and Spring Boot test support. Name test classes `*Tests`, following `BackendApplicationTests`, and use descriptive method names. The existing `contextLoads()` test checks Spring application context initialization. Add focused tests for new behavior and integration tests when persistence or application wiring changes. No coverage threshold is configured.

## Commit & Pull Request Guidelines

The short Git history mixes plain summaries with a `feat:` prefix. Prefer concise, imperative summaries and use prefixes such as `feat:` or `fix:` where appropriate. Pull requests should describe the change, link relevant issues, list validation commands and results, and identify configuration or database changes. Include screenshots for future UI changes.

## Security & Configuration

Keep database passwords and other secrets out of committed files. Supply datasource settings through local configuration or environment variables such as `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.


Atue como um engenheiro de software sênior especializado em Java, Spring Boot, Gradle, MySQL, Spring Data JPA, arquitetura REST e integração de APIs externas.

Estou desenvolvendo o backend do projeto Guacamole.

**Contexto do projeto:**
- Repositório: Guacamole
- Branch de desenvolvimento: `feature/backend-spring`
- Diretório do backend: `backend/Spring`
- Linguagem: Java
- Framework: Spring Boot
- Gerenciador de dependências: Gradle (Groovy)
- Banco de dados planejado: MySQL

**Sua tarefa:**

Crie a estrutura `.agents/skills/` na raiz do repositório e implemente sete Skills customizadas, cada uma contendo seu próprio arquivo `SKILL.md`, com frontmatter `name` e `description`, objetivos, procedimentos, critérios de validação e instruções de uso.

### Skill 1 — relational-model-interpreter

Responsável por interpretar documentos de requisitos, especialmente `api_cursos.md`, quando disponível.

Deve:
- Identificar entidades e seus atributos.
- Identificar métodos e responsabilidades.
- Determinar chaves primárias e estrangeiras.
- Identificar cardinalidades 1:1, 1:N e N:N.
- Identificar tabelas associativas e chaves compostas.
- Gerar uma proposta de modelo relacional.
- Diferenciar requisitos explícitos de sugestões de implementação.
- Não inventar requisitos ausentes.

### Skill 2 — spring-architecture-designer

Responsável por planejar a arquitetura do backend.

Deve:
- Organizar pacotes de Entity, Repository, Service, Controller/Resource, DTO, Config e Exception.
- Definir responsabilidades de cada camada.
- Planejar endpoints REST.
- Propor DTOs para entrada e saída.
- Planejar validações e tratamento de exceções.
- Respeitar a arquitetura existente.
- Evitar dependências desnecessárias.

### Skill 3 — external-api-integrator

Responsável por implementar integrações com APIs externas.

Prioridade: YouTube Data API v3.

Deve:
- Analisar a documentação oficial da API.
- Implementar clientes HTTP com RestClient ou WebClient, conforme a necessidade.
- Importar playlists como cursos e vídeos como aulas, quando previsto nos requisitos.
- Preservar identificadores externos e ordem das aulas.
- Evitar registros duplicados.
- Tratar erros HTTP, limites de requisições, timeouts e indisponibilidade.
- Manter tokens e chaves de API fora do repositório.
- Criar testes com respostas simuladas.
- Não executar importações externas sem autorização.

### Skill 4 — database-jpa

Responsável pela persistência e modelagem JPA.

Deve:
- Implementar entidades e relacionamentos.
- Utilizar corretamente @OneToMany, @ManyToOne, @ManyToMany e @OneToOne quando necessário.
- Implementar @EmbeddedId e @MapsId quando apropriado.
- Definir chaves primárias, estrangeiras e restrições de unicidade.
- Prevenir ciclos de serialização JSON.
- Analisar problemas de N+1 queries e cascades.
- Propor migrations com Flyway.
- Evitar alterações destrutivas no banco sem autorização.

### Skill 5 — spring-test-engineer

Responsável por garantir a qualidade do backend.

Deve:
- Criar testes unitários com JUnit 5 e Mockito.
- Criar testes de integração quando apropriado.
- Testar Services, Controllers e Repositories.
- Validar respostas HTTP.
- Testar DTOs, validações e exceções.
- Simular integrações externas.
- Executar testes pelo Gradle.
- Relatar testes aprovados, falhas e testes não executados.

### Skill 6 — spring-code-reviewer

Responsável pela revisão independente do código.

Deve:
- Identificar bugs e possíveis regressões.
- Avaliar segurança e exposição de dados.
- Verificar boas práticas REST.
- Analisar relacionamentos JPA.
- Identificar código duplicado e complexidade desnecessária.
- Avaliar tratamento de exceções.
- Classificar problemas por gravidade.
- Sugerir correções sem alterar arquivos automaticamente.

### Skill 7 — java-learning-mentor

Responsável por ensinar o funcionamento das implementações.

Deve:
- Explicar alterações em português brasileiro.
- Utilizar linguagem didática para iniciantes.
- Explicar anotações, classes, métodos e relacionamentos.
- Mostrar exemplos simples.
- Comparar o funcionamento anterior e posterior.
- Explicar por que cada alteração foi necessária.
- Gerar um resumo de aprendizado ao final de cada implementação.

## Regras gerais

1. Antes de alterar arquivos, analise a estrutura atual do projeto.
2. Não modifique a pasta `frontend` sem autorização.
3. Não altere a branch `main`.
4. Não execute comandos destrutivos.
5. Não faça commits, pushes ou Pull Requests sem autorização explícita.
6. Não armazene senhas, tokens ou credenciais no código.
7. Preserve os arquivos e funcionalidades existentes.
8. Utilize os requisitos documentados como fonte principal de decisões.
9. Antes de implementar funcionalidades, apresente um plano.
10. Após alterações, explique os arquivos criados, modificados e os testes executados.

**Primeira execução:**

Implemente somente os arquivos de configuração das Skills e um `AGENTS.md` na raiz, com instruções gerais para o Codex.

Não implemente entidades, endpoints, banco de dados ou integrações ainda.

Ao finalizar, apresente a estrutura criada e explique como utilizar cada Skill.