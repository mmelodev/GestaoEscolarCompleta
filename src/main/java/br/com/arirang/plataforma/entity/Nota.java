package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "notas")
public class Nota {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boletim_id", nullable = false)
    private Boletim boletim;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_nota", nullable = false)
    private TipoNota tipoNota;
    
    @NotBlank(message = "Descrição é obrigatória")
    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;
    
    @NotNull(message = "Nota é obrigatória")
    @Min(value = 1, message = "Nota deve ser no mínimo 1")
    @Max(value = 100, message = "Nota deve ser no máximo 100")
    @Column(name = "valor_nota", nullable = false)
    private Integer valorNota;
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Boletim getBoletim() { return boletim; }
    public void setBoletim(Boletim boletim) { this.boletim = boletim; }
    
    public TipoNota getTipoNota() { return tipoNota; }
    public void setTipoNota(TipoNota tipoNota) { this.tipoNota = tipoNota; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public Integer getValorNota() { return valorNota; }
    public void setValorNota(Integer valorNota) { this.valorNota = valorNota; }
}

