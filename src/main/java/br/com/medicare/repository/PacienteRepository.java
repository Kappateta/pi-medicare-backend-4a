package br.com.medicare.repository;

import br.com.medicare.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    // Consulta personalizada (derivada do nome do método): busca por parte do nome
    List<Paciente> findByNomeContainingIgnoreCase(String nome);

    boolean existsByCpf(String cpf);

    boolean existsByConvenioId(Long convenioId);
}
