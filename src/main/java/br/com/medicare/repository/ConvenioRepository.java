package br.com.medicare.repository;

import br.com.medicare.entity.Convenio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvenioRepository extends JpaRepository<Convenio, Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
