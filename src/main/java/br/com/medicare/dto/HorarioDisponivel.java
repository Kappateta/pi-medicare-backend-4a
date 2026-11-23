package br.com.medicare.dto;

import java.time.LocalDateTime;

public record HorarioDisponivel(LocalDateTime dataHora, boolean disponivel) {}
