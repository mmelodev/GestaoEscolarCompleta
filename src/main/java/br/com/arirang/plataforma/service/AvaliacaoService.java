package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.AvaliacaoDTO;
import br.com.arirang.plataforma.dto.NotaAvaliacaoDTO;
import br.com.arirang.plataforma.entity.Avaliacao;
import br.com.arirang.plataforma.entity.NotaAvaliacao;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.repository.AvaliacaoRepository;
import br.com.arirang.plataforma.repository.NotaAvaliacaoRepository;
import br.com.arirang.plataforma.repository.TurmaRepository;
import br.com.arirang.plataforma.repository.AlunoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AvaliacaoService {
    
    private static final Logger logger = LoggerFactory.getLogger(AvaliacaoService.class);
    
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    
    @Autowired
    private NotaAvaliacaoRepository notaAvaliacaoRepository;
    
    @Autowired
    private TurmaRepository turmaRepository;
    
    @Autowired
    private AlunoRepository alunoRepository;
    
    public List<AvaliacaoDTO> listarTodasAvaliacoes() {
        logger.info("Listando todas as avaliações");
        try {
            List<Avaliacao> avaliacoes = avaliacaoRepository.findAll();
            logger.info("Encontradas {} avaliações", avaliacoes.size());
            return avaliacoes.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar avaliações: {}", e.getMessage(), e);
            return List.of(); // Retorna lista vazia em caso de erro
        }
    }
    
    public List<AvaliacaoDTO> listarAvaliacoesPorTurma(Long turmaId) {
        logger.info("Listando avaliações da turma: {}", turmaId);
        try {
            List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaIdAndAtivaTrue(turmaId);
            logger.info("Encontradas {} avaliações para a turma {}", avaliacoes.size(), turmaId);
            return avaliacoes.stream().map(this::convertToDTO).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar avaliações da turma {}: {}", turmaId, e.getMessage(), e);
            return List.of(); // Retorna lista vazia em caso de erro
        }
    }
    
    public Optional<AvaliacaoDTO> buscarAvaliacaoPorId(Long id) {
        logger.info("Buscando avaliação por ID: {}", id);
        return avaliacaoRepository.findByIdWithTurma(id).map(this::convertToDTO);
    }
    
    public AvaliacaoDTO criarAvaliacao(AvaliacaoDTO avaliacaoDTO) {
        logger.info("Criando nova avaliação: {}", avaliacaoDTO.nomeAvaliacao());
        
        Turma turma = turmaRepository.findById(avaliacaoDTO.turmaId())
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setNomeAvaliacao(avaliacaoDTO.nomeAvaliacao());
        avaliacao.setDescricao(avaliacaoDTO.descricao());
        avaliacao.setTipoAvaliacao(avaliacaoDTO.tipoAvaliacao());
        avaliacao.setTurma(turma);
        avaliacao.setDataAvaliacao(avaliacaoDTO.dataAvaliacao());
        avaliacao.setPeso(avaliacaoDTO.peso());
        avaliacao.setValorMaximo(avaliacaoDTO.valorMaximo());
        avaliacao.setAtiva(avaliacaoDTO.ativa());
        
        Avaliacao savedAvaliacao = avaliacaoRepository.save(avaliacao);
        
        // Criar notas para todos os alunos da turma
        List<Aluno> alunosDaTurma = alunoRepository.findByTurmasContaining(turma);
        for (Aluno aluno : alunosDaTurma) {
            NotaAvaliacao nota = new NotaAvaliacao();
            nota.setAvaliacao(savedAvaliacao);
            nota.setAluno(aluno);
            nota.setPresente(true);
            notaAvaliacaoRepository.save(nota);
        }
        
        return convertToDTO(savedAvaliacao);
    }
    
    public AvaliacaoDTO atualizarAvaliacao(Long id, AvaliacaoDTO avaliacaoDTO) {
        logger.info("Atualizando avaliação ID: {}", id);
        
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
        
        Turma turma = turmaRepository.findById(avaliacaoDTO.turmaId())
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        
        avaliacao.setNomeAvaliacao(avaliacaoDTO.nomeAvaliacao());
        avaliacao.setDescricao(avaliacaoDTO.descricao());
        avaliacao.setTipoAvaliacao(avaliacaoDTO.tipoAvaliacao());
        avaliacao.setTurma(turma);
        avaliacao.setDataAvaliacao(avaliacaoDTO.dataAvaliacao());
        avaliacao.setPeso(avaliacaoDTO.peso());
        avaliacao.setValorMaximo(avaliacaoDTO.valorMaximo());
        avaliacao.setAtiva(avaliacaoDTO.ativa());
        
        Avaliacao updatedAvaliacao = avaliacaoRepository.save(avaliacao);
        return convertToDTO(updatedAvaliacao);
    }
    
    public void deletarAvaliacao(Long id) {
        logger.info("Deletando avaliação ID: {}", id);
        avaliacaoRepository.deleteById(id);
    }
    
    public List<NotaAvaliacaoDTO> listarNotasPorAvaliacao(Long avaliacaoId) {
        logger.info("Listando notas da avaliação: {}", avaliacaoId);
        List<NotaAvaliacao> notas = notaAvaliacaoRepository.findByAvaliacaoIdWithAluno(avaliacaoId);
        return notas.stream().map(this::convertNotaToDTO).collect(Collectors.toList());
    }
    
    public NotaAvaliacaoDTO atualizarNotaAvaliacao(Long notaId, NotaAvaliacaoDTO notaDTO) {
        logger.info("Atualizando nota ID: {}", notaId);
        
        NotaAvaliacao nota = notaAvaliacaoRepository.findById(notaId)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada"));
        
        nota.setValorNota(notaDTO.valorNota());
        nota.setObservacoes(notaDTO.observacoes());
        nota.setPresente(notaDTO.presente());
        
        NotaAvaliacao updatedNota = notaAvaliacaoRepository.save(nota);
        return convertNotaToDTO(updatedNota);
    }
    
    public Long contarAvaliacoesAtivas() {
        return avaliacaoRepository.countByAtivaTrue();
    }
    
    public Long contarAvaliacoesFinalizadas() {
        return avaliacaoRepository.countByAtivaFalse();
    }
    
    public Long contarTotalAvaliacoes() {
        return avaliacaoRepository.count();
    }
    
    private AvaliacaoDTO convertToDTO(Avaliacao avaliacao) {
        try {
            List<NotaAvaliacaoDTO> notasDTO = avaliacao.getNotas() != null ?
                    avaliacao.getNotas().stream()
                            .map(this::convertNotaToDTO)
                            .collect(Collectors.toList()) :
                    List.of();
            
            Long turmaId = avaliacao.getTurma() != null ? avaliacao.getTurma().getId() : null;
            String nomeTurma = avaliacao.getTurma() != null ? avaliacao.getTurma().getNomeTurma() : "Turma não encontrada";
            
            return new AvaliacaoDTO(
                    avaliacao.getId(),
                    avaliacao.getNomeAvaliacao(),
                    avaliacao.getDescricao(),
                    avaliacao.getTipoAvaliacao(),
                    turmaId,
                    nomeTurma,
                    avaliacao.getDataAvaliacao(),
                    avaliacao.getDataCriacao(),
                    avaliacao.getDataAtualizacao(),
                    avaliacao.getPeso(),
                    avaliacao.getValorMaximo(),
                    avaliacao.getAtiva(),
                    notasDTO
            );
        } catch (Exception e) {
            logger.error("Erro ao converter avaliação para DTO: {}", e.getMessage(), e);
            // Retorna um DTO básico em caso de erro
            return new AvaliacaoDTO(
                    avaliacao.getId(),
                    avaliacao.getNomeAvaliacao() != null ? avaliacao.getNomeAvaliacao() : "Nome não disponível",
                    avaliacao.getDescricao(),
                    avaliacao.getTipoAvaliacao(),
                    null,
                    "Turma não disponível",
                    avaliacao.getDataAvaliacao(),
                    avaliacao.getDataCriacao(),
                    avaliacao.getDataAtualizacao(),
                    avaliacao.getPeso(),
                    avaliacao.getValorMaximo(),
                    avaliacao.getAtiva(),
                    List.of()
            );
        }
    }
    
    private NotaAvaliacaoDTO convertNotaToDTO(NotaAvaliacao nota) {
        return new NotaAvaliacaoDTO(
                nota.getId(),
                nota.getAvaliacao().getId(),
                nota.getAluno().getId(),
                nota.getAluno().getNomeCompleto(),
                nota.getValorNota(),
                nota.getObservacoes(),
                nota.getDataLancamento(),
                nota.getDataAtualizacao(),
                nota.getPresente()
        );
    }
}