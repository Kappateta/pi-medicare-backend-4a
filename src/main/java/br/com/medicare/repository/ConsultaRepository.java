package br.com.medicare.repository;

import br.com.medicare.entity.Consulta;
import br.com.medicare.entity.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Consulta personalizada (JPQL): agenda de um médico num intervalo, sem as canceladas
    @Query("""
            SELECT c FROM Consulta c
            WHERE c.medico.id = :medicoId
              AND c.dataHora >= :inicio
              AND c.dataHora < :fim
              AND c.status <> br.com.medicare.entity.StatusConsulta.CANCELADA
            ORDER BY c.dataHora
            """)
    List<Consulta> buscarAgendaDoMedico(@Param("medicoId") Long medicoId,
                                        @Param("inicio") LocalDateTime inicio,
                                        @Param("fim") LocalDateTime fim);

    // Consulta personalizada (JPQL): histórico de um paciente, mais recente primeiro
    @Query("SELECT c FROM Consulta c WHERE c.paciente.id = :pacienteId ORDER BY c.dataHora DESC")
    List<Consulta> historicoDoPaciente(@Param("pacienteId") Long pacienteId);

    // Consulta derivada: filtro por status
    List<Consulta> findByStatus(StatusConsulta status);

    boolean existsByMedicoIdAndDataHoraAndStatusNot(Long medicoId, LocalDateTime dataHora, StatusConsulta status);

    boolean existsByPacienteIdAndDataHoraAndStatusNot(Long pacienteId, LocalDateTime dataHora, StatusConsulta status);

    boolean existsByMedicoId(Long medicoId);

    boolean existsByPacienteId(Long pacienteId);
}
