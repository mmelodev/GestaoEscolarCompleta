package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.TurmaDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Boletim;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.entity.AuditoriaTurma;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.entity.Professor;
import br.com.arirang.plataforma.repository.AlunoRepository;
import br.com.arirang.plataforma.repository.BoletimRepository;
import br.com.arirang.plataforma.repository.ProfessorRepository;
import br.com.arirang.plataforma.repository.TurmaRepository;
import br.com.arirang.plataforma.repository.ContratoRepository;
import br.com.arirang.plataforma.service.AuditoriaTurmaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TurmaService {

    private static final Logger logger = LoggerFactory.getLogger(TurmaService.class);

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private BoletimRepository boletimRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private ContratoRepository contratoRepository;
    
    @Autowired
    private AuditoriaTurmaService auditoriaTurmaService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "turmasLista", allEntries = true)
    }, put = {
            @CachePut(value = "turmasPorId", key = "#result.id")
    })
    public Turma criarTurma(TurmaDTO turmaDTO) {
        try {
            // Validar que o calendário PDF é obrigatório para criar/ativar turma
            // Permite valor temporário "PENDING_UPLOAD" que será substituído após upload do arquivo
            String calendarioPdf = turmaDTO.calendarioPdf();
            if (calendarioPdf == null || 
                (!"PENDING_UPLOAD".equals(calendarioPdf) && calendarioPdf.trim().isEmpty())) {
                throw new BusinessException("Calendário PDF é obrigatório para criar uma turma. Por favor, faça o upload do calendário.");
            }
            
            Turma turma = new Turma();
            turma.setNomeTurma(turmaDTO.nomeTurma());
            turma.setIdioma(turmaDTO.idioma() != null && !turmaDTO.idioma().trim().isEmpty() 
                ? turmaDTO.idioma().trim() : null);
            turma.setNivelProficiencia(turmaDTO.nivelProficiencia());
            turma.setDiaTurma(turmaDTO.diaTurma());
            turma.setTurno(turmaDTO.turno());
            turma.setFormato(turmaDTO.formato());
            turma.setModalidade(turmaDTO.modalidade());
            turma.setRealizador(turmaDTO.realizador());
            turma.setHoraInicio(turmaDTO.horaInicio());
            turma.setHoraTermino(turmaDTO.horaTermino());
            turma.setAnoSemestre(turmaDTO.anoSemestre());
            turma.setCargaHorariaTotal(turmaDTO.cargaHorariaTotal());
            turma.setQuantidadeAulas(turmaDTO.quantidadeAulas());
            turma.setCalendarioPdf(turmaDTO.calendarioPdf());
            turma.setInicioTurma(turmaDTO.inicioTurma());
            turma.setTerminoTurma(turmaDTO.terminoTurma());
            turma.setSituacaoTurma(turmaDTO.situacaoTurma());

            // Associar professor responsável se fornecido
            if (turmaDTO.professorResponsavelId() != null) {
                Professor professor = professorRepository.findById(turmaDTO.professorResponsavelId())
                        .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado com ID: " + turmaDTO.professorResponsavelId()));
                turma.setProfessorResponsavel(professor);
            }

            if (turmaDTO.alunoIds() != null && !turmaDTO.alunoIds().isEmpty()) {
                List<Aluno> alunos = alunoRepository.findAllById(turmaDTO.alunoIds());
                turma.setAlunos(alunos);
            }

            // Salvar e fazer flush para garantir persistência imediata
            Turma savedTurma = turmaRepository.saveAndFlush(turma);
            // Recarregar com fetch join para garantir que alunos estejam carregados
            savedTurma = turmaRepository.findByIdWithAlunos(savedTurma.getId())
                    .orElse(savedTurma);
            
            logger.info("Turma criada com ID: {} - Nome: {} - Nível: {} - Turno: {} - Formato: {} - Modalidade: {}", 
                    savedTurma.getId(), 
                    savedTurma.getNomeTurma(),
                    savedTurma.getNivelProficiencia(),
                    savedTurma.getTurno(),
                    savedTurma.getFormato(),
                    savedTurma.getModalidade());
            return savedTurma;
        } catch (Exception e) {
            logger.error("Erro ao criar turma: ", e);
            throw new BusinessException("Erro ao criar turma: " + e.getMessage());
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "turmasLista", allEntries = true),
            @CacheEvict(value = "turmasPorId", key = "#id")
    })
    public Turma atualizarTurma(Long id, TurmaDTO turmaDTO) {
        try {
            // Carregar turma existente com alunos para preservar relacionamentos
            Turma turmaExistente = turmaRepository.findByIdWithAlunos(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + id));

            // Log dos dados recebidos para debug
            logger.info("=== INÍCIO atualizarTurma ID {} ===", id);
            logger.info("DTO recebido - nomeTurma: '{}', nivelProficiencia: '{}', turno: {}, formato: {}, modalidade: {}, situacaoTurma: '{}', calendarioPdf: '{}'", 
                    turmaDTO.nomeTurma(), turmaDTO.nivelProficiencia(), turmaDTO.turno(), 
                    turmaDTO.formato(), turmaDTO.modalidade(), turmaDTO.situacaoTurma(), turmaDTO.calendarioPdf());
            logger.info("Estado ANTES da atualização - nomeTurma: '{}', nivelProficiencia: '{}', turno: {}, formato: {}, modalidade: {}, situacaoTurma: '{}', calendarioPdf: '{}'", 
                    turmaExistente.getNomeTurma(), turmaExistente.getNivelProficiencia(), turmaExistente.getTurno(), 
                    turmaExistente.getFormato(), turmaExistente.getModalidade(), turmaExistente.getSituacaoTurma(), turmaExistente.getCalendarioPdf());

            // Atualizar todos os campos do DTO
            // IMPORTANTE: O controller já fez a mesclagem dos dados, então aqui apenas atualizamos
            // Sempre atualizar TODOS os campos para garantir que as mudanças sejam detectadas pelo Hibernate
            
            // Campos obrigatórios
            turmaExistente.setNomeTurma(turmaDTO.nomeTurma() != null ? turmaDTO.nomeTurma().trim() : null);
            
            // Campo idioma - sempre atualizar
            String idioma = turmaDTO.idioma() != null && !turmaDTO.idioma().trim().isEmpty() 
                ? turmaDTO.idioma().trim() : null;
            turmaExistente.setIdioma(idioma);
            
            // Campos opcionais - sempre atualizar com o valor do DTO (já foi mesclado no controller)
            String nivelProficiencia = turmaDTO.nivelProficiencia() != null && !turmaDTO.nivelProficiencia().trim().isEmpty() 
                ? turmaDTO.nivelProficiencia().trim() : null;
            turmaExistente.setNivelProficiencia(nivelProficiencia);
            
            String diaTurma = turmaDTO.diaTurma() != null && !turmaDTO.diaTurma().trim().isEmpty() 
                ? turmaDTO.diaTurma().trim() : null;
            turmaExistente.setDiaTurma(diaTurma);
            
            // Enums - sempre atualizar (mesmo que seja null, o controller já fez a mesclagem)
            turmaExistente.setTurno(turmaDTO.turno());
            turmaExistente.setFormato(turmaDTO.formato());
            turmaExistente.setModalidade(turmaDTO.modalidade());
            
            String realizador = turmaDTO.realizador() != null && !turmaDTO.realizador().trim().isEmpty() 
                ? turmaDTO.realizador().trim() : null;
            turmaExistente.setRealizador(realizador);
            
            String horaInicio = turmaDTO.horaInicio() != null && !turmaDTO.horaInicio().trim().isEmpty() 
                ? turmaDTO.horaInicio().trim() : null;
            turmaExistente.setHoraInicio(horaInicio);
            
            String horaTermino = turmaDTO.horaTermino() != null && !turmaDTO.horaTermino().trim().isEmpty() 
                ? turmaDTO.horaTermino().trim() : null;
            turmaExistente.setHoraTermino(horaTermino);
            
            String anoSemestre = turmaDTO.anoSemestre() != null && !turmaDTO.anoSemestre().trim().isEmpty() 
                ? turmaDTO.anoSemestre().trim() : null;
            turmaExistente.setAnoSemestre(anoSemestre);
            
            // Campos numéricos - sempre atualizar
            turmaExistente.setCargaHorariaTotal(turmaDTO.cargaHorariaTotal());
            turmaExistente.setQuantidadeAulas(turmaDTO.quantidadeAulas());
            
            // Validar calendário PDF se a turma está sendo ativada
            if (turmaDTO.situacaoTurma() != null && "ATIVA".equalsIgnoreCase(turmaDTO.situacaoTurma())) {
                if ((turmaDTO.calendarioPdf() == null || turmaDTO.calendarioPdf().trim().isEmpty()) 
                    && (turmaExistente.getCalendarioPdf() == null || turmaExistente.getCalendarioPdf().trim().isEmpty())) {
                    throw new BusinessException("Calendário PDF é obrigatório para ativar uma turma. Por favor, faça o upload do calendário.");
                }
            }
            
            // Calendário PDF - sempre atualizar (mesmo que seja o mesmo valor, o controller já fez a mesclagem)
            String calendarioPdf = turmaDTO.calendarioPdf() != null && !turmaDTO.calendarioPdf().trim().isEmpty() 
                ? turmaDTO.calendarioPdf().trim() : null;
            turmaExistente.setCalendarioPdf(calendarioPdf);
            
            // Datas - sempre atualizar
            turmaExistente.setInicioTurma(turmaDTO.inicioTurma());
            turmaExistente.setTerminoTurma(turmaDTO.terminoTurma());
            
            // Situação - sempre atualizar (mesmo que seja o mesmo valor)
            String situacaoTurma = turmaDTO.situacaoTurma() != null && !turmaDTO.situacaoTurma().trim().isEmpty() 
                ? turmaDTO.situacaoTurma().trim() : null;
            turmaExistente.setSituacaoTurma(situacaoTurma);

            // Atualizar professor responsável
            if (turmaDTO.professorResponsavelId() != null) {
                Professor professor = professorRepository.findById(turmaDTO.professorResponsavelId())
                        .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado com ID: " + turmaDTO.professorResponsavelId()));
                turmaExistente.setProfessorResponsavel(professor);
            } else {
                // Se professorResponsavelId for null, remover o professor responsável
                turmaExistente.setProfessorResponsavel(null);
            }

            // Salvar lista de alunos antes da atualização para verificar quais foram removidos
            List<Aluno> alunosAntigos = turmaExistente.getAlunos() != null ? new java.util.ArrayList<>(turmaExistente.getAlunos()) : new java.util.ArrayList<>();
            
            // Atualizar alunos - preservar alunos existentes se não houver novos IDs no DTO
            // O formulário HTML não envia alunoIds, então preservamos os existentes
            if (turmaDTO.alunoIds() != null) {
                if (!turmaDTO.alunoIds().isEmpty()) {
                    List<Aluno> alunos = alunoRepository.findAllById(turmaDTO.alunoIds());
                    turmaExistente.setAlunos(alunos);
                    logger.debug("Turma ID {} atualizada com {} alunos", id, alunos.size());
                } else {
                    // Lista vazia explícita - limpar alunos
                    turmaExistente.setAlunos(Collections.emptyList());
                    logger.debug("Turma ID {} teve alunos removidos (lista vazia explícita)", id);
                }
            } else {
                // alunoIds é null - preservar alunos existentes (formulário não enviou)
                logger.debug("Turma ID {} mantendo alunos existentes ({} alunos) - alunoIds não enviado no DTO", id, 
                        turmaExistente.getAlunos() != null ? turmaExistente.getAlunos().size() : 0);
            }
            
            // Identificar alunos que foram removidos da turma
            List<Aluno> alunosRemovidos = new java.util.ArrayList<>();
            if (turmaDTO.alunoIds() != null) {
                List<Long> novosAlunoIds = turmaDTO.alunoIds();
                for (Aluno alunoAntigo : alunosAntigos) {
                    if (!novosAlunoIds.contains(alunoAntigo.getId())) {
                        alunosRemovidos.add(alunoAntigo);
                    }
                }
            }

            // Log ANTES de salvar para debug
            logger.info("ANTES DE SALVAR - Turma ID {} - Nome: '{}' - Nível: '{}' - Turno: {} - Formato: {} - Modalidade: {} - Situação: '{}' - CalendarioPdf: '{}'", 
                    turmaExistente.getId(),
                    turmaExistente.getNomeTurma(),
                    turmaExistente.getNivelProficiencia(),
                    turmaExistente.getTurno(),
                    turmaExistente.getFormato(),
                    turmaExistente.getModalidade(),
                    turmaExistente.getSituacaoTurma(),
                    turmaExistente.getCalendarioPdf());
            
            // Salvar e fazer flush para garantir persistência imediata no banco
            Turma updatedTurma = turmaRepository.saveAndFlush(turmaExistente);
            
            // Log APÓS saveAndFlush
            logger.info("APÓS saveAndFlush - Turma ID {} - Nome: '{}' - Nível: '{}' - Turno: {} - Formato: {} - Modalidade: {} - Situação: '{}' - CalendarioPdf: '{}'", 
                    updatedTurma.getId(),
                    updatedTurma.getNomeTurma(),
                    updatedTurma.getNivelProficiencia(),
                    updatedTurma.getTurno(),
                    updatedTurma.getFormato(),
                    updatedTurma.getModalidade(),
                    updatedTurma.getSituacaoTurma(),
                    updatedTurma.getCalendarioPdf());
            
            // Forçar sincronização com o banco de dados (saveAndFlush já faz isso, mas garantimos novamente)
            turmaRepository.flush();
            
            // Recarregar com fetch join para garantir que todos os dados estejam carregados do banco
            // Usar findByIdWithAlunos força uma nova consulta ao banco, ignorando qualquer cache de primeiro nível
            Turma turmaRecarregada = turmaRepository.findByIdWithAlunos(updatedTurma.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada após atualização com ID: " + id));
            
            // Log APÓS recarregar do banco
            logger.info("APÓS RECARREGAR DO BANCO - Turma ID {} - Nome: '{}' - Nível: '{}' - Turno: {} - Formato: {} - Modalidade: {} - Situação: '{}' - CalendarioPdf: '{}'", 
                    turmaRecarregada.getId(),
                    turmaRecarregada.getNomeTurma(),
                    turmaRecarregada.getNivelProficiencia(),
                    turmaRecarregada.getTurno(),
                    turmaRecarregada.getFormato(),
                    turmaRecarregada.getModalidade(),
                    turmaRecarregada.getSituacaoTurma(),
                    turmaRecarregada.getCalendarioPdf());
            
            // Forçar inicialização de todas as coleções lazy dentro da transação
            if (turmaRecarregada.getAlunos() != null) {
                turmaRecarregada.getAlunos().size(); // Força inicialização
            }
            
            updatedTurma = turmaRecarregada;
            
            // Atualizar status dos alunos que foram removidos da turma
            if (!alunosRemovidos.isEmpty()) {
                for (Aluno alunoRemovido : alunosRemovidos) {
                    // Recarregar aluno com turmas atualizadas
                    Optional<Aluno> alunoAtualizado = alunoRepository.findByIdWithTurmasAndResponsavel(alunoRemovido.getId());
                    if (alunoAtualizado.isPresent()) {
                        Aluno alunoComTurmas = alunoAtualizado.get();
                        // Se não tem mais turmas, atualizar para INATIVO
                        if (alunoComTurmas.getTurmas() == null || alunoComTurmas.getTurmas().isEmpty()) {
                            alunoComTurmas.setSituacao("INATIVO");
                            alunoRepository.save(alunoComTurmas);
                            logger.debug("Aluno ID {} atualizado para INATIVO após ser removido da turma ID {}", alunoRemovido.getId(), id);
                        }
                    }
                }
            }
            
            // Log detalhado após atualização
            logger.info("Turma atualizada com ID: {} - Nome: {} - Nível: {} - Turno: {} - Formato: {} - Modalidade: {} - Situação: {} - Alunos: {}", 
                    updatedTurma.getId(), 
                    updatedTurma.getNomeTurma(),
                    updatedTurma.getNivelProficiencia(),
                    updatedTurma.getTurno(),
                    updatedTurma.getFormato(),
                    updatedTurma.getModalidade(),
                    updatedTurma.getSituacaoTurma(),
                    updatedTurma.getAlunos() != null ? updatedTurma.getAlunos().size() : 0);
            
            return updatedTurma;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao atualizar turma com ID {}: ", id, e);
            throw new BusinessException("Erro ao atualizar turma: " + e.getMessage());
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "turmasLista", allEntries = true),
            @CacheEvict(value = "turmasPorId", key = "#id")
    })
    public void deletarTurma(Long id) {
        try {
            Turma turma = turmaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + id));
            
            // Verificar se há contratos vinculados à turma
            List<br.com.arirang.plataforma.entity.Contrato> contratos = contratoRepository.findByTurmaIdOrderByDataCriacaoDesc(id);
            if (contratos != null && !contratos.isEmpty()) {
                logger.warn("Turma ID {} possui {} contrato(s) vinculado(s). Não é possível deletar a turma enquanto houver contratos ativos.", id, contratos.size());
                throw new BusinessException("Não é possível deletar a turma. Existem " + contratos.size() + " contrato(s) vinculado(s) a esta turma. " +
                        "Por favor, cancele ou delete os contratos antes de deletar a turma.");
            }
            
            // Primeiro, deletar todos os boletins relacionados à turma
            // Isso é necessário porque há uma foreign key constraint
            List<Boletim> boletins = boletimRepository.findByTurmaId(id);
            if (boletins != null && !boletins.isEmpty()) {
                logger.info("Deletando {} boletins relacionados à turma ID {}", boletins.size(), id);
                boletimRepository.deleteAll(boletins);
                boletimRepository.flush(); // Garantir que os boletins sejam deletados antes de deletar a turma
            }
            
            // Salvar lista de alunos antes de remover associações para atualizar status depois
            List<Aluno> alunosAfetados = turma.getAlunos() != null ? new java.util.ArrayList<>(turma.getAlunos()) : new java.util.ArrayList<>();
            
            // Remover associações com alunos
            turma.getAlunos().clear();
            turmaRepository.save(turma); // Atualiza a tabela turma_aluno
            turmaRepository.flush(); // Garantir que as associações sejam removidas
            
            // Agora pode deletar a turma com segurança
            turmaRepository.deleteById(id);
            turmaRepository.flush(); // Garantir que a turma seja deletada
            
            // Atualizar status dos alunos que perderam a turma
            if (!alunosAfetados.isEmpty()) {
                for (Aluno aluno : alunosAfetados) {
                    // Recarregar aluno com turmas atualizadas
                    Optional<Aluno> alunoAtualizado = alunoRepository.findByIdWithTurmasAndResponsavel(aluno.getId());
                    if (alunoAtualizado.isPresent()) {
                        Aluno alunoComTurmas = alunoAtualizado.get();
                        // Se não tem mais turmas, atualizar para INATIVO
                        if (alunoComTurmas.getTurmas() == null || alunoComTurmas.getTurmas().isEmpty()) {
                            alunoComTurmas.setSituacao("INATIVO");
                            alunoRepository.save(alunoComTurmas);
                            logger.debug("Aluno ID {} atualizado para INATIVO após deletar turma ID {}", aluno.getId(), id);
                        }
                    }
                }
            }
            
            logger.info("Turma deletada com sucesso - ID: {}, {} boletins foram deletados, {} alunos afetados", 
                    id, boletins != null ? boletins.size() : 0, alunosAfetados.size());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (BusinessException e) {
            throw e; // Re-lançar BusinessException para manter a mensagem específica
        } catch (Exception e) {
            logger.error("Erro ao deletar turma com ID {}: ", id, e);
            throw new BusinessException("Erro ao deletar turma: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "turmasLista")
    public List<Turma> listarTodasTurmas() {
        try {
            // Usar findAllWithAlunos para garantir que professor e alunos sejam carregados
            List<Turma> turmas = turmaRepository.findAllWithAlunos();
            // Filtrar turmas com valores inválidos de enum e tratar erros
            List<Turma> turmasValidas = new java.util.ArrayList<>();
            for (Turma turma : turmas) {
                try {
                    // Forçar inicialização do professor responsável dentro da transação
                    if (turma.getProfessorResponsavel() != null) {
                        turma.getProfessorResponsavel().getId();
                        turma.getProfessorResponsavel().getNomeCompleto();
                    }
                    
                    // Tentar acessar campos enum - se houver valor inválido, lançará exceção
                    if (turma.getNomeTurma() != null) turma.getNomeTurma();
                    if (turma.getNivelProficiencia() != null) turma.getNivelProficiencia();
                    
                    // Tentar acessar turno - se for inválido, tratar aqui
                    try {
                        if (turma.getTurno() != null) turma.getTurno();
                    } catch (IllegalArgumentException e) {
                        logger.warn("Turma ID {} tem valor inválido de turno. Corrigindo para null. Valor no banco pode estar incorreto.", turma.getId());
                        turma.setTurno(null);
                    }
                    
                    // Tentar acessar formato
                    if (turma.getFormato() != null) turma.getFormato();
                    
                    // Tentar acessar modalidade - se for inválida, tratar aqui
                    try {
                        if (turma.getModalidade() != null) turma.getModalidade();
                    } catch (IllegalArgumentException e) {
                        logger.warn("Turma ID {} tem valor inválido de modalidade. Corrigindo para null. Valor no banco pode estar incorreto.", turma.getId());
                        turma.setModalidade(null);
                    }
                    turmasValidas.add(turma);
                } catch (Exception e) {
                    logger.error("Erro ao processar turma ID {}: {}. Pulando esta turma.", turma.getId(), e.getMessage());
                    // Continuar processando outras turmas mesmo se uma falhar
                }
            }
            return turmasValidas;
        } catch (Exception e) {
            logger.error("Erro ao listar todas as turmas: ", e);
            throw new BusinessException("Erro ao listar turmas: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Turma> listarTodasTurmasComAlunos() {
        try {
            List<Turma> turmas = turmaRepository.findAllWithAlunos();
            // Forçar inicialização dos alunos dentro da transação
            turmas.forEach(turma -> {
                if (turma.getAlunos() != null) {
                    turma.getAlunos().size(); // Força inicialização
                }
            });
            return turmas;
        } catch (Exception e) {
            logger.error("Erro ao listar todas as turmas com alunos: ", e);
            throw new BusinessException("Erro ao listar turmas com alunos: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Optional<Turma> buscarTurmaPorId(Long id) {
        try {
            // Usar método com fetch join para carregar alunos dentro da transação
            // Isso evita erro de lazy initialization quando acessar turma.getAlunos() depois
            Optional<Turma> turma = turmaRepository.findByIdWithAlunos(id);
            // Forçar inicialização de campos lazy dentro da transação
            turma.ifPresent(t -> {
                // Forçar inicialização do professor responsável
                if (t.getProfessorResponsavel() != null) {
                    t.getProfessorResponsavel().getId(); // Forçar inicialização
                }
                // Forçar inicialização da coleção de alunos dentro da transação
                if (t.getAlunos() != null) {
                    t.getAlunos().size(); // Forçar inicialização da coleção
                    // Também acessar cada aluno para garantir que estejam carregados
                    t.getAlunos().forEach(aluno -> {
                        if (aluno != null) {
                            aluno.getId(); // Garantir que o aluno está carregado
                        }
                    });
                }
                // Forçar acesso ao campo idioma para garantir que está carregado
                t.getIdioma();
            });
            return turma;
        } catch (Exception e) {
            logger.error("Erro ao buscar turma por ID {}: ", id, e);
            throw new BusinessException("Erro ao buscar turma: " + e.getMessage());
        }
    }
    
    /**
     * Busca turma por ID forçando refresh do banco (sem usar cache)
     * Usado quando precisamos garantir dados atualizados (ex: para contratos)
     */
    @Transactional(readOnly = true)
    public Optional<Turma> buscarTurmaPorIdSemCache(Long id) {
        try {
            // Buscar diretamente do banco usando findById (não usa findByIdWithAlunos que pode ter cache)
            // Depois fazer refresh manual se necessário
            return turmaRepository.findById(id)
                    .map(turma -> {
                        // Forçar inicialização de todos os campos importantes
                        if (turma.getProfessorResponsavel() != null) {
                            turma.getProfessorResponsavel().getId();
                        }
                        // Acessar idioma para garantir que está carregado
                        turma.getIdioma();
                        return turma;
                    });
        } catch (Exception e) {
            logger.error("Erro ao buscar turma por ID {} sem cache: ", id, e);
            throw new BusinessException("Erro ao buscar turma: " + e.getMessage());
        }
    }

    /**
     * Busca turma por ID e converte para DTO dentro da transação
     * Isso garante que os alunos sejam carregados antes da conversão
     */
    @Transactional(readOnly = true)
    public Optional<TurmaDTO> buscarTurmaPorIdAsDTO(Long id) {
        try {
            Optional<Turma> turmaOpt = buscarTurmaPorId(id);
            if (turmaOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Turma turma = turmaOpt.get();
            // Converter para DTO dentro da transação, garantindo que alunos estejam carregados
            TurmaDTO dto = new TurmaDTO(
                    turma.getId(),
                    turma.getNomeTurma(),
                    turma.getIdioma(),
                    turma.getProfessorResponsavel() != null ? turma.getProfessorResponsavel().getId() : null,
                    turma.getNivelProficiencia(),
                    turma.getDiaTurma(),
                    turma.getTurno(),
                    turma.getFormato(),
                    turma.getModalidade(),
                    turma.getRealizador(),
                    turma.getHoraInicio(),
                    turma.getHoraTermino(),
                    turma.getAnoSemestre(),
                    turma.getCargaHorariaTotal(),
                    turma.getQuantidadeAulas(),
                    turma.getCalendarioPdf(),
                    turma.getInicioTurma(),
                    turma.getTerminoTurma(),
                    turma.getSituacaoTurma(),
                    turma.getAlunos() != null ? 
                        turma.getAlunos().stream().map(Aluno::getId).collect(Collectors.toList()) : 
                        Collections.emptyList()
            );
            return Optional.of(dto);
        } catch (Exception e) {
            logger.error("Erro ao buscar turma por ID {} como DTO: ", id, e);
            throw new BusinessException("Erro ao buscar turma: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza turma com auditoria (justificativa obrigatória)
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "turmasLista", allEntries = true),
            @CacheEvict(value = "turmasPorId", key = "#id")
    })
    public Turma atualizarTurmaProtegida(Long id, TurmaDTO turmaDTO, String usuario, String justificativa, jakarta.servlet.http.HttpServletRequest request) {
        try {
            // Obter estado anterior para auditoria ANTES de qualquer alteração
            TurmaDTO estadoAnterior = buscarTurmaPorIdAsDTO(id).orElse(null);
            
            // Verificar se turma está fechada - bloqueio total
            Turma turmaExistente = turmaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + id));
            
            if ("FECHADA".equalsIgnoreCase(turmaExistente.getSituacaoTurma())) {
                throw new BusinessException("Não é possível alterar uma turma fechada.");
            }
            
            // Validar justificativa obrigatória
            if (justificativa == null || justificativa.trim().isEmpty()) {
                throw new BusinessException("Justificativa é obrigatória para alterações em turmas. Por favor, descreva o motivo da alteração.");
            }
            
            // Atualizar turma (isso já faz saveAndFlush e recarrega do banco)
            Turma turmaAtualizada = atualizarTurma(id, turmaDTO);
            
            // Obter estado posterior após atualização
            // O método atualizarTurma já retorna a turma recarregada do banco
            TurmaDTO estadoPosterior = new TurmaDTO(
                    turmaAtualizada.getId(),
                    turmaAtualizada.getNomeTurma(),
                    turmaAtualizada.getIdioma(),
                    turmaAtualizada.getProfessorResponsavel() != null ? turmaAtualizada.getProfessorResponsavel().getId() : null,
                    turmaAtualizada.getNivelProficiencia(),
                    turmaAtualizada.getDiaTurma(),
                    turmaAtualizada.getTurno(),
                    turmaAtualizada.getFormato(),
                    turmaAtualizada.getModalidade(),
                    turmaAtualizada.getRealizador(),
                    turmaAtualizada.getHoraInicio(),
                    turmaAtualizada.getHoraTermino(),
                    turmaAtualizada.getAnoSemestre(),
                    turmaAtualizada.getCargaHorariaTotal(),
                    turmaAtualizada.getQuantidadeAulas(),
                    turmaAtualizada.getCalendarioPdf(),
                    turmaAtualizada.getInicioTurma(),
                    turmaAtualizada.getTerminoTurma(),
                    turmaAtualizada.getSituacaoTurma(),
                    turmaAtualizada.getAlunos() != null ? 
                        turmaAtualizada.getAlunos().stream().map(Aluno::getId).collect(Collectors.toList()) : 
                        Collections.emptyList()
            );
            
            // Registrar auditoria (não deve causar rollback se falhar)
            boolean auditoriaRegistrada = false;
            try {
                AuditoriaTurma auditoria = auditoriaTurmaService.registrarAlteracao(
                    id,
                    estadoAnterior,
                    estadoPosterior,
                    usuario != null ? usuario : "Sistema",
                    justificativa.trim(),
                    request
                );
                auditoriaRegistrada = (auditoria != null);
                if (!auditoriaRegistrada) {
                    logger.warn("Auditoria não foi registrada para turma ID {}, mas a atualização continuará", id);
                }
            } catch (Exception e) {
                logger.error("Erro ao registrar auditoria para turma ID {}: {}", id, e.getMessage(), e);
                // Não falhar a atualização se a auditoria falhar
            }
            
            // Forçar flush final para garantir que todas as alterações sejam persistidas
            // Só fazer flush se não houver erro na auditoria que possa ter marcado a transação como rollback-only
            try {
                turmaRepository.flush();
            } catch (Exception e) {
                logger.error("Erro ao fazer flush após atualização da turma ID {}: {}", id, e.getMessage(), e);
                // Se o flush falhar, pode ser porque a transação foi marcada como rollback-only
                // Nesse caso, relançar a exceção para que a transação seja revertida
                throw new BusinessException("Erro ao persistir alterações: " + e.getMessage());
            }
            
            // Recarregar do banco uma última vez para garantir que os dados estão atualizados
            Turma turmaFinal = turmaRepository.findByIdWithAlunos(turmaAtualizada.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada após atualização com ID: " + id));
            
            logger.info("Turma ID {} finalizada - Nome: '{}' - Nível: '{}' - Turno: {} - Formato: {} - Modalidade: {} - Situação: '{}' - CalendarioPdf: '{}'", 
                    turmaFinal.getId(),
                    turmaFinal.getNomeTurma(),
                    turmaFinal.getNivelProficiencia(),
                    turmaFinal.getTurno(),
                    turmaFinal.getFormato(),
                    turmaFinal.getModalidade(),
                    turmaFinal.getSituacaoTurma(),
                    turmaFinal.getCalendarioPdf());
            
            return turmaFinal;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao atualizar turma protegida ID {}: ", id, e);
            throw new BusinessException("Erro ao atualizar turma: " + e.getMessage());
        }
    }
    
    /**
     * Gera protocolo de alteração (usado para exibição)
     */
    public String gerarProtocoloAlteracao(Long turmaId, String usuario) {
        return auditoriaTurmaService.gerarProtocolo(turmaId, usuario);
    }
    
    /**
     * Busca histórico de alterações
     */
    @Transactional(readOnly = true)
    public java.util.List<br.com.arirang.plataforma.entity.AuditoriaTurma> buscarHistoricoAlteracoes(Long turmaId) {
        return auditoriaTurmaService.buscarHistorico(turmaId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "turmasLista", allEntries = true),
            @CacheEvict(value = "turmasPorId", key = "#id")
    }, put = {
            @CachePut(value = "turmasPorId", key = "#id")
    })
    public Turma fecharTurma(Long id) {
        try {
            Turma turma = turmaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + id));

            // Verificar se todos os alunos têm boletins finalizados
            Long totalAlunos = boletimRepository.countAlunosByTurmaId(id);
            Long boletinsFinalizados = boletimRepository.countBoletinsFinalizadosByTurmaId(id);
            
            if (!totalAlunos.equals(boletinsFinalizados)) {
                throw new BusinessException("Existem alunos que boletim ainda não foi lançado");
            }

            turma.setSituacaoTurma("FECHADA");
            Turma turmaFechada = turmaRepository.save(turma);
            logger.info("Turma fechada com ID: {}", id);
            return turmaFechada;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao fechar turma com ID {}: ", id, e);
            throw new BusinessException("Erro ao fechar turma: " + e.getMessage());
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "turmasLista", allEntries = true),
            @CacheEvict(value = "turmasPorId", key = "#id")
    }, put = {
            @CachePut(value = "turmasPorId", key = "#id")
    })
    public Turma reabrirTurma(Long id) {
        try {
            Turma turma = turmaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + id));

            if (!"FECHADA".equals(turma.getSituacaoTurma())) {
                throw new BusinessException("Apenas turmas fechadas podem ser reabertas");
            }

            turma.setSituacaoTurma("ATIVA");
            Turma turmaReaberta = turmaRepository.save(turma);
            logger.info("Turma reaberta com ID: {}", id);
            return turmaReaberta;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao reabrir turma com ID {}: ", id, e);
            throw new BusinessException("Erro ao reabrir turma: " + e.getMessage());
        }
    }

    /**
     * Lista todos os alunos de uma turma específica
     * Carrega os alunos com suas turmas e responsável para evitar LazyInitializationException
     */
    @Transactional(readOnly = true)
    public List<Aluno> listarAlunosPorTurma(Long turmaId) {
        try {
            // Verificar se a turma existe
            if (!turmaRepository.existsById(turmaId)) {
                throw new ResourceNotFoundException("Turma não encontrada com ID: " + turmaId);
            }
            
            // Usar método do AlunoRepository que faz fetch join das turmas e responsável
            // Isso evita erro de lazy initialization quando acessar aluno.getTurmas() depois
            List<Aluno> alunos = alunoRepository.findAllByTurmaIdWithFetch(turmaId);
            
            // Forçar inicialização das coleções lazy dentro da transação
            alunos.forEach(aluno -> {
                if (aluno != null) {
                    // Forçar inicialização das turmas do aluno
                    if (aluno.getTurmas() != null) {
                        aluno.getTurmas().size(); // Força inicialização da coleção
                    }
                    // Forçar inicialização do responsável
                    if (aluno.getResponsavel() != null) {
                        aluno.getResponsavel().getId(); // Força inicialização
                    }
                }
            });
            
            return alunos;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao listar alunos da turma ID {}: ", turmaId, e);
            throw new BusinessException("Erro ao listar alunos da turma: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> verificarDuplicacaoTurmas() {
        try {
            List<Turma> todasTurmas = turmaRepository.findAll();
            java.util.Map<String, java.util.List<Turma>> turmasPorNome = new java.util.HashMap<>();
            
            // Agrupar turmas por nome (ignorando case e espaços)
            for (Turma turma : todasTurmas) {
                if (turma.getNomeTurma() != null) {
                    String nomeNormalizado = turma.getNomeTurma().trim().toLowerCase();
                    turmasPorNome.computeIfAbsent(nomeNormalizado, k -> new java.util.ArrayList<>()).add(turma);
                }
            }
            
            // Filtrar apenas duplicatas (grupos com mais de 1 turma)
            List<java.util.Map<String, Object>> duplicatas = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, java.util.List<Turma>> entry : turmasPorNome.entrySet()) {
                if (entry.getValue().size() > 1) {
                    java.util.Map<String, Object> duplicata = new java.util.HashMap<>();
                    duplicata.put("nomeTurma", entry.getValue().get(0).getNomeTurma());
                    duplicata.put("quantidade", entry.getValue().size());
                    duplicata.put("ids", entry.getValue().stream().map(Turma::getId).collect(Collectors.toList()));
                    duplicatas.add(duplicata);
                }
            }
            
            logger.info("Verificação de duplicatas: {} grupo(s) de turmas duplicadas encontrado(s)", duplicatas.size());
            return duplicatas;
        } catch (Exception e) {
            logger.error("Erro ao verificar duplicação de turmas: ", e);
            throw new BusinessException("Erro ao verificar duplicação de turmas: " + e.getMessage());
        }
    }
}