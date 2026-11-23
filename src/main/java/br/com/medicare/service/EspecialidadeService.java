package br.com.medicare.service;

import br.com.medicare.entity.Especialidade;
import br.com.medicare.exception.RecursoNaoEncontradoException;
import br.com.medicare.exception.RegraNegocioException;
import br.com.medicare.repository.EspecialidadeRepository;
import br.com.medicare.repository.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EspecialidadeService {

    private final EspecialidadeRepository repository;
    private final MedicoRepository medicoRepository;

    public EspecialidadeService(EspecialidadeRepository repository, MedicoRepository medicoRepository) {
        this.repository = repository;
        this.medicoRepository = medicoRepository;
    }

    public List<Especialidade> listar() {
        return repository.findAll();
    }

    public Especialidade buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Especialidade", id));
    }

    @Transactional
    public Especialidade criar(Especialidade especialidade) {
        if (repository.existsByNomeIgnoreCase(especialidade.getNome())) {
            throw new RegraNegocioException("Já existe uma especialidade com o nome " + especialidade.getNome());
        }
        especialidade.setId(null);
        return repository.save(especialidade);
    }

    @Transactional
    public Especialidade atualizar(Long id, Especialidade dados) {
        Especialidade atual = buscar(id);
        if (!atual.getNome().equalsIgnoreCase(dados.getNome()) && repository.existsByNomeIgnoreCase(dados.getNome())) {
            throw new RegraNegocioException("Já existe uma especialidade com o nome " + dados.getNome());
        }
        atual.setNome(dados.getNome());
        atual.setDescricao(dados.getDescricao());
        return repository.save(atual);
    }

    /** Regra de negócio: não é permitido excluir uma especialidade que possui médicos vinculados. */
    @Transactional
    public void excluir(Long id) {
        Especialidade especialidade = buscar(id);
        if (medicoRepository.existsByEspecialidadeId(id)) {
            throw new RegraNegocioException("Não é possível excluir a especialidade "
                    + especialidade.getNome() + " porque existem médicos vinculados a ela");
        }
        repository.delete(especialidade);
    }
}
