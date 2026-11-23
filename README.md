# Guacamole

Aplicação de cursos em vídeo: React no navegador, API Java/Spring Boot e MySQL para usuários, cursos e progresso. O vídeo é reproduzido pelo player do YouTube. Para entender o código Java, leia [ESTUDO_JAVA.md](ESTUDO_JAVA.md).

## Requisitos

- JDK 25, Node.js 20.19+ ou 22.12+ e npm.
- MySQL 8 disponível, ou Docker para iniciar um banco local.
- Internet para baixar dependências e reproduzir vídeos. Para importar playlists públicas, também é necessária uma chave da YouTube Data API v3.

## 1. Banco de dados

Com um MySQL próprio, crie um banco vazio chamado `guacamole` e use um usuário com permissões nesse banco. Alternativamente, para desenvolvimento local com Docker, escolha uma senha local e execute:

```sh
docker run -d --name guacamole-local -p 127.0.0.1:3307:3306 \
  -e MYSQL_DATABASE=guacamole -e MYSQL_ROOT_PASSWORD='SUA_SENHA_LOCAL' mysql:8.0
```

Guarde a senha escolhida: será necessária para o backend. Este exemplo usa a porta **3307** para não interferir em outro MySQL. O banco ficará dentro do container; não o remova se quiser preservar os dados. Para reiniciá-lo depois: `docker start guacamole-local`. Para parar: `docker stop guacamole-local`.

## 2. Backend

Copie `backend/Spring/.env.example` para `backend/Spring/.env` e preencha os valores. Para o Docker acima, por exemplo:

```properties
DB_URL=jdbc:mysql://127.0.0.1:3307/guacamole
DB_USERNAME=root
DB_PASSWORD=SUA_SENHA_LOCAL
```

O arquivo `.env` é local e ignorado pelo Git. Nunca envie credenciais. No terminal:

```sh
cd backend/Spring
sh gradlew bootRun
```

No Windows, use `gradlew.bat bootRun`. O Gradle usa Java 25; se necessário, configure `JAVA_HOME`. O Flyway cria as tabelas no banco vazio e o backend atende em `http://localhost:8080`. O processo deve continuar aberto. A raiz `/` pode retornar 404; teste `http://localhost:8080/courses`.

## 3. Frontend

Em outro terminal:

```sh
cd frontend
npm ci
npm run dev -- --host 127.0.0.1
```

Abra o endereço mostrado pelo Vite, normalmente `http://127.0.0.1:5173`. O Vite encaminha `/api` para o backend em `localhost:8080`, evitando configuração de CORS no desenvolvimento. Cadastre-se com senha de pelo menos 12 caracteres e no máximo 72 bytes; entre, salve cursos e marque aulas concluídas. Ao sair, o token é revogado. Em implantação, configure um proxy que encaminhe `/api` ao Spring ou configure `VITE_API_URL` e CORS adequados; o proxy acima é apenas para desenvolvimento.

Um banco novo tem catálogo vazio. A recuperação de senha ainda não foi implementada e não envia e-mails. Os totais do perfil consideram cursos atualmente salvos; retirar um curso não apaga as conclusões no banco.

## Importar playlist do YouTube

1. No Google Cloud, ative a YouTube Data API v3 e crie uma chave apropriada. Não coloque a chave no código nem no Git.
2. Escolha uma playlist pública e copie o valor de `list` da URL. Consulte a [documentação de playlists](https://developers.google.com/youtube/data/reference/rest/v3/playlists/list) e de [itens da playlist](https://developers.google.com/youtube/data/reference/rest/v3/playlistItems/list).
3. Pare o backend em execução. No mesmo terminal configurado com o banco, informe `YOUTUBE_API_KEY` no ambiente e inicie explicitamente com a propriedade abaixo:

```sh
cd backend/Spring
export YOUTUBE_API_KEY='SUA_CHAVE'
sh gradlew bootRun --args='--youtube.import.playlist=ID_DA_PLAYLIST'
```

No PowerShell, use `$env:YOUTUBE_API_KEY = 'SUA_CHAVE'` e `./gradlew.bat bootRun --args='--youtube.import.playlist=ID_DA_PLAYLIST'`. Não adicione a chave ao comando `--args`: ela deve permanecer no ambiente. Ao concluir a importação o servidor permanece ligado. Na próxima execução normal, omita `--args` para não tentar importar de novo.

O importador cria um curso com o nome da playlist e um professor com o nome do canal; lê todas as páginas e guarda IDs e posições dos vídeos. Vídeos privados/excluídos identificados pelo YouTube são ignorados. Falhas HTTP, quota ou timeout abortam a operação; verifique a chave, permissões e disponibilidade. A importação é transacional e rejeita uma playlist já existente; não atualiza playlists já importadas. Execute apenas uma importação por vez: não há trava entre processos concorrentes. Alguns vídeos podem bloquear reprodução incorporada. O player depende de conexão com o YouTube.

## Testes

```sh
cd backend/Spring
sh gradlew test
```

Os testes Java usam H2 em memória, sem exigir MySQL. O teste do importador simula a API YouTube localmente. Para compilar o frontend: `cd frontend && npm ci && npm run build`.

Para testar cadastro, perfil e logout no navegador, deixe frontend e backend ligados, execute `npx playwright install chromium` uma vez e rode `npm run test:flow` em `frontend/`. **Esse teste cria uma conta no banco configurado**; use um banco de testes quando necessário. Os arquivos antigos `flow.mjs` e `audit.mjs` dependem de uma API simulada ausente e não representam o fluxo atual.

## Estrutura

- `backend/Spring/src/main/java/com/guacamole/backend`: entidades, repositórios, serviços, recursos HTTP, DTOs e segurança.
- `backend/Spring/src/main/resources/db/migration`: migrations Flyway; `application.properties` configura banco e autenticação.
- `frontend/src`: páginas React, componentes, cliente HTTP e estilos.
- `api_cursos.md`: requisitos funcionais locais, quando disponível.
