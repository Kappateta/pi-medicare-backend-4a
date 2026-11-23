package br.com.medicare.service;

import br.com.medicare.dto.HorarioDisponivel;
import br.com.medicare.dto.MedicoRequest;
import br.com.medicare.entity.Consulta;
import br.com.medicare.entity.Medico;
import br.com.medicare.exception.RecursoNaoEncontradoException;
import br.com.medicare.exception.RegraNegocioException;
import br.com.medicare.repository.ConsultaRepository;
import br.com.medicare.repository.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MedicoService {

    /** Horário de atendimento da clínica: consultas de 1 hora, das 8h às 18h. */
    public static final int HORA_ABERTURA = 8;
    public static final int HORA_FECHAMENTO = 18;

    private final MedicoRepository repository;
    private final ConsultaRepository consultaRepository;
    private final EspecialidadeService especialidadeService;

    public MedicoService(MedicoRepository repository, ConsultaRepository consultaRepository,
                         EspecialidadeService especialidadeService) {
        this.repository = repository;
        this.consultaRepository = consultaRepository;
        this.especialidadeService = especialidadeService;
    }

    public List<Medico> listar(String especialidade) {
        if (especialidade != null && !especialidade.isBlank()) {
            return repository.findByEspecialidadeNomeIgnoreCase(especialidade);
        }
        return repository.findAll();
    }

    public Medico buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico", id));
    }

    @Transactional
    public Medico criar(MedicoRequest dados) {
        if (repository.existsByCrm(dados.crm())) {
            throw new RegraNegocioException("Já existe um médico cadastrado com o CRM " + dados.crm());
        }
        Medico medico = new Medico();
        preencher(medico, dados);
        return repository.save(medico);
    }

    @Transactional
    public Medico atualizar(Long id, MedicoRequest dados) {
        Medico medico = buscar(id);
        if (!medico.getCrm().equals(dados.crm()) && repository.existsByCrm(dados.crm())) {
            throw new RegraNegocioException("Já existe um médico cadastrado com o CRM " + dados.crm());
        }
        preencher(medico, dados);
        return repository.save(medico);
    }

    /** Regra de negócio: médico com consultas no histórico não pode ser excluído. */
    @Transactional
    public void excluir(Long id) {
        Medico medico = buscar(id);
        if (consultaRepository.existsByMedicoId(id)) {
            throw new RegraNegocioException("Não é possível excluir o médico " + medico.getNome()
                    + " porque ele possui consultas registradas");
        }
        repository.delete(medico);
    }

    /**
     * Funcionalidade específica do PI: monta a agenda do médico num dia,
     * indicando quais horários estão livres para agendamento.
     */
    public List<HorarioDisponivel> agendaDoDia(Long medicoId, LocalDate data) {
        buscar(medicoId);
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        Set<LocalDateTime> ocupados = consultaRepository.buscarAgendaDoMedico(medicoId, inicio, fim)
                .stream().map(Consulta::getDataHora).collect(Collectors.toSet());

        LocalDateTime agora = LocalDateTime.now();
        List<HorarioDisponivel> agenda = new ArrayList<>();
        for (int hora = HORA_ABERTURA; hora < HORA_FECHAMENTO; hora++) {
            LocalDateTime horario = data.atTime(hora, 0);
            boolean livre = !ocupados.contains(horario) && horario.isAfter(agora);
            agenda.add(new HorarioDisponivel(horario, livre));
        }
        return agenda;
    }

    private void preencher(Medico medico, MedicoRequest dados) {
        // Regra de negócio: o valor da consulta precisa ser positivo
        if (dados.valorConsulta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor da consulta deve ser maior que zero");
        }
        medico.setNome(dados.nome());
        medico.setCrm(dados.crm());
        medico.setEmail(dados.email());
        medico.setValorConsulta(dados.valorConsulta());
        medico.setEspecialidade(especialidadeService.buscar(dados.especialidadeId()));
    }
}
