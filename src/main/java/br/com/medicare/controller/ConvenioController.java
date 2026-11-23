package br.com.medicare.controller;

import br.com.medicare.entity.Convenio;
import br.com.medicare.service.ConvenioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/convenios")
public class ConvenioController {

    private final ConvenioService service;

    public ConvenioController(ConvenioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Convenio> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Convenio buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Convenio> criar(@Valid @RequestBody Convenio convenio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(convenio));
    }

    @PutMapping("/{id}")
    public Convenio atualizar(@PathVariable Long id, @Valid @RequestBody Convenio convenio) {
        return service.atualizar(id, convenio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
