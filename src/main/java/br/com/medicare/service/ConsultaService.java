package br.com.medicare.service;

import br.com.medicare.dto.ConsultaRequest;
import br.com.medicare.entity.*;
import br.com.medicare.exception.RecursoNaoEncontradoException;
import br.com.medicare.exception.RegraNegocioException;
import br.com.medicare.repository.ConsultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaService {

    private final ConsultaRepository repository;
    private final MedicoService medicoService;
    private final PacienteService pacienteService;

    public ConsultaService(ConsultaRepository repository, MedicoService medicoService,
                           PacienteService pacienteService) {
        this.repository = repository;
        this.medicoService = medicoService;
        this.pacienteService = pacienteService;
    }

    public List<Consulta> listar(StatusConsulta status) {
        return status == null ? repository.findAll() : repository.findByStatus(status);
    }

    public Consulta buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", id));
    }

    /** Agenda uma nova consulta aplicando as regras de negócio da clínica. */
    @Transactional
    public Consulta agendar(ConsultaRequest dados) {
        Medico medico = medicoService.buscar(dados.medicoId());
        Paciente paciente = pacienteService.buscar(dados.pacienteId());
        validarHorario(dados.dataHora(), medico.getId(), paciente.getId(), null);

        Consulta consulta = new Consulta();
        consulta.setMedico(medico);
        consulta.setPaciente(paciente);
        consulta.setDataHora(dados.dataHora());
        consulta.setObservacoes(dados.observacoes());
        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta.setValorFinal(calcularValor(medico, paciente));
        return repository.save(consulta);
    }

    /** Reagendamento: só é possível alterar consultas que ainda estão AGENDADAS. */
    @Transactional
    public Consulta atualizar(Long id, ConsultaRequest dados) {
        Consulta consulta = buscar(id);
        exigirAgendada(consulta, "alterar");

        Medico medico = medicoService.buscar(dados.medicoId());
        Paciente paciente = pacienteService.buscar(dados.pacienteId());
        validarHorario(dados.dataHora(), medico.getId(), paciente.getId(), consulta);

        consulta.setMedico(medico);
        consulta.setPaciente(paciente);
        consulta.setDataHora(dados.dataHora());
        consulta.setObservacoes(dados.observacoes());
        consulta.setValorFinal(calcularValor(medico, paciente));
        return repository.save(consulta);
    }

    /** Regra de negócio: cancelamento só com status AGENDADA e pelo menos 2 horas de antecedência. */
    @Transactional
    public Consulta cancelar(Long id) {
        Consulta consulta = buscar(id);
        exigirAgendada(consulta, "cancelar");
        if (consulta.getDataHora().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new RegraNegocioException("A consulta só pode ser cancelada com pelo menos 2 horas de antecedência");
        }
        consulta.setStatus(StatusConsulta.CANCELADA);
        return repository.save(consulta);
    }

    @Transactional
    public Consulta realizar(Long id) {
        Consulta consulta = buscar(id);
        exigirAgendada(consulta, "marcar como realizada");
        consulta.setStatus(StatusConsulta.REALIZADA);
        return repository.save(consulta);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    // ---------------------------------------------------------------- regras

    private void validarHorario(LocalDateTime dataHora, Long medicoId, Long pacienteId, Consulta atual) {
        // Regra 1: horário dentro do expediente e em hora cheia (consultas de 1h)
        int hora = dataHora.getHour();
        if (dataHora.getMinute() != 0 || dataHora.getSecond() != 0
                || hora < MedicoService.HORA_ABERTURA || hora >= MedicoService.HORA_FECHAMENTO) {
            throw new RegraNegocioException("As consultas devem ser marcadas em hora cheia, entre "
                    + MedicoService.HORA_ABERTURA + "h e " + (MedicoService.HORA_FECHAMENTO - 1) + "h");
        }

        boolean mesmoHorario = atual != null && atual.getDataHora().equals(dataHora);

        // Regra 2: o médico não pode ter duas consultas no mesmo horário
        boolean mesmoMedico = mesmoHorario && atual.getMedico().getId().equals(medicoId);
        if (!mesmoMedico && repository.existsByMedicoIdAndDataHoraAndStatusNot(
                medicoId, dataHora, StatusConsulta.CANCELADA)) {
            throw new RegraNegocioException("O médico já possui uma consulta agendada para " + dataHora);
        }

        // Regra 3: o paciente não pode ter duas consultas no mesmo horário
        boolean mesmoPaciente = mesmoHorario && atual.getPaciente().getId().equals(pacienteId);
        if (!mesmoPaciente && repository.existsByPacienteIdAndDataHoraAndStatusNot(
                pacienteId, dataHora, StatusConsulta.CANCELADA)) {
            throw new RegraNegocioException("O paciente já possui uma consulta agendada para " + dataHora);
        }
    }

    /** Regra 4: o valor cobrado recebe o desconto da cobertura do convênio do paciente. */
    private BigDecimal calcularValor(Medico medico, Paciente paciente) {
        BigDecimal valor = medico.getValorConsulta();
        if (paciente.getConvenio() == null) {
            return valor.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal cobertura = BigDecimal.valueOf(paciente.getConvenio().getPercentualCobertura());
        BigDecimal fatorPaciente = BigDecimal.valueOf(100).subtract(cobertura)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return valor.multiply(fatorPaciente).setScale(2, RoundingMode.HALF_UP);
    }

    private void exigirAgendada(Consulta consulta, String acao) {
        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("Não é possível " + acao + " uma consulta com status "
                    + consulta.getStatus());
        }
    }
}
