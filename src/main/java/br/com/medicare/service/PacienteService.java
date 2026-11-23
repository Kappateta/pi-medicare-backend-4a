package br.com.medicare.service;

import br.com.medicare.dto.PacienteRequest;
import br.com.medicare.entity.Consulta;
import br.com.medicare.entity.Paciente;
import br.com.medicare.exception.RecursoNaoEncontradoException;
import br.com.medicare.exception.RegraNegocioException;
import br.com.medicare.repository.ConsultaRepository;
import br.com.medicare.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final ConsultaRepository consultaRepository;
    private final ConvenioService convenioService;

    public PacienteService(PacienteRepository repository, ConsultaRepository consultaRepository,
                           ConvenioService convenioService) {
        this.repository = repository;
        this.consultaRepository = consultaRepository;
        this.convenioService = convenioService;
    }

    public List<Paciente> listar(String nome) {
        if (nome != null && !nome.isBlank()) {
            return repository.findByNomeContainingIgnoreCase(nome);
        }
        return repository.findAll();
    }

    public Paciente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    public List<Consulta> historico(Long id) {
        buscar(id);
        return consultaRepository.historicoDoPaciente(id);
    }

    @Transactional
    public Paciente criar(PacienteRequest dados) {
        if (repository.existsByCpf(dados.cpf())) {
            throw new RegraNegocioException("Já existe um paciente cadastrado com o CPF " + dados.cpf());
        }
        Paciente paciente = new Paciente();
        preencher(paciente, dados);
        return repository.save(paciente);
    }

    @Transactional
    public Paciente atualizar(Long id, PacienteRequest dados) {
        Paciente paciente = buscar(id);
        if (!paciente.getCpf().equals(dados.cpf()) && repository.existsByCpf(dados.cpf())) {
            throw new RegraNegocioException("Já existe um paciente cadastrado com o CPF " + dados.cpf());
        }
        preencher(paciente, dados);
        return repository.save(paciente);
    }

    /** Regra de negócio: paciente com consultas no histórico não pode ser excluído. */
    @Transactional
    public void excluir(Long id) {
        Paciente paciente = buscar(id);
        if (consultaRepository.existsByPacienteId(id)) {
            throw new RegraNegocioException("Não é possível excluir o paciente " + paciente.getNome()
                    + " porque ele possui consultas registradas");
        }
        repository.delete(paciente);
    }

    private void preencher(Paciente paciente, PacienteRequest dados) {
        paciente.setNome(dados.nome());
        paciente.setCpf(dados.cpf());
        paciente.setEmail(dados.email());
        paciente.setDataNascimento(dados.dataNascimento());
        paciente.setConvenio(dados.convenioId() == null ? null : convenioService.buscar(dados.convenioId()));
    }
}
