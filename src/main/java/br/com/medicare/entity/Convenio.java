package br.com.medicare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "convenios")
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do convênio é obrigatório")
    @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres")
    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @NotNull(message = "O percentual de cobertura é obrigatório")
    @Min(value = 0, message = "A cobertura mínima é 0%")
    @Max(value = 100, message = "A cobertura máxima é 100%")
    @Column(nullable = false)
    private Integer percentualCobertura;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getPercentualCobertura() { return percentualCobertura; }
    public void setPercentualCobertura(Integer percentualCobertura) { this.percentualCobertura = percentualCobertura; }
}
