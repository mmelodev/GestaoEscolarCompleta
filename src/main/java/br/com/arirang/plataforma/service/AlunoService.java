package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.AlunoDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Responsavel;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.repository.AlunoRepository;
import br.com.arirang.plataforma.repository.ResponsavelRepository;
import br.com.arirang.plataforma.repository.TurmaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlunoService {

    private static final Logger logger = LoggerFactory.getLogger(AlunoService.class);

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ResponsavelRepository responsavelRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Transactional(readOnly = true)
    public List<Aluno> listarTodosAlunos() {
        return alunoRepository.findAllWithTurmasAndResponsavel();
    }

    @Transactional(readOnly = true)
    public Optional<Aluno> buscarAlunoPorId(Long id) {
        // Usar método com fetch join para carregar turmas e responsável dentro da transação
        // Isso evita erro de lazy initialization quando acessar aluno.getTurmas() depois
        return alunoRepository.findByIdWithTurmasAndResponsavel(id);
    }

    @Transactional(readOnly = true)
    public List<Aluno> listarAlunosPorTurma(Long turmaId) {
        return alunoRepository.findAllByTurmaIdWithFetch(turmaId);
    }

    @Transactional(readOnly = true)
    public List<Aluno> buscarAniversariantesDoDia() {
        return alunoRepository.findAniversariantesDoDia();
    }

    /**
     * Remove formatação de CPF/telefone (remove pontos, traços, parênteses, espaços)
     * Retorna null se o valor estiver vazio após limpeza
     * IMPORTANTE: Sempre retorna null para strings vazias, nunca string vazia ""
     */
    private String limparDocumento(String valor) {
        if (valor == null) {
            return null;
        }
        String trim = valor.trim();
        if (trim.isEmpty()) {
            return null;
        }
        String limpo = trim.replaceAll("\\D", "");
        // Garantir que nunca retornamos string vazia, sempre null
        return limpo.isEmpty() ? null : limpo;
    }

    /**
     * Valida e limpa CPF - retorna null se CPF inválido ou vazio
     * CPF válido deve ter exatamente 11 dígitos numéricos e passar no algoritmo de validação
     * IMPORTANTE: Retorna null para CPFs inválidos, pois o validador @CPF aceita null (campo opcional)
     */
    private String limparEValidarCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return null;
        }
        
        String limpo = limparDocumento(cpf);
        if (limpo == null || limpo.length() != 11) {
            logger.debug("CPF inválido ou incompleto após limpeza: '{}' (tamanho: {})", 
                limpo != null ? limpo : "null", limpo != null ? limpo.length() : 0);
            return null; // CPF inválido ou incompleto - retornar null para não quebrar validação
        }
        
        // Verificar se não são todos os dígitos iguais (ex: 11111111111)
        if (limpo.matches("(\\d)\\1{10}")) {
            logger.debug("CPF inválido (todos dígitos iguais): {}", limpo);
            return null;
        }
        
        // Validar algoritmo do CPF
        if (!validarAlgoritmoCPF(limpo)) {
            logger.debug("CPF inválido (falha no algoritmo de validação): {}", limpo);
            return null;
        }
        
        logger.debug("CPF limpo e válido: {}", limpo);
        return limpo;
    }
    
    /**
     * Valida o algoritmo do CPF (dígitos verificadores)
     */
    private boolean validarAlgoritmoCPF(String cpf) {
        try {
            if (cpf == null || cpf.length() != 11) {
                return false;
            }
            
            // Calcula o primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int resto = soma % 11;
            int primeiroDigito = (resto < 2) ? 0 : 11 - resto;

            // Calcula o segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            resto = soma % 11;
            int segundoDigito = (resto < 2) ? 0 : 11 - resto;

            // Verifica se os dígitos calculados coincidem com os informados
            return primeiroDigito == Character.getNumericValue(cpf.charAt(9)) &&
                   segundoDigito == Character.getNumericValue(cpf.charAt(10));
        } catch (Exception e) {
            logger.warn("Erro ao validar algoritmo do CPF: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Atualiza o status do aluno baseado na relação com turmas:
     * - Se tem pelo menos uma turma → ATIVO
     * - Se não tem turmas → INATIVO
     */
    private void atualizarStatusBaseadoEmTurmas(Aluno aluno) {
        if (aluno.getTurmas() == null || aluno.getTurmas().isEmpty()) {
            aluno.setSituacao("INATIVO");
            logger.debug("Aluno ID {} atualizado para INATIVO (sem turmas)", aluno.getId());
        } else {
            aluno.setSituacao("ATIVO");
            logger.debug("Aluno ID {} atualizado para ATIVO ({} turma(s))", aluno.getId(), aluno.getTurmas().size());
        }
    }

    @Transactional
    public Aluno criarAluno(AlunoDTO alunoDTO) {
        try {
            Aluno aluno = new Aluno();
            aluno.setNomeCompleto(alunoDTO.nomeCompleto());
            aluno.setEmail(alunoDTO.email());
            // Limpar formatação do CPF antes de salvar
            aluno.setCpf(limparDocumento(alunoDTO.cpf()));
            aluno.setRg(alunoDTO.rg());
            aluno.setOrgaoExpeditorRg(alunoDTO.orgaoExpeditorRg());
            aluno.setNacionalidade(alunoDTO.nacionalidade());
            aluno.setUf(alunoDTO.uf());
            // Limpar formatação do telefone antes de salvar
            aluno.setTelefone(limparDocumento(alunoDTO.telefone()));
            aluno.setDataNascimento(alunoDTO.dataNascimento());
            aluno.setNomeSocial(alunoDTO.nomeSocial());
            aluno.setGenero(alunoDTO.genero());
            aluno.setUltimoNivel(alunoDTO.ultimoNivel());
            aluno.setEndereco(alunoDTO.endereco());
            aluno.setGrauParentesco(alunoDTO.grauParentesco());
            aluno.setResponsavelFinanceiro(alunoDTO.responsavelFinanceiro());
            
            // Processar idiomas
            if (alunoDTO.idiomas() != null && !alunoDTO.idiomas().isEmpty()) {
                aluno.setIdiomas(new java.util.ArrayList<>(alunoDTO.idiomas()));
            } else {
                aluno.setIdiomas(new java.util.ArrayList<>());
            }

            // Criar responsável se checkbox marcado OU nome preenchido
            boolean deveTerResponsavel = alunoDTO.responsavelFinanceiro() || 
                (alunoDTO.nomeResponsavel() != null && !alunoDTO.nomeResponsavel().trim().isEmpty());
            
            if (deveTerResponsavel && 
                alunoDTO.nomeResponsavel() != null && 
                !alunoDTO.nomeResponsavel().trim().isEmpty()) {
                Responsavel responsavel = new Responsavel();
                responsavel.setNomeCompleto(alunoDTO.nomeResponsavel() != null ? alunoDTO.nomeResponsavel().trim() : null);
                
                // Limpar e validar CPF antes de salvar (deve ter 11 dígitos)
                String cpfLimpo = limparEValidarCPF(alunoDTO.cpfResponsavel());
                // Garantir que nunca salvamos string vazia, sempre null
                if (cpfLimpo != null && cpfLimpo.trim().isEmpty()) {
                    cpfLimpo = null;
                }
                responsavel.setCpf(cpfLimpo);
                
                // Limpar formatação do telefone antes de salvar
                String telefoneLimpo = limparDocumento(alunoDTO.telefoneResponsavel());
                // Garantir que nunca salvamos string vazia, sempre null
                if (telefoneLimpo != null && telefoneLimpo.trim().isEmpty()) {
                    telefoneLimpo = null;
                }
                responsavel.setTelefone(telefoneLimpo);
                
                logger.debug("Salvando responsável (CREATE) - Nome: '{}', CPF: '{}' (tamanho: {}), Telefone: '{}'", 
                    responsavel.getNomeCompleto(), 
                    responsavel.getCpf() != null ? responsavel.getCpf() : "null",
                    responsavel.getCpf() != null ? responsavel.getCpf().length() : 0,
                    responsavel.getTelefone() != null ? responsavel.getTelefone() : "null");
                
                responsavel.setEmail(alunoDTO.emailResponsavel() != null && !alunoDTO.emailResponsavel().trim().isEmpty() ? alunoDTO.emailResponsavel().trim() : null);
                
                // Validar responsável antes de salvar
                try {
                    aluno.setResponsavel(responsavelRepository.save(responsavel));
                } catch (jakarta.validation.ConstraintViolationException e) {
                    logger.error("Erro de validação ao salvar responsável - CPF: '{}', Erros: {}", 
                        responsavel.getCpf(), e.getConstraintViolations());
                    throw new BusinessException("Erro ao salvar dados do responsável: CPF inválido. Por favor, verifique o CPF informado.");
                }
                // Forçar checkbox como true se responsável foi criado
                aluno.setResponsavelFinanceiro(true);
                logger.info("Responsável criado para aluno: {} (CPF: {})", 
                    responsavel.getNomeCompleto(), responsavel.getCpf());
            } else {
                aluno.setResponsavel(null);
                logger.debug("Responsável não criado - checkbox: {}, nome: '{}'", 
                    alunoDTO.responsavelFinanceiro(), 
                    alunoDTO.nomeResponsavel() != null ? alunoDTO.nomeResponsavel() : "null");
            }

            // Garante geração de ID antes de relacionamentos ManyToMany
            Aluno savedAluno = alunoRepository.saveAndFlush(aluno);

            if (alunoDTO.turmaIds() != null && !alunoDTO.turmaIds().isEmpty()) {
                List<Turma> turmas = turmaRepository.findAllById(alunoDTO.turmaIds());
                
                // Verificar se alguma turma está fechada
                for (Turma turma : turmas) {
                    if ("FECHADA".equals(turma.getSituacaoTurma())) {
                        throw new BusinessException("Não é possível vincular alunos a turmas fechadas. A turma '" + turma.getNomeTurma() + "' está fechada para novos alunos.");
                    }
                }
                
                savedAluno.setTurmas(turmas);
            } else {
                savedAluno.setTurmas(Collections.emptyList());
            }
            
            // Atualizar status baseado nas turmas (não usar o valor do DTO)
            atualizarStatusBaseadoEmTurmas(savedAluno);
            savedAluno = alunoRepository.saveAndFlush(savedAluno);

            return savedAluno;
        } catch (Exception e) {
            logger.error("Erro ao criar aluno", e);
            throw new BusinessException("Erro ao criar aluno: " + e.getMessage());
        }
    }

    @Transactional
    public Aluno atualizarAluno(Long id, AlunoDTO alunoDTO) {
        // Usar método com fetch join para carregar responsável dentro da transação
        Aluno aluno = alunoRepository.findByIdWithTurmasAndResponsavel(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + id));

        aluno.setNomeCompleto(alunoDTO.nomeCompleto());
        aluno.setEmail(alunoDTO.email());
        // Limpar formatação do CPF antes de salvar
        aluno.setCpf(limparDocumento(alunoDTO.cpf()));
        aluno.setRg(alunoDTO.rg());
        aluno.setOrgaoExpeditorRg(alunoDTO.orgaoExpeditorRg());
        aluno.setNacionalidade(alunoDTO.nacionalidade());
        aluno.setUf(alunoDTO.uf());
        // Limpar formatação do telefone antes de salvar
        aluno.setTelefone(limparDocumento(alunoDTO.telefone()));
        aluno.setDataNascimento(alunoDTO.dataNascimento());
        aluno.setNomeSocial(alunoDTO.nomeSocial());
        aluno.setGenero(alunoDTO.genero());
        aluno.setUltimoNivel(alunoDTO.ultimoNivel());
        aluno.setEndereco(alunoDTO.endereco());
        aluno.setGrauParentesco(alunoDTO.grauParentesco());
        aluno.setResponsavelFinanceiro(alunoDTO.responsavelFinanceiro());
        
        // Processar idiomas
        if (alunoDTO.idiomas() != null && !alunoDTO.idiomas().isEmpty()) {
            aluno.setIdiomas(new java.util.ArrayList<>(alunoDTO.idiomas()));
        } else {
            aluno.setIdiomas(new java.util.ArrayList<>());
        }

        // Log detalhado para debug
        logger.debug("Processando responsável para aluno ID {} - checkbox: {}, nome: '{}', cpf: '{}'", 
            id, alunoDTO.responsavelFinanceiro(), 
            alunoDTO.nomeResponsavel() != null ? alunoDTO.nomeResponsavel() : "null",
            alunoDTO.cpfResponsavel() != null ? alunoDTO.cpfResponsavel() : "null");
        
        // Criar/atualizar responsável se:
        // 1. Checkbox marcado OU
        // 2. Nome do responsável preenchido (para casos onde checkbox pode não ser enviado corretamente)
        boolean deveTerResponsavel = alunoDTO.responsavelFinanceiro() || 
            (alunoDTO.nomeResponsavel() != null && !alunoDTO.nomeResponsavel().trim().isEmpty());
        
        if (deveTerResponsavel && 
            alunoDTO.nomeResponsavel() != null && 
            !alunoDTO.nomeResponsavel().trim().isEmpty()) {
            Responsavel responsavel = aluno.getResponsavel() != null ? aluno.getResponsavel() : new Responsavel();
            responsavel.setNomeCompleto(alunoDTO.nomeResponsavel() != null ? alunoDTO.nomeResponsavel().trim() : null);
            
            // Limpar e validar CPF antes de salvar (deve ter 11 dígitos)
            String cpfLimpo = limparEValidarCPF(alunoDTO.cpfResponsavel());
            // Garantir que nunca salvamos string vazia, sempre null
            if (cpfLimpo != null && cpfLimpo.trim().isEmpty()) {
                cpfLimpo = null;
            }
            responsavel.setCpf(cpfLimpo);
            
            // Limpar formatação do telefone antes de salvar
            String telefoneLimpo = limparDocumento(alunoDTO.telefoneResponsavel());
            // Garantir que nunca salvamos string vazia, sempre null
            if (telefoneLimpo != null && telefoneLimpo.trim().isEmpty()) {
                telefoneLimpo = null;
            }
            responsavel.setTelefone(telefoneLimpo);
            
            logger.debug("Salvando responsável (UPDATE) - Nome: '{}', CPF: '{}' (tamanho: {}), Telefone: '{}'", 
                responsavel.getNomeCompleto(), 
                responsavel.getCpf() != null ? responsavel.getCpf() : "null",
                responsavel.getCpf() != null ? responsavel.getCpf().length() : 0,
                responsavel.getTelefone() != null ? responsavel.getTelefone() : "null");
            
            responsavel.setEmail(alunoDTO.emailResponsavel() != null && !alunoDTO.emailResponsavel().trim().isEmpty() ? alunoDTO.emailResponsavel().trim() : null);
            
            // Validar responsável antes de salvar
            try {
                aluno.setResponsavel(responsavelRepository.save(responsavel));
            } catch (jakarta.validation.ConstraintViolationException e) {
                logger.error("Erro de validação ao salvar responsável - CPF: '{}', Erros: {}", 
                    responsavel.getCpf(), e.getConstraintViolations());
                throw new BusinessException("Erro ao salvar dados do responsável: CPF inválido. Por favor, verifique o CPF informado.");
            }
            // Forçar checkbox como true se responsável foi criado/atualizado
            aluno.setResponsavelFinanceiro(true);
            logger.info("Responsável atualizado para aluno ID {}: {} (CPF: {})", 
                id, responsavel.getNomeCompleto(), responsavel.getCpf());
        } else {
            // Só remover responsável se checkbox explicitamente desmarcado E não há nome preenchido
            if (!alunoDTO.responsavelFinanceiro() && 
                (alunoDTO.nomeResponsavel() == null || alunoDTO.nomeResponsavel().trim().isEmpty())) {
                // Se havia responsável antes e agora não deve ter, remover referência
                if (aluno.getResponsavel() != null) {
                    Responsavel responsavelAntigo = aluno.getResponsavel();
                    aluno.setResponsavel(null);
                    // Verificar se há outros alunos usando este responsável antes de deletar
                    List<Aluno> alunosComResponsavel = alunoRepository.findAll().stream()
                        .filter(a -> a.getResponsavel() != null && 
                                    a.getResponsavel().getId() != null &&
                                    a.getResponsavel().getId().equals(responsavelAntigo.getId()) &&
                                    !a.getId().equals(id))
                        .collect(Collectors.toList());
                    
                    if (alunosComResponsavel.isEmpty()) {
                        // Nenhum outro aluno usa este responsável, pode deletar
                        try {
                            responsavelRepository.deleteById(responsavelAntigo.getId());
                            logger.debug("Responsável órfão deletado: ID {}", responsavelAntigo.getId());
                        } catch (Exception e) {
                            logger.warn("Não foi possível deletar responsável ID {}: {}", responsavelAntigo.getId(), e.getMessage());
                        }
                    } else {
                        logger.debug("Responsável ID {} mantido (usado por {} outro(s) aluno(s))", 
                            responsavelAntigo.getId(), alunosComResponsavel.size());
                    }
                }
                logger.debug("Responsável removido do aluno ID {} - checkbox: {}, nome: {}", 
                    id, alunoDTO.responsavelFinanceiro(), alunoDTO.nomeResponsavel());
            } else {
                // Manter responsável existente se houver nome mas checkbox não foi enviado
                if (aluno.getResponsavel() != null) {
                    logger.debug("Mantendo responsável existente para aluno ID {} (checkbox: {}, nome: '{}')", 
                        id, alunoDTO.responsavelFinanceiro(), alunoDTO.nomeResponsavel());
                }
            }
        }

        if (alunoDTO.turmaIds() != null && !alunoDTO.turmaIds().isEmpty()) {
            List<Turma> turmas = turmaRepository.findAllById(alunoDTO.turmaIds());
            
            // Verificar se alguma turma está fechada
            for (Turma turma : turmas) {
                if ("FECHADA".equals(turma.getSituacaoTurma())) {
                    throw new BusinessException("Não é possível vincular alunos a turmas fechadas. A turma '" + turma.getNomeTurma() + "' está fechada para novos alunos.");
                }
            }
            
            aluno.setTurmas(turmas);
        } else {
            aluno.setTurmas(Collections.emptyList());
        }
        
        // Atualizar status baseado nas turmas (não usar o valor do DTO)
        atualizarStatusBaseadoEmTurmas(aluno);

        return alunoRepository.save(aluno);
    }

    @Transactional
    public void deletarAluno(Long id) {
        alunoRepository.deleteById(id);
    }

    

    @Transactional
    public void associarTurma(Long alunoId, Long turmaId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + alunoId));
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + turmaId));

        // Verificar se a turma está fechada
        if ("FECHADA".equals(turma.getSituacaoTurma())) {
            throw new BusinessException("Não é possível vincular alunos a turmas fechadas");
        }

        if (aluno.getTurmas() == null || !aluno.getTurmas().contains(turma)) {
            if (aluno.getTurmas() == null) {
                aluno.setTurmas(new java.util.ArrayList<>());
            }
            aluno.getTurmas().add(turma);
            // Atualizar status para ATIVO ao associar turma
            atualizarStatusBaseadoEmTurmas(aluno);
            alunoRepository.save(aluno);
        }
    }

    @Transactional
    public void removerTurma(Long alunoId, Long turmaId) {
        Aluno aluno = alunoRepository.findByIdWithTurmasAndResponsavel(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + alunoId));
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + turmaId));

        if (aluno.getTurmas() != null && aluno.getTurmas().contains(turma)) {
            aluno.getTurmas().remove(turma);
            // Atualizar status automaticamente baseado nas turmas restantes
            // Se não tiver mais turmas, ficará INATIVO
            atualizarStatusBaseadoEmTurmas(aluno);
            alunoRepository.save(aluno);
            logger.info("Aluno ID {} removido da turma ID {}", alunoId, turmaId);
        } else {
            logger.warn("Tentativa de remover aluno ID {} da turma ID {}, mas o aluno não está associado a esta turma", alunoId, turmaId);
            throw new BusinessException("O aluno não está associado a esta turma");
        }
    }

    @Transactional
    public void alterarSituacao(Long alunoId, String situacao) {
        Aluno aluno = alunoRepository.findByIdWithTurmasAndResponsavel(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + alunoId));
        
        // O status é automaticamente gerenciado pela relação com turmas
        // Se o aluno tem turmas, será ATIVO; se não tem, será INATIVO
        // Ignorar o parâmetro situacao e atualizar baseado nas turmas
        logger.warn("Tentativa de alterar situação manualmente para aluno ID {}. O status será atualizado automaticamente baseado nas turmas.", alunoId);
        atualizarStatusBaseadoEmTurmas(aluno);
        alunoRepository.save(aluno);
    }

    /**
     * Atualiza o status de todos os alunos que perderam a relação com uma turma.
     * Chamado quando uma turma é deletada.
     */
    @Transactional
    public void atualizarStatusAlunosSemTurmas() {
        try {
            List<Aluno> todosAlunos = alunoRepository.findAllWithTurmasAndResponsavel();
            int atualizados = 0;
            
            for (Aluno aluno : todosAlunos) {
                if (aluno.getTurmas() == null || aluno.getTurmas().isEmpty()) {
                    if (!"INATIVO".equals(aluno.getSituacao())) {
                        aluno.setSituacao("INATIVO");
                        alunoRepository.save(aluno);
                        atualizados++;
                        logger.debug("Aluno ID {} atualizado para INATIVO (sem turmas)", aluno.getId());
                    }
                }
            }
            
            if (atualizados > 0) {
                logger.info("Status atualizado para {} aluno(s) que ficaram sem turmas", atualizados);
            }
        } catch (Exception e) {
            logger.error("Erro ao atualizar status de alunos sem turmas: ", e);
            throw new BusinessException("Erro ao atualizar status de alunos: " + e.getMessage());
        }
    }
}