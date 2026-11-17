# Backend Guacamole

Execute os comandos neste diretório com JDK 25 e MySQL disponíveis. O Spring Boot carrega automaticamente `.env` como arquivo de propriedades ao iniciar pelo Gradle ou pelo IntelliJ; no IntelliJ, use a raiz do repositório ou `backend/Spring` como diretório de trabalho.

1. Se necessário, copie `.env.example` para `.env`.
2. Configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` para o MySQL. Usuário e senha são obrigatórios; use uma conta exclusiva com privilégios mínimos. Nunca compartilhe esse arquivo.
3. Execute `.\gradlew.bat bootRun` no Windows ou `./gradlew bootRun` no Linux/macOS. No IntelliJ, inicie `BackendApplication`.
4. Execute os testes com `.\gradlew.bat test` ou `./gradlew test`. Eles usam configuração própria em `src/test/resources`.

O `.env` usa a sintaxe de arquivos `.properties` do Java: não envolva valores em aspas; barras invertidas e caracteres especiais devem ser escapados conforme essa sintaxe. Variáveis de ambiente do sistema têm prioridade sobre os valores do arquivo.

O Flyway executa as migrations em bancos novos. Em bancos existentes sem histórico, a aplicação não faz baseline automaticamente. Faça backup e compare a estrutura antes de qualquer baseline manual. Consulte `../../docs/SEGURANCA_E_MANUTENCAO.md` para o procedimento e as limitações de segurança.
