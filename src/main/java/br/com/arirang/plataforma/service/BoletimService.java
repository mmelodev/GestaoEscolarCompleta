package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.BoletimDTO;
import br.com.arirang.plataforma.dto.NotaDTO;
import br.com.arirang.plataforma.entity.*;
import br.com.arirang.plataforma.repository.BoletimRepository;
import br.com.arirang.plataforma.repository.NotaRepository;
import br.com.arirang.plataforma.repository.TurmaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoletimService {

    private static final Logger logger = LoggerFactory.getLogger(BoletimService.class);

    @Autowired
    private BoletimRepository boletimRepository;

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private TurmaRepository turmaRepository;
    
    @Autowired
    private PadraoBoletimService padraoBoletimService;

    @Transactional
    public Boletim criarBoletim(Long alunoId, Long turmaId) {
        Aluno aluno = alunoService.buscarAlunoPorId(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

        // Verificar se já existe boletim para este aluno nesta turma
        Optional<Boletim> boletimExistente = boletimRepository.findByAlunoIdAndTurmaId(alunoId, turmaId);
        if (boletimExistente.isPresent()) {
            Boletim boletim = boletimExistente.get();
            // Forçar inicialização do aluno e turma dentro da transação
            if (boletim.getAluno() != null) {
                boletim.getAluno().getId();
                boletim.getAluno().getNomeCompleto();
            }
            if (boletim.getTurma() != null) {
                boletim.getTurma().getId();
                boletim.getTurma().getNomeTurma();
            }
            // Aplicar padrão mesmo se o boletim já existir (para notas que ainda não existem)
            aplicarPadraoSeExistir(boletim, turmaId);
            // Forçar flush para garantir que as notas sejam persistidas
            boletimRepository.flush();
            // Recarregar com JOIN FETCH para garantir que todas as notas estejam carregadas
            return boletimRepository.findByAlunoIdAndTurmaId(alunoId, turmaId)
                    .orElse(boletim);
        }

        Boletim boletim = new Boletim();
        boletim.setAluno(aluno);
        boletim.setTurma(turma);
        boletim.setDataLancamento(LocalDateTime.now());
        boletim.setSituacaoFinal(SituacaoFinal.EM_ANDAMENTO);

        Boletim savedBoletim = boletimRepository.saveAndFlush(boletim);
        
        // Aplicar padrão da turma se existir
        aplicarPadraoSeExistir(savedBoletim, turmaId);
        
        // Forçar flush para garantir que as notas sejam persistidas
        boletimRepository.flush();
        
        // Recarregar com JOIN FETCH para garantir que aluno, turma e notas estejam carregados
        return boletimRepository.findByAlunoIdAndTurmaId(alunoId, turmaId)
                .orElse(savedBoletim);
    }
    
    /**
     * Aplica o padrão de boletim da turma, se existir
     * Cria notas padrão para cada tipo que tiver descrição definida no padrão
     */
    private void aplicarPadraoSeExistir(Boletim boletim, Long turmaId) {
        logger.debug("Aplicando padrão para boletim ID {} da turma ID {}", boletim.getId(), turmaId);
        Optional<PadraoBoletim> padraoOpt = padraoBoletimService.buscarPadraoPorTurmaIdEntity(turmaId);
        if (padraoOpt.isEmpty()) {
            logger.debug("Nenhum padrão encontrado para turma ID {}", turmaId);
            return;
        }
        
        PadraoBoletim padrao = padraoOpt.get();
        logger.debug("Padrão encontrado para turma ID {}, aplicando notas padrão", turmaId);
        
        int notasCriadas = 0;
        // Criar notas padrão para cada tipo que tiver descrição definida
        for (TipoNota tipoNota : TipoNota.values()) {
            String descricaoPadrao = padrao.getDescricaoPorTipo(tipoNota);
            if (descricaoPadrao != null && !descricaoPadrao.trim().isEmpty()) {
                // Verificar se já existe nota deste tipo
                boolean jaExiste = notaRepository.findByBoletimId(boletim.getId()).stream()
                        .anyMatch(nota -> nota.getTipoNota() == tipoNota);
                
                if (!jaExiste) {
                    Nota nota = new Nota();
                    nota.setBoletim(boletim);
                    nota.setTipoNota(tipoNota);
                    nota.setDescricao(descricaoPadrao);
                    nota.setValorNota(1); // Valor inicial mínimo (usuário pode alterar depois)
                    notaRepository.saveAndFlush(nota);
                    notasCriadas++;
                    logger.debug("Nota padrão criada: tipo {} - descrição: {}", tipoNota, descricaoPadrao);
                } else {
                    logger.debug("Nota do tipo {} já existe para boletim ID {}", tipoNota, boletim.getId());
                }
            }
        }
        
        logger.debug("Padrão aplicado: {} notas criadas para boletim ID {}", notasCriadas, boletim.getId());
        
        // Recalcular média após adicionar notas padrão
        if (notasCriadas > 0) {
            calcularMediaFinal(boletim.getId());
        }
    }

    @Transactional
    public Boletim adicionarNota(Long boletimId, NotaDTO notaDTO) {
        Boletim boletim = boletimRepository.findById(boletimId)
                .orElseThrow(() -> new RuntimeException("Boletim não encontrado"));

        if (boletim.isFinalizado()) {
            throw new RuntimeException("Não é possível adicionar notas a um boletim finalizado");
        }

        Nota nota = new Nota();
        nota.setBoletim(boletim);
        nota.setTipoNota(notaDTO.tipoNota());
        nota.setDescricao(notaDTO.descricao());
        nota.setValorNota(notaDTO.valorNota());

        notaRepository.save(nota);
        
        // Recalcular média
        calcularMediaFinal(boletimId);
        
        return boletimRepository.findById(boletimId).orElse(boletim);
    }

    @Transactional
    public Boletim removerNota(Long notaId) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada"));
        
        Boletim boletim = nota.getBoletim();
        
        if (boletim.isFinalizado()) {
            throw new RuntimeException("Não é possível remover notas de um boletim finalizado");
        }

        notaRepository.delete(nota);
        
        // Recalcular média
        calcularMediaFinal(boletim.getId());
        
        return boletimRepository.findById(boletim.getId()).orElse(boletim);
    }

    @Transactional
    public Boletim finalizarBoletim(Long boletimId) {
        Boletim boletim = boletimRepository.findById(boletimId)
                .orElseThrow(() -> new RuntimeException("Boletim não encontrado"));

        if (boletim.isFinalizado()) {
            throw new RuntimeException("Boletim já está finalizado");
        }

        // Verificar se todas as notas obrigatórias estão presentes
        List<Nota> notas = notaRepository.findByBoletimId(boletimId);
        if (notas.size() < 9) { // 9 tipos de nota obrigatórios
            throw new RuntimeException("Existem campos a serem preenchidos");
        }

        // Calcular média final
        calcularMediaFinal(boletimId);
        
        // Atualizar boletim
        boletim = boletimRepository.findById(boletimId).orElse(boletim);
        boletim.setFinalizado(true);
        // Critério de aprovação: média >= 7.0 (escala de 0 a 10)
        boletim.setSituacaoFinal(boletim.getMediaFinal() != null && boletim.getMediaFinal() >= 7.0 ? SituacaoFinal.APROVADO : SituacaoFinal.REPROVADO);

        return boletimRepository.save(boletim);
    }
    
    /**
     * Salva o boletim (força persistência sem finalizar)
     * Útil para dar feedback visual ao usuário de que o boletim foi salvo
     */
    @Transactional
    public Boletim salvarBoletim(Long boletimId) {
        Boletim boletim = boletimRepository.findById(boletimId)
                .orElseThrow(() -> new RuntimeException("Boletim não encontrado"));
        
        if (boletim.isFinalizado()) {
            throw new RuntimeException("Não é possível salvar um boletim finalizado");
        }
        
        // Recalcular média
        calcularMediaFinal(boletimId);
        
        // Salvar boletim (pode não ter mudanças, mas força persistência)
        return boletimRepository.save(boletim);
    }

    @Transactional(readOnly = true)
    public Optional<Boletim> buscarBoletimPorId(Long id) {
        Optional<Boletim> boletimOpt = boletimRepository.findByIdWithAlunoAndTurma(id);
        // Forçar inicialização do aluno e turma dentro da transação
        boletimOpt.ifPresent(boletim -> {
            if (boletim.getAluno() != null) {
                boletim.getAluno().getId();
                boletim.getAluno().getNomeCompleto();
            }
            if (boletim.getTurma() != null) {
                boletim.getTurma().getId();
                boletim.getTurma().getNomeTurma();
            }
        });
        return boletimOpt;
    }

    @Transactional(readOnly = true)
    public Optional<BoletimDTO> buscarBoletimPorIdAsDTO(Long id) {
        return buscarBoletimPorId(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public Optional<Boletim> buscarBoletimPorAlunoETurma(Long alunoId, Long turmaId) {
        Optional<Boletim> boletimOpt = boletimRepository.findByAlunoIdAndTurmaId(alunoId, turmaId);
        // Forçar inicialização do aluno e turma dentro da transação
        boletimOpt.ifPresent(boletim -> {
            if (boletim.getAluno() != null) {
                boletim.getAluno().getId(); // Forçar inicialização
                boletim.getAluno().getNomeCompleto(); // Forçar inicialização
            }
            if (boletim.getTurma() != null) {
                boletim.getTurma().getId(); // Forçar inicialização
                boletim.getTurma().getNomeTurma(); // Forçar inicialização
            }
            // Aplicar padrão mesmo ao buscar boletim existente (para notas que ainda não existem)
            aplicarPadraoSeExistir(boletim, turmaId);
            // Forçar flush para garantir que as notas sejam persistidas
            boletimRepository.flush();
        });
        // Recarregar com JOIN FETCH para garantir que todas as notas estejam carregadas
        if (boletimOpt.isPresent()) {
            return boletimRepository.findByAlunoIdAndTurmaId(alunoId, turmaId);
        }
        return boletimOpt;
    }

    @Transactional
    public Optional<BoletimDTO> buscarBoletimPorAlunoETurmaAsDTO(Long alunoId, Long turmaId) {
        return buscarBoletimPorAlunoETurma(alunoId, turmaId)
                .map(this::convertToDTO);
    }

    @Transactional
    public BoletimDTO criarBoletimAsDTO(Long alunoId, Long turmaId) {
        Boletim boletim = criarBoletim(alunoId, turmaId);
        return convertToDTO(boletim);
    }

    @Transactional(readOnly = true)
    public List<Boletim> buscarBoletinsPorTurma(Long turmaId) {
        List<Boletim> boletins = boletimRepository.findByTurmaId(turmaId);
        // Forçar inicialização de aluno e turma dentro da transação
        boletins.forEach(boletim -> {
            if (boletim.getAluno() != null) {
                boletim.getAluno().getId();
                boletim.getAluno().getNomeCompleto();
            }
            if (boletim.getTurma() != null) {
                boletim.getTurma().getId();
                boletim.getTurma().getNomeTurma();
            }
        });
        return boletins;
    }

    @Transactional(readOnly = true)
    public List<Boletim> buscarBoletinsPorAluno(Long alunoId) {
        List<Boletim> boletins = boletimRepository.findByAlunoId(alunoId);
        // Forçar inicialização de aluno e turma dentro da transação
        boletins.forEach(boletim -> {
            if (boletim.getAluno() != null) {
                boletim.getAluno().getId();
                boletim.getAluno().getNomeCompleto();
            }
            if (boletim.getTurma() != null) {
                boletim.getTurma().getId();
                boletim.getTurma().getNomeTurma();
            }
        });
        return boletins;
    }

    @Transactional(readOnly = true)
    public List<BoletimDTO> buscarBoletinsPorTurmaAsDTO(Long turmaId) {
        return buscarBoletinsPorTurma(turmaId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BoletimDTO> buscarBoletinsPorAlunoAsDTO(Long alunoId) {
        return buscarBoletinsPorAluno(alunoId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean podeFecharTurma(Long turmaId) {
        Long totalAlunos = boletimRepository.countAlunosByTurmaId(turmaId);
        Long boletinsFinalizados = boletimRepository.countBoletinsFinalizadosByTurmaId(turmaId);
        
        return totalAlunos.equals(boletinsFinalizados);
    }

    @Transactional(readOnly = true)
    public Long contarTotalBoletins() {
        return boletimRepository.countAllBoletins();
    }

    @Transactional(readOnly = true)
    public Long contarBoletinsPendentes() {
        return boletimRepository.countBoletinsPendentes();
    }

    @Transactional(readOnly = true)
    public Long contarBoletinsFinalizados() {
        return boletimRepository.countBoletinsFinalizados();
    }

    @Transactional(readOnly = true)
    public Double calcularMediaGeral() {
        List<Boletim> boletinsFinalizados = boletimRepository.findAll().stream()
                .filter(Boletim::isFinalizado)
                .filter(b -> b.getMediaFinal() != null)
                .collect(Collectors.toList());
        
        if (boletinsFinalizados.isEmpty()) {
            return 0.0;
        }
        
        double soma = boletinsFinalizados.stream()
                .mapToDouble(b -> b.getMediaFinal() != null ? b.getMediaFinal() : 0.0)
                .sum();
        
        return Math.round((soma / boletinsFinalizados.size()) * 100.0) / 100.0;
    }

    @Transactional(readOnly = true)
    public Double calcularMediaPorTurma(Long turmaId) {
        List<Boletim> boletins = buscarBoletinsPorTurma(turmaId).stream()
                .filter(Boletim::isFinalizado)
                .filter(b -> b.getMediaFinal() != null)
                .collect(Collectors.toList());
        
        if (boletins.isEmpty()) {
            return 0.0;
        }
        
        double soma = boletins.stream()
                .mapToDouble(b -> b.getMediaFinal() != null ? b.getMediaFinal() : 0.0)
                .sum();
        
        return Math.round((soma / boletins.size()) * 100.0) / 100.0;
    }

    @Transactional(readOnly = true)
    public Double calcularMediaPorAluno(Long alunoId) {
        List<Boletim> boletins = buscarBoletinsPorAluno(alunoId).stream()
                .filter(Boletim::isFinalizado)
                .filter(b -> b.getMediaFinal() != null)
                .collect(Collectors.toList());
        
        if (boletins.isEmpty()) {
            return 0.0;
        }
        
        double soma = boletins.stream()
                .mapToDouble(b -> b.getMediaFinal() != null ? b.getMediaFinal() : 0.0)
                .sum();
        
        return Math.round((soma / boletins.size()) * 100.0) / 100.0;
    }

    @Transactional(readOnly = true)
    public Long contarAprovados() {
        return boletimRepository.findAll().stream()
                .filter(Boletim::isFinalizado)
                .filter(b -> b.getSituacaoFinal() == SituacaoFinal.APROVADO)
                .count();
    }

    @Transactional(readOnly = true)
    public Long contarReprovados() {
        return boletimRepository.findAll().stream()
                .filter(Boletim::isFinalizado)
                .filter(b -> b.getSituacaoFinal() == SituacaoFinal.REPROVADO)
                .count();
    }

    @Transactional(readOnly = true)
    public Long contarAprovadosPorTurma(Long turmaId) {
        return buscarBoletinsPorTurma(turmaId).stream()
                .filter(Boletim::isFinalizado)
                .filter(b -> b.getSituacaoFinal() == SituacaoFinal.APROVADO)
                .count();
    }

    @Transactional(readOnly = true)
    public Long contarReprovadosPorTurma(Long turmaId) {
        return buscarBoletinsPorTurma(turmaId).stream()
                .filter(Boletim::isFinalizado)
                .filter(b -> b.getSituacaoFinal() == SituacaoFinal.REPROVADO)
                .count();
    }

    @Transactional(readOnly = true)
    public Double calcularTaxaAprovacaoPorTurma(Long turmaId) {
        List<Boletim> boletinsFinalizados = buscarBoletinsPorTurma(turmaId).stream()
                .filter(Boletim::isFinalizado)
                .collect(Collectors.toList());
        
        if (boletinsFinalizados.isEmpty()) {
            return 0.0;
        }
        
        long aprovados = contarAprovadosPorTurma(turmaId);
        return Math.round((aprovados * 100.0 / boletinsFinalizados.size()) * 100.0) / 100.0;
    }

    @Transactional
    public Boletim atualizarMediaFinal(Long boletimId, Double mediaFinal) {
        Boletim boletim = boletimRepository.findById(boletimId)
                .orElseThrow(() -> new RuntimeException("Boletim não encontrado"));
        
        boletim.setMediaFinal(mediaFinal);
        
        // Recalcular situação final baseada na nova média (critério: >= 7.0 para aprovação)
        if (boletim.isFinalizado() && mediaFinal != null) {
            boletim.setSituacaoFinal(mediaFinal >= 7.0 ? SituacaoFinal.APROVADO : SituacaoFinal.REPROVADO);
        }
        
        return boletimRepository.save(boletim);
    }

    private void calcularMediaFinal(Long boletimId) {
        List<Nota> notas = notaRepository.findByBoletimId(boletimId);
        
        if (notas.isEmpty()) {
            return;
        }

        double soma = notas.stream()
                .mapToInt(Nota::getValorNota)
                .sum();
        
        double media = soma / notas.size();
        
        Boletim boletim = boletimRepository.findById(boletimId).orElse(null);
        if (boletim != null) {
            boletim.setMediaFinal(Math.round(media * 100.0) / 100.0);
            boletimRepository.save(boletim);
        }
    }

    @Transactional(readOnly = true)
    public BoletimDTO convertToDTO(Boletim boletim) {
        // Garantir que aluno e turma estejam inicializados
        if (boletim.getAluno() != null) {
            boletim.getAluno().getId();
            boletim.getAluno().getNomeCompleto();
        }
        if (boletim.getTurma() != null) {
            boletim.getTurma().getId();
            boletim.getTurma().getNomeTurma();
        }
        
        List<NotaDTO> notasDTO = boletim.getNotas() != null ? 
                boletim.getNotas().stream()
                        .map(n -> new NotaDTO(n.getId(), n.getTipoNota(), n.getDescricao(), n.getValorNota()))
                        .collect(Collectors.toList()) : 
                List.of();

        return new BoletimDTO(
                boletim.getId(),
                boletim.getAluno() != null ? boletim.getAluno().getId() : null,
                boletim.getAluno() != null ? boletim.getAluno().getNomeCompleto() : null,
                boletim.getTurma() != null ? boletim.getTurma().getId() : null,
                boletim.getTurma() != null ? boletim.getTurma().getNomeTurma() : null,
                notasDTO,
                boletim.getMediaFinal(),
                boletim.getSituacaoFinal() != null ? boletim.getSituacaoFinal().name() : null,
                boletim.getDataLancamento(),
                boletim.isFinalizado()
        );
    }
}
