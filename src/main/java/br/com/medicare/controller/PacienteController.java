package br.com.medicare.controller;

import br.com.medicare.dto.PacienteRequest;
import br.com.medicare.entity.Consulta;
import br.com.medicare.entity.Paciente;
import br.com.medicare.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    /** GET /api/pacientes ou /api/pacientes?nome=silva */
    @GetMapping
    public List<Paciente> listar(@RequestParam(required = false) String nome) {
        return service.listar(nome);
    }

    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/historico")
    public List<Consulta> historico(@PathVariable Long id) {
        return service.historico(id);
    }

    @PostMapping
    public ResponseEntity<Paciente> criar(@Valid @RequestBody PacienteRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dados));
    }

    @PutMapping("/{id}")
    public Paciente atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
