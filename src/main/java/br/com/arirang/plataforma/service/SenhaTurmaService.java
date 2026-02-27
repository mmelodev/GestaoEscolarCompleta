package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.entity.SenhaTurma;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.repository.SenhaTurmaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class SenhaTurmaService {
    
    private static final Logger logger = LoggerFactory.getLogger(SenhaTurmaService.class);
    
    @Autowired
    private SenhaTurmaRepository senhaTurmaRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Gera uma senha automática para a turma
     * Formato: TUR + ANO + MÊS + DIA + 4 dígitos aleatórios
     * Exemplo: TUR202512091234
     */
    public String gerarSenhaAutomatica(Long turmaId) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dataFormatada = now.format(formatter);
        
        Random random = new Random();
        int numeroAleatorio = 1000 + random.nextInt(9000); // 4 dígitos
        
        String senha = "TUR" + dataFormatada + numeroAleatorio;
        logger.info("Senha gerada para turma ID {}: {}", turmaId, senha);
        return senha;
    }
    
    /**
     * Cria e salva a senha para uma turma
     */
    public SenhaTurma criarSenhaTurma(Long turmaId) {
        String senhaOriginal = gerarSenhaAutomatica(turmaId);
        String senhaHash = passwordEncoder.encode(senhaOriginal);
        
        SenhaTurma senhaTurma = new SenhaTurma();
        senhaTurma.setTurmaId(turmaId);
        senhaTurma.setSenhaHash(senhaHash);
        senhaTurma.setSenhaOriginal(senhaOriginal); // Armazenar temporariamente para exibição
        senhaTurma.setDataCriacao(LocalDateTime.now());
        
        SenhaTurma salva = senhaTurmaRepository.save(senhaTurma);
        logger.info("Senha criada para turma ID {}", turmaId);
        return salva;
    }
    
    /**
     * Valida a senha fornecida para uma turma
     */
    public boolean validarSenhaTurma(Long turmaId, String senha) {
        Optional<SenhaTurma> senhaTurmaOpt = senhaTurmaRepository.findByTurmaId(turmaId);
        
        if (senhaTurmaOpt.isEmpty()) {
            logger.warn("Tentativa de validar senha para turma sem senha cadastrada: {}", turmaId);
            return false;
        }
        
        SenhaTurma senhaTurma = senhaTurmaOpt.get();
        
        // Verificar se está bloqueada
        if (Boolean.TRUE.equals(senhaTurma.getBloqueado())) {
            logger.warn("Tentativa de usar senha bloqueada para turma ID {}", turmaId);
            throw new BusinessException("Senha bloqueada devido a múltiplas tentativas incorretas. Contate o administrador.");
        }
        
        // Validar senha
        boolean senhaValida = passwordEncoder.matches(senha, senhaTurma.getSenhaHash());
        
        if (senhaValida) {
            // Resetar tentativas em caso de sucesso
            senhaTurma.resetarTentativas();
            senhaTurmaRepository.save(senhaTurma);
            logger.info("Senha validada com sucesso para turma ID {}", turmaId);
        } else {
            // Incrementar tentativas falhas
            senhaTurma.incrementarTentativaFalha();
            senhaTurmaRepository.save(senhaTurma);
            logger.warn("Senha inválida para turma ID {}. Tentativas: {}", turmaId, senhaTurma.getTentativasFalhas());
        }
        
        return senhaValida;
    }
    
    /**
     * Recupera a senha original (apenas se ainda estiver armazenada)
     */
    public Optional<String> recuperarSenhaOriginal(Long turmaId) {
        Optional<SenhaTurma> senhaTurmaOpt = senhaTurmaRepository.findByTurmaId(turmaId);
        if (senhaTurmaOpt.isPresent() && senhaTurmaOpt.get().getSenhaOriginal() != null) {
            return Optional.of(senhaTurmaOpt.get().getSenhaOriginal());
        }
        return Optional.empty();
    }
    
    /**
     * Gera nova senha para uma turma (recuperação)
     */
    public String gerarNovaSenha(Long turmaId) {
        String novaSenha = gerarSenhaAutomatica(turmaId);
        String senhaHash = passwordEncoder.encode(novaSenha);
        
        Optional<SenhaTurma> senhaTurmaOpt = senhaTurmaRepository.findByTurmaId(turmaId);
        SenhaTurma senhaTurma;
        
        if (senhaTurmaOpt.isPresent()) {
            senhaTurma = senhaTurmaOpt.get();
            senhaTurma.setSenhaHash(senhaHash);
            senhaTurma.setSenhaOriginal(novaSenha);
            senhaTurma.resetarTentativas();
        } else {
            senhaTurma = new SenhaTurma();
            senhaTurma.setTurmaId(turmaId);
            senhaTurma.setSenhaHash(senhaHash);
            senhaTurma.setSenhaOriginal(novaSenha);
        }
        
        senhaTurmaRepository.save(senhaTurma);
        logger.info("Nova senha gerada para turma ID {}", turmaId);
        return novaSenha;
    }
    
    /**
     * Verifica se a turma tem senha cadastrada
     */
    public boolean temSenhaCadastrada(Long turmaId) {
        return senhaTurmaRepository.existsByTurmaId(turmaId);
    }
    
    /**
     * Verifica se a turma está no período de graça (< 24h)
     */
    public boolean estaNoPeriodoGraca(Long turmaId) {
        Optional<SenhaTurma> senhaTurmaOpt = senhaTurmaRepository.findByTurmaId(turmaId);
        if (senhaTurmaOpt.isEmpty()) {
            return true; // Se não tem senha, está no período de graça
        }
        
        LocalDateTime dataCriacao = senhaTurmaOpt.get().getDataCriacao();
        LocalDateTime agora = LocalDateTime.now();
        return agora.isBefore(dataCriacao.plusHours(24));
    }
}

