package br.com.medicare.repository;

import br.com.medicare.entity.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
