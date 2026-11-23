package br.com.medicare.controller;

import br.com.medicare.dto.ConsultaRequest;
import br.com.medicare.entity.Consulta;
import br.com.medicare.entity.StatusConsulta;
import br.com.medicare.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    /** GET /api/consultas ou /api/consultas?status=AGENDADA */
    @GetMapping
    public List<Consulta> listar(@RequestParam(required = false) StatusConsulta status) {
        return service.listar(status);
    }

    @GetMapping("/{id}")
    public Consulta buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Consulta> agendar(@Valid @RequestBody ConsultaRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agendar(dados));
    }

    @PutMapping("/{id}")
    public Consulta atualizar(@PathVariable Long id, @Valid @RequestBody ConsultaRequest dados) {
        return service.atualizar(id, dados);
    }

    @PatchMapping("/{id}/cancelar")
    public Consulta cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @PatchMapping("/{id}/realizar")
    public Consulta realizar(@PathVariable Long id) {
        return service.realizar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
