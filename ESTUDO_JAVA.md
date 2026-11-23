# Guia de estudo: Java e Spring no Guacamole

Este material explica o backend para apresentação. Os caminhos Java citados ficam em `backend/Spring/src/main/java/com/guacamole/backend`. O [README](README.md) mostra como executar.

## 1. O que o projeto faz

O usuário cria uma conta, consulta cursos, salva cursos e marca aulas como concluídas. O navegador mostra vídeos do YouTube. O backend responde a requisições HTTP e grava dados no MySQL. Cada usuário enxerga seu próprio progresso.

```text
Navegador React → HTTP/JSON → Resource → Service → Repository → MySQL
                                  ↓          ↓
                                 DTO      Entity / JPA
```

Exemplo: ao clicar em salvar, o React envia `POST /courses/7/watchlist` com um token. `TokenAuthenticationFilter` identifica o usuário. `CourseResource` recebe o pedido; `UserCourseService` valida curso e usuário; `CourseWatchlistRepository` grava o vínculo. A resposta é `204 No Content`.

## 2. Java, Gradle e Spring

Java é a linguagem. Gradle lê `build.gradle`, baixa bibliotecas, compila e executa testes. O Gradle Wrapper (`gradlew` e `gradlew.bat`) fixa a versão do Gradle para o projeto. O JDK 25 compila o código.

`BackendApplication` contém `main`: a JVM inicia por esse método. `SpringApplication.run` inicia o Spring e o servidor HTTP. `@SpringBootApplication` ativa configuração automática e procura componentes no pacote da aplicação. `@EnableScheduling` permite tarefas periódicas, como limpeza de tokens expirados.

Spring cria objetos chamados *beans*. Em vez de cada classe criar seus repositórios manualmente, declara dependências no construtor. O Spring fornece os objetos; isso é **injeção de dependências**. Exemplo: `CourseResource` recebe os serviços no construtor.

## 3. HTTP e JSON

HTTP transporta pedidos e respostas. `GET` consulta, `POST` cria ou executa uma ação e `DELETE` remove um vínculo. O número de resposta ajuda o cliente: `200` sucesso com dados, `201` criado, `204` sucesso sem corpo, `400` entrada inválida, `401` autenticação necessária, `404` recurso inexistente, `409` conflito e `429` excesso de tentativas.

JSON representa dados em texto. Por exemplo, o cadastro envia `{"name":"Ana","email":"ana@exemplo.com","password":"..."}`. O backend valida e devolve um objeto com `token`. O React guarda esse token e envia `Authorization: Bearer <token>` nos pedidos seguintes. O cliente não escolhe o ID do usuário para operações pessoais.

## 4. Modelo de dados e entidades

Uma entidade é uma classe que representa uma tabela. `@Entity` habilita o mapeamento JPA; `@Table` define a tabela; `@Id` identifica a chave primária; `@GeneratedValue` delega a geração do ID ao banco. `@Column` configura uma coluna.

- `User`: ID, nome, email único e **hash** da senha. Relaciona-se a cursos salvos e aulas concluídas.
- `Teacher`: ID e nome do professor ou canal. Pode ter vários cursos.
- `Course`: título, descrição, ID externo da playlist e professor. Tem várias aulas.
- `Lesson`: título, ID do vídeo, posição e curso. O par curso/posição é único; a consulta ordena as aulas por posição.
- `CourseWatchlist`: associa usuário e curso salvo e registra `createdAt`.
- `LessonCompletion`: associa usuário e aula concluída e registra `completedAt`.
- `AuthToken`: guarda hash do token, usuário, criação e expiração; o token puro não é gravado.

`@ManyToOne` significa que vários registros podem apontar para um registro: várias aulas pertencem a um curso. `@JoinColumn` indica a coluna da chave estrangeira, como `course_id`. `@OneToMany(mappedBy = "course")` representa o lado inverso. Para persistir a associação, é necessário definir a referência no lado que possui a chave estrangeira; apenas adicionar na coleção inversa não basta. `LAZY` adia a busca do relacionamento. `@JsonIgnore` evita expor dados ou ciclos ao serializar entidades.

### Chaves compostas

Em `CourseWatchlist`, a chave é o par usuário + curso. Em `LessonCompletion`, é usuário + aula. As classes `CourseWatchlistId` e `LessonCompletionId` implementam `Serializable`, `equals` e `hashCode`, permitindo comparar corretamente os pares. `@IdClass` conecta cada entidade à classe da chave. A chave composta impede duplicar o mesmo vínculo. `@PrePersist` preenche a data antes da primeira gravação.

## 5. Repositórios

Um repositório faz acesso aos dados. As interfaces em `repositories/` estendem `JpaRepository`: Spring Data cria implementações com `save`, `findById`, `existsById` e outras operações.

`CourseRepository` pagina cursos e usa `@EntityGraph` para buscar também o professor e reduzir consultas adicionais. `LessonRepository` busca aulas ordenadas e conta aulas. `CourseWatchlistRepository` e `LessonCompletionRepository` consultam vínculos e contagens; `UserRepository` busca por email e pode bloquear a linha do usuário durante gravações. `TeacherRepository` e `AuthTokenRepository` acessam professores e tokens. Consultas especiais usam `@Query` quando o nome do método não basta.

## 6. Serviços: regras de negócio

`@Service` marca uma classe gerenciada pelo Spring. Serviços separam regras das rotas HTTP:

- `CourseService`: pagina de zero em diante, exige tamanho entre 1 e 50, ordena por título e ID e retorna 404 quando não encontra curso.
- `LessonService`: valida a existência do curso e retorna suas aulas em ordem; procura aula por ID.
- `CourseQueryService`: monta detalhes e respostas com nome do professor, número de aulas, situação pessoal e progresso. Consulta dados dentro de transação para acessar relacionamentos `LAZY`.
- `UserCourseService`: salva/remove curso, marca/desmarca conclusão e calcula contagens. Bloqueia a linha do usuário durante escrita para coordenar operações concorrentes desses serviços. Repetir uma ação não duplica vínculo; retirar curso salvo preserva conclusões.
- `AuthenticationService`: cadastra e autentica, protege contra email repetido, confere senha e emite token. `me` retorna dados públicos do usuário.
- `PasswordService`: usa BCrypt com salt para criar e conferir hash. Também reconhece hashes PBKDF2 antigos e o serviço de autenticação os substitui após login bem-sucedido quando possível. BCrypt limita a senha a 72 bytes.
- `TokenService`: gera token aleatório, grava somente seu hash SHA-256, verifica expiração e revoga no logout. Uma tarefa periódica limpa tokens expirados.
- `LoginRateLimiter`: limita tentativas por IP e por email numa janela local de 15 minutos. Os contadores vivem na memória de cada instância e reiniciam com ela.
- `YoutubeImportService`: consulta playlist e páginas de vídeos, cria professor, curso e aulas e recusa playlist já cadastrada. Detalhes e limites estão abaixo.

`@Transactional` agrupa operações: se uma falha gerar exceção, a transação normalmente é revertida. `readOnly = true` identifica consultas. `ResponseStatusException` transforma erros previstos em respostas HTTP correspondentes. Uma transação não torna automaticamente operações entre múltiplos processos à prova de corrida: o importador deve ser executado uma vez por vez.

## 7. Recursos HTTP

`@RestController` indica que os métodos respondem a pedidos HTTP. `@RequestMapping` define um prefixo; `@GetMapping`, `@PostMapping` e `@DeleteMapping` escolhem método e rota. `@PathVariable` extrai um ID da URL; `@RequestParam` lê parâmetros; `@RequestBody` converte JSON para um objeto. `ResponseEntity` permite definir status e corpo.

| Classe | Rotas e papel |
| --- | --- |
| `AuthenticationResource` | `POST /register`, `/login`, `/logout`: cadastro, login e revogação. |
| `CourseResource` | `GET /courses`, `/courses/{id}`, `/courses/{id}/lessons`, `/courses/{id}/progress`; `POST` e `DELETE /courses/{id}/watchlist`. |
| `LessonResource` | `GET /lessons/{id}`; `POST` e `DELETE /lessons/{id}/complete`. |
| `UserResource` | `GET /me` e `/me/watchlist`, referentes ao usuário autenticado. |

Rotas de leitura de cursos e aulas são públicas; dados e operações pessoais exigem autenticação. `@AuthenticationPrincipal Long userId` recebe o ID colocado no contexto pelo filtro, não um ID passado pelo frontend.

## 8. DTOs e validação

DTO (*Data Transfer Object*) define o formato que entra ou sai da API. Evita retornar entidades completas, senhas e coleções internas. O projeto usa `record`, recurso Java que cria uma classe imutável e seus acessores a partir dos campos declarados.

- `RegisterRequest` e `LoginRequest`: dados de entrada. `@NotBlank`, `@Email` e `@Size` definem regras; `@Valid` no controller solicita a validação.
- `TokenDto` e `UserDto`: token e dados públicos do usuário.
- `CourseSummaryDto`, `CourseDetailDto` e `CoursePageDto`: resumo, detalhe e página de cursos. A página do servidor começa em zero; o frontend apresenta páginas a partir de um.
- `LessonDto`: dados da aula, ID do vídeo, curso quando necessário e conclusão.
- `ProgressDto`: contagens internas. `ProgressResponseDto.from` calcula `100 × concluídas / total`, ou zero para curso sem aulas.
- `WatchlistCourseDto`: resumo de curso salvo e contagens para o perfil.

`@JsonProperty` define nomes JSON como `youtube_id_video`; `@JsonInclude` omite campo nulo conforme configurado. `Boolean` pode representar valor ausente; `boolean` representa apenas verdadeiro ou falso.

## 9. Segurança

`SecurityConfig` desliga sessão HTTP e CSRF para esta API baseada em token, define rotas públicas e exige autenticação nas restantes. `TokenAuthenticationFilter`, executado uma vez por requisição, verifica o cabeçalho; se o token for válido, preenche o contexto de segurança. O filtro é registrado na cadeia Spring Security e sua inscrição automática como filtro de servlet é desabilitada para evitar execução duplicada.

Senha em hash não é criptografia reversível: o servidor compara a senha informada com o hash, sem guardar a senha original. Token é uma credencial: mantenha-o privado. O frontend usa `sessionStorage` por padrão ou `localStorage` quando escolhido “manter conectado”. Dados acessíveis por JavaScript podem ser expostos em caso de XSS; implantação pública exige revisão de segurança, HTTPS e configuração de origem.

## 10. YouTube e reprodução

`YoutubeImportRunner` só cria a tarefa quando a propriedade `youtube.import.playlist` está presente na inicialização. `YoutubeImportService` usa `RestClient`, com timeout de conexão e leitura. Envia a chave pelo cabeçalho `x-goog-api-key`, consulta `/playlists` e depois `/playlistItems`, seguindo `nextPageToken` até acabar. A API permite até 50 itens por página. As aulas usam o ID do vídeo e a posição fornecida, convertida para começar em 1.

O ID externo da playlist verifica se ela já foi importada. A operação não sincroniza alterações posteriores; vídeos privados/excluídos identificados pelo nome são ignorados. Depois da importação, as aulas são servidas pelo banco. O React cria um `iframe` para `youtube-nocookie.com/embed/ID`; não é o backend que hospeda os vídeos. É possível que o dono bloqueie incorporação.

Antes destas mudanças, faltava `src/api.js`, o que impedia compilar o frontend; o painel mostrava exemplos fixos e a recuperação de senha fingia enviar um email. Agora o cliente chama as rotas reais, o painel não inventa cursos e a recuperação informa que está indisponível.

## 11. Banco e migrations

`application.properties` lê URL, usuário e senha do ambiente ou `.env` local. `ddl-auto=validate` pede ao Hibernate que valide o esquema, sem criá-lo automaticamente. O Flyway executa migrations em ordem:

- `V1`: tabelas de usuários, professores, cursos, aulas e vínculos.
- `V2`: arquivo existente sem comandos.
- `V3`: tabela `revoked_token` legada; a estratégia atual usa `auth_token`.
- `V4`: tabela `auth_token` e índice de expiração.

Não altere migrations já aplicadas em bancos existentes; mudanças futuras precisam de nova migration. `src/test/resources/application.properties` usa H2 em memória para os testes, em modo de compatibilidade MySQL. H2 facilita testes, mas não substitui todas as diferenças do MySQL real.

## 12. Testes e roteiro para apresentação

Em `src/test/java`, testes de serviços e recursos verificam regras, status e autenticação; testes de contexto e migrations verificam inicialização e esquema; testes de token verificam comportamento após reinicialização. `YoutubeImportServiceTests` usa um servidor HTTP local que simula duas páginas, verifica ordem e recusa duplicata. Execute `sh gradlew test` no backend.

Sugestão de apresentação:

1. Mostre React, Spring e MySQL no diagrama da seção 1.
2. Demonstre cadastro: DTO validado → serviço → hash → repositório → token.
3. Mostre `Course`, `Lesson` e chaves compostas; explique chave primária e estrangeira.
4. Siga um clique em concluir aula: filtro → resource → service → repository → resposta.
5. Explique contagem de progresso e por que o usuário vem do token.
6. Mostre importação manual e player externo; diferencie ID do YouTube de ID interno.
7. Cite testes e limitações: catálogo vazio sem importação, recuperação de senha indisponível e importação sem sincronização automática.

**Resumo para estudar:** entidades modelam dados; repositórios acessam banco; serviços aplicam regras; recursos recebem HTTP; DTOs controlam formatos; o filtro identifica usuário; Flyway versiona o esquema; testes verificam comportamento.
