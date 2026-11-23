package br.com.medicare.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record ConsultaRequest(
        @NotNull(message = "O médico é obrigatório")
        Long medicoId,

        @NotNull(message = "O paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "A data e hora são obrigatórias")
        @Future(message = "A consulta deve ser agendada para uma data futura")
        LocalDateTime dataHora,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes
) {}
