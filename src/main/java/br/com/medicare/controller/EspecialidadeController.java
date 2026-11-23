package br.com.medicare.controller;

import br.com.medicare.entity.Especialidade;
import br.com.medicare.service.EspecialidadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService service;

    public EspecialidadeController(EspecialidadeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Especialidade> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Especialidade buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Especialidade> criar(@Valid @RequestBody Especialidade especialidade) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(especialidade));
    }

    @PutMapping("/{id}")
    public Especialidade atualizar(@PathVariable Long id, @Valid @RequestBody Especialidade especialidade) {
        return service.atualizar(id, especialidade);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
