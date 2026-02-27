package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade para armazenar configurações de personalização do usuário
 * Permite que cada usuário customize cores, logo e outros elementos visuais
 */
@Entity
@Table(name = "configuracoes_usuario")
public class ConfiguracaoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // Cores do tema
    @Column(name = "cor_primaria", length = 7)
    private String corPrimaria; // Hex color (ex: #01004e)

    @Column(name = "cor_secundaria", length = 7)
    private String corSecundaria; // Hex color (ex: #860213)

    @Column(name = "cor_header", length = 7)
    private String corHeader; // Cor do header (ex: #1a1a1a)

    @Column(name = "cor_texto", length = 7)
    private String corTexto; // Cor do texto principal

    @Column(name = "cor_destaque", length = 7)
    private String corDestaque; // Cor de destaque/links (ex: #007bff)

    // Logo personalizado
    @Column(name = "logo_url", length = 500)
    private String logoUrl; // URL do logo personalizado

    @Column(name = "logo_alt", length = 100)
    private String logoAlt; // Texto alternativo do logo

    // Configurações adicionais
    @Column(name = "fonte_tamanho_base")
    private Integer fonteTamanhoBase; // Tamanho base da fonte em px

    @Column(name = "borda_arredondada")
    private Boolean bordaArredondada; // Se prefere bordas arredondadas

    @Column(name = "tema_escuro")
    private Boolean temaEscuro; // Se prefere tema escuro

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Construtor padrão
    public ConfiguracaoUsuario() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        // Valores padrão
        this.corPrimaria = "#01004e";
        this.corSecundaria = "#860213";
        this.corHeader = "#1a1a1a";
        this.corTexto = "#f5f5f5";
        this.corDestaque = "#007bff";
        this.fonteTamanhoBase = 16;
        this.bordaArredondada = true;
        this.temaEscuro = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getCorPrimaria() {
        return corPrimaria;
    }

    public void setCorPrimaria(String corPrimaria) {
        this.corPrimaria = corPrimaria;
    }

    public String getCorSecundaria() {
        return corSecundaria;
    }

    public void setCorSecundaria(String corSecundaria) {
        this.corSecundaria = corSecundaria;
    }

    public String getCorHeader() {
        return corHeader;
    }

    public void setCorHeader(String corHeader) {
        this.corHeader = corHeader;
    }

    public String getCorTexto() {
        return corTexto;
    }

    public void setCorTexto(String corTexto) {
        this.corTexto = corTexto;
    }

    public String getCorDestaque() {
        return corDestaque;
    }

    public void setCorDestaque(String corDestaque) {
        this.corDestaque = corDestaque;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLogoAlt() {
        return logoAlt;
    }

    public void setLogoAlt(String logoAlt) {
        this.logoAlt = logoAlt;
    }

    public Integer getFonteTamanhoBase() {
        return fonteTamanhoBase;
    }

    public void setFonteTamanhoBase(Integer fonteTamanhoBase) {
        this.fonteTamanhoBase = fonteTamanhoBase;
    }

    public Boolean getBordaArredondada() {
        return bordaArredondada;
    }

    public void setBordaArredondada(Boolean bordaArredondada) {
        this.bordaArredondada = bordaArredondada;
    }

    public Boolean getTemaEscuro() {
        return temaEscuro;
    }

    public void setTemaEscuro(Boolean temaEscuro) {
        this.temaEscuro = temaEscuro;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
