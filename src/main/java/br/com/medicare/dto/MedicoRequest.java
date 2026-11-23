package br.com.medicare.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MedicoRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "O CRM é obrigatório")
        @Size(max = 20, message = "O CRM deve ter no máximo 20 caracteres")
        String crm,

        @Email(message = "E-mail inválido")
        String email,

        @NotNull(message = "O valor da consulta é obrigatório")
        BigDecimal valorConsulta,

        @NotNull(message = "A especialidade é obrigatória")
        Long especialidadeId
) {}
