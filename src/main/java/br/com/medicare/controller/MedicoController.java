package br.com.medicare.controller;

import br.com.medicare.dto.HorarioDisponivel;
import br.com.medicare.dto.MedicoRequest;
import br.com.medicare.entity.Medico;
import br.com.medicare.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    /** GET /api/medicos ou /api/medicos?especialidade=Cardiologia */
    @GetMapping
    public List<Medico> listar(@RequestParam(required = false) String especialidade) {
        return service.listar(especialidade);
    }

    @GetMapping("/{id}")
    public Medico buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** Funcionalidade do PI: GET /api/medicos/1/agenda?data=2026-12-01 */
    @GetMapping("/{id}/agenda")
    public List<HorarioDisponivel> agenda(@PathVariable Long id,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.agendaDoDia(id, data);
    }

    @PostMapping
    public ResponseEntity<Medico> criar(@Valid @RequestBody MedicoRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dados));
    }

    @PutMapping("/{id}")
    public Medico atualizar(@PathVariable Long id, @Valid @RequestBody MedicoRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
