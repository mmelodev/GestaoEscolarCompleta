package br.com.arirang.plataforma.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_turma")
public class AuditoriaTurma {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "turma_id", nullable = false)
    private Long turmaId;
    
    @Column(name = "protocolo", nullable = false, unique = true, length = 100)
    private String protocolo;
    
    @Column(name = "alteracao_antes", columnDefinition = "TEXT")
    private String alteracaoAntes; // JSON com estado anterior
    
    @Column(name = "alteracao_depois", columnDefinition = "TEXT")
    private String alteracaoDepois; // JSON com estado novo
    
    @Column(name = "alteracoes_detalhadas", columnDefinition = "TEXT")
    private String alteracoesDetalhadas; // Descrição legível das alterações
    
    @Column(name = "usuario", length = 100)
    private String usuario;
    
    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;
    
    @Column(name = "justificativa", nullable = false, length = 500)
    private String justificativa;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 255)
    private String userAgent;
    
    public AuditoriaTurma() {
        this.dataAlteracao = LocalDateTime.now();
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getTurmaId() { return turmaId; }
    public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }
    
    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }
    
    public String getAlteracaoAntes() { return alteracaoAntes; }
    public void setAlteracaoAntes(String alteracaoAntes) { this.alteracaoAntes = alteracaoAntes; }
    
    public String getAlteracaoDepois() { return alteracaoDepois; }
    public void setAlteracaoDepois(String alteracaoDepois) { this.alteracaoDepois = alteracaoDepois; }
    
    public String getAlteracoesDetalhadas() { return alteracoesDetalhadas; }
    public void setAlteracoesDetalhadas(String alteracoesDetalhadas) { this.alteracoesDetalhadas = alteracoesDetalhadas; }
    
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    
    public LocalDateTime getDataAlteracao() { return dataAlteracao; }
    public void setDataAlteracao(LocalDateTime dataAlteracao) { this.dataAlteracao = dataAlteracao; }
    
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}

