package br.com.medicare.service;

import br.com.medicare.entity.Convenio;
import br.com.medicare.exception.RecursoNaoEncontradoException;
import br.com.medicare.exception.RegraNegocioException;
import br.com.medicare.repository.ConvenioRepository;
import br.com.medicare.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConvenioService {

    private final ConvenioRepository repository;
    private final PacienteRepository pacienteRepository;

    public ConvenioService(ConvenioRepository repository, PacienteRepository pacienteRepository) {
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;
    }

    public List<Convenio> listar() {
        return repository.findAll();
    }

    public Convenio buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convênio", id));
    }

    @Transactional
    public Convenio criar(Convenio convenio) {
        if (repository.existsByNomeIgnoreCase(convenio.getNome())) {
            throw new RegraNegocioException("Já existe um convênio com o nome " + convenio.getNome());
        }
        convenio.setId(null);
        return repository.save(convenio);
    }

    @Transactional
    public Convenio atualizar(Long id, Convenio dados) {
        Convenio atual = buscar(id);
        if (!atual.getNome().equalsIgnoreCase(dados.getNome()) && repository.existsByNomeIgnoreCase(dados.getNome())) {
            throw new RegraNegocioException("Já existe um convênio com o nome " + dados.getNome());
        }
        atual.setNome(dados.getNome());
        atual.setPercentualCobertura(dados.getPercentualCobertura());
        return repository.save(atual);
    }

    /** Regra de negócio: não é permitido excluir um convênio que ainda possui pacientes. */
    @Transactional
    public void excluir(Long id) {
        Convenio convenio = buscar(id);
        if (pacienteRepository.existsByConvenioId(id)) {
            throw new RegraNegocioException("Não é possível excluir o convênio "
                    + convenio.getNome() + " porque existem pacientes vinculados a ele");
        }
        repository.delete(convenio);
    }
}
