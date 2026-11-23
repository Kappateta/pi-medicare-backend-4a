# Diagrama de classes — Medicare

O GitHub desenha este diagrama automaticamente (Mermaid).

```mermaid
classDiagram
    class Especialidade {
        Long id
        String nome
        String descricao
    }
    class Convenio {
        Long id
        String nome
        Integer percentualCobertura
    }
    class Medico {
        Long id
        String nome
        String crm
        String email
        BigDecimal valorConsulta
    }
    class Paciente {
        Long id
        String nome
        String cpf
        String email
        LocalDate dataNascimento
    }
    class Consulta {
        Long id
        LocalDateTime dataHora
        StatusConsulta status
        BigDecimal valorFinal
        String observacoes
    }
    class StatusConsulta {
        <<enumeration>>
        AGENDADA
        REALIZADA
        CANCELADA
    }

    Especialidade "1" <-- "0..*" Medico : pertence a
    Convenio "0..1" <-- "0..*" Paciente : possui
    Medico "1" <-- "0..*" Consulta : atende
    Paciente "1" <-- "0..*" Consulta : agenda
    Consulta --> StatusConsulta
```

## Tabelas geradas (MySQL)

| Tabela | Chaves estrangeiras |
|---|---|
| especialidades | — |
| convenios | — |
| medicos | especialidade_id → especialidades.id |
| pacientes | convenio_id → convenios.id (opcional) |
| consultas | medico_id → medicos.id, paciente_id → pacientes.id |
