package br.com.medicare.repository;

import br.com.medicare.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    // Consulta personalizada (derivada do nome do método): médicos por nome da especialidade
    List<Medico> findByEspecialidadeNomeIgnoreCase(String nomeEspecialidade);

    boolean existsByEspecialidadeId(Long especialidadeId);

    boolean existsByCrm(String crm);
}
