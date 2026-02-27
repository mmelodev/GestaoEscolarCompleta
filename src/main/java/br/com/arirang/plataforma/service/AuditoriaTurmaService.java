package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.TurmaDTO;
import br.com.arirang.plataforma.entity.AuditoriaTurma;
import br.com.arirang.plataforma.repository.AuditoriaTurmaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AuditoriaTurmaService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditoriaTurmaService.class);
    
    @Autowired
    private AuditoriaTurmaRepository auditoriaTurmaRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Gera um protocolo único para a alteração
     * Formato: ALT-TUR-[ID]-[DDMMYYYY]-[HHMM]-[USUARIO]
     */
    public String gerarProtocolo(Long turmaId, String usuario) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HHmm");
        
        String data = now.format(dataFormatter);
        String hora = now.format(horaFormatter);
        String usuarioLimpo = usuario != null ? usuario.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() : "sistema";
        
        String protocolo = String.format("ALT-TUR-%d-%s-%s-%s", turmaId, data, hora, usuarioLimpo);
        logger.info("Protocolo gerado: {}", protocolo);
        return protocolo;
    }
    
    /**
     * Compara dois DTOs e retorna um mapa com as diferenças
     */
    private Map<String, Map<String, Object>> compararAlteracoes(TurmaDTO antes, TurmaDTO depois) {
        Map<String, Map<String, Object>> alteracoes = new HashMap<>();
        
        if (antes == null || depois == null) {
            return alteracoes;
        }
        
        // Comparar campos principais
        // Usar HashMap em vez de Map.of() para permitir valores null
        if (!equals(antes.nomeTurma(), depois.nomeTurma())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.nomeTurma());
            valores.put("depois", depois.nomeTurma());
            alteracoes.put("nomeTurma", valores);
        }
        
        if (!equals(antes.professorResponsavelId(), depois.professorResponsavelId())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.professorResponsavelId());
            valores.put("depois", depois.professorResponsavelId());
            alteracoes.put("professorResponsavelId", valores);
        }
        
        if (!equals(antes.nivelProficiencia(), depois.nivelProficiencia())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.nivelProficiencia());
            valores.put("depois", depois.nivelProficiencia());
            alteracoes.put("nivelProficiencia", valores);
        }
        
        if (!equals(antes.turno(), depois.turno())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.turno());
            valores.put("depois", depois.turno());
            alteracoes.put("turno", valores);
        }
        
        if (!equals(antes.formato(), depois.formato())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.formato());
            valores.put("depois", depois.formato());
            alteracoes.put("formato", valores);
        }
        
        if (!equals(antes.modalidade(), depois.modalidade())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.modalidade());
            valores.put("depois", depois.modalidade());
            alteracoes.put("modalidade", valores);
        }
        
        if (!equals(antes.inicioTurma(), depois.inicioTurma())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.inicioTurma());
            valores.put("depois", depois.inicioTurma());
            alteracoes.put("inicioTurma", valores);
        }
        
        if (!equals(antes.terminoTurma(), depois.terminoTurma())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.terminoTurma());
            valores.put("depois", depois.terminoTurma());
            alteracoes.put("terminoTurma", valores);
        }
        
        if (!equals(antes.situacaoTurma(), depois.situacaoTurma())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.situacaoTurma());
            valores.put("depois", depois.situacaoTurma());
            alteracoes.put("situacaoTurma", valores);
        }
        
        if (!equals(antes.cargaHorariaTotal(), depois.cargaHorariaTotal())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.cargaHorariaTotal());
            valores.put("depois", depois.cargaHorariaTotal());
            alteracoes.put("cargaHorariaTotal", valores);
        }
        
        if (!equals(antes.quantidadeAulas(), depois.quantidadeAulas())) {
            Map<String, Object> valores = new HashMap<>();
            valores.put("antes", antes.quantidadeAulas());
            valores.put("depois", depois.quantidadeAulas());
            alteracoes.put("quantidadeAulas", valores);
        }
        
        return alteracoes;
    }
    
    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    /**
     * Gera descrição legível das alterações
     */
    private String gerarDescricaoAlteracoes(Map<String, Map<String, Object>> alteracoes) {
        StringBuilder descricao = new StringBuilder();
        
        alteracoes.forEach((campo, valores) -> {
            Object antes = valores.get("antes");
            Object depois = valores.get("depois");
            
            String nomeCampo = traduzirCampo(campo);
            descricao.append(String.format("☐ %s: \"%s\" → \"%s\"\n", nomeCampo, 
                antes != null ? antes : "não informado", 
                depois != null ? depois : "não informado"));
        });
        
        return descricao.toString();
    }
    
    private String traduzirCampo(String campo) {
        Map<String, String> traducoes = new HashMap<>();
        traducoes.put("nomeTurma", "Nome");
        traducoes.put("professorResponsavelId", "Professor");
        traducoes.put("nivelProficiencia", "Nível");
        traducoes.put("turno", "Turno");
        traducoes.put("formato", "Formato");
        traducoes.put("modalidade", "Modalidade");
        traducoes.put("inicioTurma", "Data Início");
        traducoes.put("terminoTurma", "Data Término");
        traducoes.put("situacaoTurma", "Situação");
        traducoes.put("cargaHorariaTotal", "Carga Horária");
        traducoes.put("quantidadeAulas", "Quantidade de Aulas");
        return traducoes.getOrDefault(campo, campo);
    }
    
    /**
     * Registra uma alteração auditada
     * Usa REQUIRES_NEW para garantir que a auditoria seja persistida mesmo se a transação principal falhar
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditoriaTurma registrarAlteracao(
            Long turmaId,
            TurmaDTO antes,
            TurmaDTO depois,
            String usuario,
            String justificativa,
            HttpServletRequest request) {
        
        try {
            // Gerar protocolo
            String protocolo = gerarProtocolo(turmaId, usuario);
            
            // Comparar alterações
            Map<String, Map<String, Object>> alteracoes = compararAlteracoes(antes, depois);
            
            // Converter para JSON
            String alteracaoAntesJson = antes != null ? objectMapper.writeValueAsString(antes) : null;
            String alteracaoDepoisJson = depois != null ? objectMapper.writeValueAsString(depois) : null;
            String alteracoesDetalhadas = gerarDescricaoAlteracoes(alteracoes);
            
            // Criar registro de auditoria
            AuditoriaTurma auditoria = new AuditoriaTurma();
            auditoria.setTurmaId(turmaId);
            auditoria.setProtocolo(protocolo);
            auditoria.setAlteracaoAntes(alteracaoAntesJson);
            auditoria.setAlteracaoDepois(alteracaoDepoisJson);
            auditoria.setAlteracoesDetalhadas(alteracoesDetalhadas);
            auditoria.setUsuario(usuario);
            auditoria.setJustificativa(justificativa);
            auditoria.setDataAlteracao(LocalDateTime.now());
            
            if (request != null) {
                auditoria.setIpAddress(obterIpAddress(request));
                auditoria.setUserAgent(request.getHeader("User-Agent"));
            }
            
            AuditoriaTurma salva = auditoriaTurmaRepository.save(auditoria);
            logger.info("Alteração auditada registrada - Protocolo: {} - Turma ID: {}", protocolo, turmaId);
            
            return salva;
        } catch (Exception e) {
            logger.error("Erro ao registrar auditoria para turma ID {}: {}", turmaId, e.getMessage(), e);
            // Não lançar exceção para não causar rollback da transação principal
            // A auditoria é importante mas não deve impedir a atualização da turma
            return null;
        }
    }
    
    private String obterIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * Busca histórico de alterações de uma turma
     */
    @Transactional(readOnly = true)
    public List<AuditoriaTurma> buscarHistorico(Long turmaId) {
        return auditoriaTurmaRepository.findHistoricoCompleto(turmaId);
    }
    
    /**
     * Busca auditoria por protocolo
     */
    @Transactional(readOnly = true)
    public Optional<AuditoriaTurma> buscarPorProtocolo(String protocolo) {
        return auditoriaTurmaRepository.findByProtocolo(protocolo);
    }
    
    /**
     * Verifica se há múltiplas alterações no mesmo dia
     */
    @Transactional(readOnly = true)
    public boolean temMultiplasAlteracoesHoje(Long turmaId) {
        Long count = auditoriaTurmaRepository.countAlteracoesHoje(turmaId);
        return count != null && count > 1;
    }
}

