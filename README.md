# Medicare — Backend REST

**Projeto Integrador:** Medicare
**Disciplina:** Padrões de Desenvolvimento Web · Engenharia de Software · 4º Termo

## Descrição

O Medicare é um sistema de gestão de clínica médica. Este backend REST cuida do cadastro de
especialidades, convênios, médicos e pacientes, e do **agendamento de consultas**. O agendamento
verifica a disponibilidade do médico e do paciente e calcula o valor a pagar de acordo com a
cobertura do convênio.

## Objetivo

Oferecer uma API segura para a recepção e a administração da clínica organizarem a agenda médica.
A API evita conflitos de horário e garante a integridade dos cadastros.

## Tecnologias

- Java 17
- Spring Boot 3.3 (Web, Data JPA, Validation, Security)
- MySQL 8
- Maven
- Postman (testes)

## Como executar

1. Instale o **JDK 17+**, o **Maven** e o **MySQL 8**. O IntelliJ e o VS Code já trazem o Maven.
2. Ajuste o usuário e a senha do MySQL em `src/main/resources/application.properties`.
   O banco `medicare` é criado automaticamente.
3. Na pasta do projeto, rode:
   ```
   mvn spring-boot:run
   ```
4. A API sobe em `http://localhost:8080/api`.
   Para testar sem MySQL, use o perfil de teste (banco H2 em memória, sempre vazio):
   `mvn spring-boot:run -Dspring-boot.run.profiles=test`
5. No Postman, importe `postman/projeto-integrador.postman_collection.json` e rode a coleção
   **com o banco vazio e na ordem**, porque os testes usam os ids 1 e 2.

## Usuários de teste (em memória)

| Usuário | Senha | Perfil | Pode |
|---|---|---|---|
| admin | admin123 | ADMIN | Tudo, inclusive excluir e cadastrar médicos, especialidades e convênios |
| recepcao | recepcao123 | RECEPCAO | Consultar tudo, cadastrar e editar pacientes, agendar, cancelar e realizar consultas |

Sem credenciais, a API responde **401**. Quando a recepção tenta excluir ou mexer em médicos,
especialidades ou convênios, a resposta é **403**.

## Arquitetura

```
controller  →  service  →  repository  →  entity (JPA)
                  ↑
          regras de negócio
```
Os controllers só falam com os services. Os erros são tratados num único ponto, o
`GlobalExceptionHandler` (`@RestControllerAdvice`):

| Situação | HTTP |
|---|---|
| Bean Validation falhou / JSON inválido | 400 |
| Recurso não existe (`RecursoNaoEncontradoException`) | 404 |
| Regra de negócio violada (`RegraNegocioException`) / duplicidade | 409 |

## Regras de negócio (camada Service)

1. A consulta precisa ser em hora cheia, dentro do expediente (8h às 17h).
2. O médico não pode ter duas consultas no mesmo horário.
3. O paciente não pode ter duas consultas no mesmo horário.
4. O valor final da consulta é o valor do médico menos a % de cobertura do convênio.
5. Só é possível cancelar consultas AGENDADAS, com pelo menos 2 horas de antecedência.
6. Uma especialidade com médicos não pode ser excluída, nem um convênio com pacientes.
   Médicos e pacientes com consultas registradas também não.
7. O CRM, o CPF e os nomes de especialidade e convênio são únicos. O valor da consulta deve ser maior que zero.

## Consultas personalizadas

| Repositório | Método | Tipo |
|---|---|---|
| PacienteRepository | `findByNomeContainingIgnoreCase` | derivada do nome |
| MedicoRepository | `findByEspecialidadeNomeIgnoreCase` | derivada do nome |
| ConsultaRepository | `buscarAgendaDoMedico` | `@Query` JPQL |
| ConsultaRepository | `historicoDoPaciente` | `@Query` JPQL |

## Funcionalidade específica do PI: agendamento inteligente

- `GET /api/medicos/{id}/agenda?data=AAAA-MM-DD` devolve os 10 horários do dia (8h às 17h) e
  indica quais estão livres. Horários já passados e horários ocupados aparecem como indisponíveis.
- `POST /api/consultas` agenda a consulta, aplica as regras 1 a 3 e calcula o valor com o desconto do convênio.
- `PATCH /api/consultas/{id}/cancelar` e `PATCH /api/consultas/{id}/realizar` controlam o ciclo de vida da consulta.

## Endpoints

| Método | Rota | Perfil |
|---|---|---|
| GET | /api/especialidades, /api/especialidades/{id} | autenticado |
| POST, PUT | /api/especialidades | ADMIN |
| DELETE | /api/especialidades/{id} | ADMIN |
| GET | /api/convenios, /api/convenios/{id} | autenticado |
| POST, PUT | /api/convenios | ADMIN |
| DELETE | /api/convenios/{id} | ADMIN |
| GET | /api/medicos?especialidade=, /api/medicos/{id} | autenticado |
| GET | /api/medicos/{id}/agenda?data= | autenticado |
| POST, PUT | /api/medicos | ADMIN |
| DELETE | /api/medicos/{id} | ADMIN |
| GET | /api/pacientes?nome=, /api/pacientes/{id}, /api/pacientes/{id}/historico | ADMIN, RECEPCAO |
| POST, PUT | /api/pacientes | ADMIN, RECEPCAO |
| DELETE | /api/pacientes/{id} | ADMIN |
| GET | /api/consultas?status=, /api/consultas/{id} | ADMIN, RECEPCAO |
| POST, PUT | /api/consultas | ADMIN, RECEPCAO |
| PATCH | /api/consultas/{id}/cancelar, /api/consultas/{id}/realizar | ADMIN, RECEPCAO |
| DELETE | /api/consultas/{id} | ADMIN |

### Exemplo: agendar consulta

```json
POST /api/consultas
{
  "medicoId": 1,
  "pacienteId": 1,
  "dataHora": "2026-12-15T10:00:00",
  "observacoes": "Check-up anual"
}
```
Resposta `201`: consulta com `status: "AGENDADA"` e `valorFinal: 100.00`
(médico de R$ 250,00 e convênio com 60% de cobertura).

### Formato de erro

```json
{
  "timestamp": "2026-11-22T14:00:00",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "O médico já possui uma consulta agendada para 2026-12-15T10:00",
  "detalhes": []
}
```

## Diagrama

Veja [docs/diagrama-classes.md](docs/diagrama-classes.md).

## Estrutura

```
medicare-backend/
├── README.md
├── pom.xml
├── docs/diagrama-classes.md
├── postman/projeto-integrador.postman_collection.json
└── src/main/java/br/com/medicare/
    ├── config/        (Spring Security)
    ├── controller/
    ├── dto/
    ├── entity/
    ├── exception/
    ├── repository/
    └── service/
```

## Integrantes

| Nome completo | ID acadêmico |
|---|---|
| _preencher_ | _preencher_ |
| _preencher_ | _preencher_ |
| _preencher_ | _preencher_ |
