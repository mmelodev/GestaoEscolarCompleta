package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracoes_financeiras")
public class ConfiguracaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chave", length = 100, nullable = false, unique = true)
    private String chave;

    @Column(name = "valor", length = 255)
    private String valor;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "tipo", length = 20, nullable = false)
    private String tipo; // STRING, NUMERIC, BOOLEAN, DECIMAL

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "usuario_criacao", length = 100)
    private String usuarioCriacao;

    // Construtor padrão
    public ConfiguracaoFinanceira() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Métodos utilitários
    public BigDecimal getValorAsDecimal() {
        try {
            return new BigDecimal(valor);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public Integer getValorAsInteger() {
        try {
            return Integer.parseInt(valor);
        } catch (Exception e) {
            return 0;
        }
    }

    public Boolean getValorAsBoolean() {
        return Boolean.parseBoolean(valor);
    }

    public void setValorFromDecimal(BigDecimal decimal) {
        this.valor = decimal.toString();
    }

    public void setValorFromInteger(Integer integer) {
        this.valor = integer.toString();
    }

    public void setValorFromBoolean(Boolean bool) {
        this.valor = bool.toString();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChave() { return chave; }
    public void setChave(String chave) { this.chave = chave; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public String getUsuarioCriacao() { return usuarioCriacao; }
    public void setUsuarioCriacao(String usuarioCriacao) { this.usuarioCriacao = usuarioCriacao; }
}
