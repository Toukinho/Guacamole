# Guacamole

Backend Java para uma API de cursos. O documento [api_cursos.md](api_cursos.md) descreve os requisitos; a implementação ainda não cobre todas as rotas nele previstas.

## Estrutura

O backend fica em `backend/Spring`. O código Java está em `src/main/java/com/guacamole/backend`:

| Pacote | Responsabilidade |
| --- | --- |
| `entity` | Entidades JPA e chaves compostas. |
| `repositories` | Acesso aos dados com Spring Data JPA. |
| `services` | Consultas e regras de negócio. |
| `resources` | Entrada HTTP; atualmente expõe a listagem de cursos. |
| `dto` | Dados devolvidos pela listagem e contagens de progresso. |

`frontend/` é um espaço reservado, sem aplicação configurada.

## Modelo de dados

Um `Teacher` ministra cursos (`Course`); cada curso possui aulas (`Lesson`) ordenadas por `position`. A combinação de curso e posição deve ser única. `User` tem email único.

`CourseWatchlist` representa o curso salvo por um usuário e registra `createdAt`. `LessonCompletion` representa a conclusão de uma aula e registra `completedAt`. Cada entidade associativa usa uma chave composta pelos IDs das duas entidades relacionadas; as datas são preenchidas ao persistir o vínculo.

As coleções `@OneToMany` são o lado inverso dos relacionamentos. O campo `@ManyToOne` controla a chave estrangeira: adicionar algo apenas à coleção inversa não persiste o vínculo. Coleções marcadas como `LAZY` são carregadas quando acessadas, dentro de uma sessão apropriada.

## Execução local

São necessários JDK 25, MySQL acessível e um esquema compatível com as entidades. Em PowerShell:

```powershell
cd backend/Spring
$env:SPRING_PROFILES_ACTIVE = 'local'
.\gradlew.bat bootRun
```

O perfil `local` carrega `src/main/resources/application-local.properties`, se existir. Mantenha esse arquivo ignorado pelo Git e configure nele as credenciais locais, ou forneça as variáveis de ambiente. A configuração padrão usa `DB_URL` (padrão `jdbc:mysql://localhost:3306/guacamole`), usuário `root` e `DB_PASSWORD`. Não compartilhe senhas.

`bootRun` permanece ativo enquanto o servidor estiver em execução; pressione `Ctrl+C` para encerrá-lo. Uma resposta 404 em `/` não indica, por si só, falha de inicialização.

Para compilar e executar testes:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
.\gradlew.bat test
.\gradlew.bat build
```

O teste de contexto utiliza a configuração do banco e pode exigir MySQL acessível. Use `test --rerun-tasks` para forçar a execução sem reaproveitar o resultado anterior.

## Banco e migrations

O Flyway está habilitado e procura migrations em `src/main/resources/db/migration`. O Hibernate usa `ddl-auto=validate`: ele valida o esquema, mas não cria as tabelas. O baseline está configurado na versão 1 para bancos existentes. O arquivo `V2__add_category_to_course.sql` está vazio; as migrations presentes não fornecem a criação inicial das tabelas. A conexão e um esquema compatível precisam estar disponíveis antes de iniciar a aplicação.

## Listagem de cursos

`GET /courses` retorna cursos paginados, com nome do professor. Os parâmetros são:

| Parâmetro | Padrão | Valores |
| --- | --- | --- |
| `page` | `0` | Inteiro maior ou igual a zero. |
| `size` | `20` | Inteiro entre 1 e 50. |

Os resultados são ordenados por título crescente e, em seguida, por ID. Exemplo: `GET /courses?page=0&size=20`.

```json
{
  "content": [{"id": 1, "title": "Java", "teacher": "Ana"}],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Valores numéricos fora dos limites e valores que não podem ser convertidos para inteiro resultam em resposta 400. A ordenação textual depende da collation configurada no banco.

## Serviços disponíveis e próximos passos

`CourseService` pagina e busca cursos. `LessonService` busca aulas e as lista em ordem. `UserCourseService` adiciona ou remove cursos salvos e conclusões, consulta vínculos e retorna contagens de progresso. As operações de escrita bloqueiam a linha do usuário durante a transação para coordenar alterações concorrentes realizadas por esses serviços. Remover um curso salvo não remove suas conclusões.

O ID recebido pelos métodos pessoais deve vir de uma autenticação confiável; esses métodos não validam tokens. As rotas pessoais e a autenticação ainda não foram implementadas. A porcentagem do progresso também permanece pendente: `ProgressDto` contém apenas as contagens. O documento de requisitos não define a estratégia de token, o arredondamento ou o comportamento para cursos sem aulas.
