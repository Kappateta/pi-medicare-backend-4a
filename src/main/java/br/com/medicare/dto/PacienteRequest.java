package br.com.medicare.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PacienteRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 dígitos, sem pontos ou traço")
        String cpf,

        @Email(message = "E-mail inválido")
        String email,

        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        // Opcional (paciente particular)
        Long convenioId
) {}
