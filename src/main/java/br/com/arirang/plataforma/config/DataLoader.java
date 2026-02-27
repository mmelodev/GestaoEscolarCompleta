package br.com.arirang.plataforma.config;

import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Contrato;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.entity.StatusContrato;
import br.com.arirang.plataforma.entity.Usuario;
import br.com.arirang.plataforma.enums.Turno;
import br.com.arirang.plataforma.enums.Formato;
import br.com.arirang.plataforma.enums.Modalidade;
import br.com.arirang.plataforma.repository.AlunoRepository;
import br.com.arirang.plataforma.repository.TurmaRepository;
import br.com.arirang.plataforma.repository.ContratoRepository;
import br.com.arirang.plataforma.service.UsuarioService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private TurmaRepository turmaRepository;
    
    @Autowired
    private ContratoRepository contratoRepository;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Verificar se já existem dados
        if (alunoRepository.count() > 0 && contratoRepository.count() > 0) {
            System.out.println("✅ Dados já existem no banco. Pulando carregamento inicial.");
            return;
        }

        System.out.println("🚀 Carregando dados iniciais...");

        // Criar usuário padrão em transação separada para não afetar transação principal
        transactionTemplate.executeWithoutResult(status -> {
            try {
                if (!usuarioService.buscarPorUsername("admin").isPresent()) {
                    usuarioService.criarUsuario("admin", "admin@arirang.com", "admin123", "Administrador", Usuario.Role.ADMIN);
                    System.out.println("✅ Usuário admin criado com sucesso!");
                } else {
                    System.out.println("ℹ️ Usuário admin já existe. Pulando criação.");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Erro ao verificar/criar usuário admin: " + e.getMessage());
                // Não re-lançar exception, apenas logar
            }
        });

        // Criar turmas
        Turma turmaZebra = new Turma();
        turmaZebra.setNomeTurma("Zebra");
        turmaZebra.setNivelProficiencia("Iniciante");
        turmaZebra.setDiaTurma("Segunda");
        turmaZebra.setTurno(Turno.MATUTINO);
        turmaZebra.setFormato(Formato.PRESENCIAL);
        turmaZebra.setModalidade(Modalidade.REGULAR);
        turmaZebra.setRealizador("Arirang");
        turmaZebra.setHoraInicio("08:00");
        turmaZebra.setHoraTermino("10:00");
        turmaZebra.setAnoSemestre("2024/1");
        turmaZebra.setCargaHorariaTotal(40);
        turmaZebra.setInicioTurma(LocalDate.now().minusDays(30));
        turmaZebra.setTerminoTurma(LocalDate.now().plusDays(60));
        turmaZebra.setSituacaoTurma("Ativa");

        Turma turmaLeao = new Turma();
        turmaLeao.setNomeTurma("Leão");
        turmaLeao.setNivelProficiencia("Intermediário");
        turmaLeao.setDiaTurma("Terça");
        turmaLeao.setTurno(Turno.VESPERTINO);
        turmaLeao.setFormato(Formato.PRESENCIAL);
        turmaLeao.setModalidade(Modalidade.REGULAR);
        turmaLeao.setRealizador("Arirang");
        turmaLeao.setHoraInicio("14:00");
        turmaLeao.setHoraTermino("16:00");
        turmaLeao.setAnoSemestre("2024/1");
        turmaLeao.setCargaHorariaTotal(40);
        turmaLeao.setInicioTurma(LocalDate.now().minusDays(30));
        turmaLeao.setTerminoTurma(LocalDate.now().plusDays(60));
        turmaLeao.setSituacaoTurma("Ativa");

        // Salvar turmas primeiro
        turmaZebra = turmaRepository.save(turmaZebra);
        turmaLeao = turmaRepository.save(turmaLeao);
        
        // Flush para garantir que as turmas estão persistidas
        turmaRepository.flush();
        
        // Buscar turmas do banco para ter entidades managed (não detached)
        turmaZebra = turmaRepository.findById(turmaZebra.getId()).orElse(turmaZebra);
        turmaLeao = turmaRepository.findById(turmaLeao.getId()).orElse(turmaLeao);

        // Criar alunos SEM associar turmas primeiro (para evitar cascade PERSIST)
        Aluno aluno1 = new Aluno();
        aluno1.setNomeCompleto("Murilo Melo Teste");
        aluno1.setEmail("murilo@teste.com");
        aluno1.setTelefone("11999999999"); // Formato numérico: DDD + 9 + 8 dígitos (celular)
        aluno1.setDataNascimento(LocalDate.of(1990, 5, 15));

        Aluno aluno2 = new Aluno();
        aluno2.setNomeCompleto("João Silva");
        aluno2.setEmail("joao@teste.com");
        aluno2.setTelefone("11988888888"); // Formato numérico: DDD + 9 + 8 dígitos (celular)
        aluno2.setDataNascimento(LocalDate.of(1992, 8, 20));

        Aluno aluno3 = new Aluno();
        aluno3.setNomeCompleto("Maria Santos");
        aluno3.setEmail("maria@teste.com");
        aluno3.setTelefone("11977777777"); // Formato numérico: DDD + 9 + 8 dígitos (celular)
        aluno3.setDataNascimento(LocalDate.of(1988, 3, 10));

        // Salvar alunos primeiro (sem turmas)
        aluno1 = alunoRepository.save(aluno1);
        aluno2 = alunoRepository.save(aluno2);
        aluno3 = alunoRepository.save(aluno3);
        
        // Flush para garantir que os alunos estão persistidos
        alunoRepository.flush();
        
        // Buscar alunos e turmas do banco para ter entidades managed (não detached)
        aluno1 = alunoRepository.findById(aluno1.getId()).orElse(aluno1);
        aluno2 = alunoRepository.findById(aluno2.getId()).orElse(aluno2);
        aluno3 = alunoRepository.findById(aluno3.getId()).orElse(aluno3);
        
        // Usar getReference() para obter proxies managed sem tocar no banco
        // Isso evita o erro de detached entity ao associar turmas
        Turma turmaZebraManaged = entityManager.getReference(Turma.class, turmaZebra.getId());
        Turma turmaLeaoManaged = entityManager.getReference(Turma.class, turmaLeao.getId());
        
        // AGORA associar turmas aos alunos usando entidades managed (proxies)
        // Usar ArrayList em vez de Arrays.asList() para permitir modificação pelo Hibernate
        List<Turma> turmasAluno1 = new ArrayList<>();
        turmasAluno1.add(turmaZebraManaged);
        aluno1.setTurmas(turmasAluno1);
        
        List<Turma> turmasAluno2 = new ArrayList<>();
        turmasAluno2.add(turmaLeaoManaged);
        aluno2.setTurmas(turmasAluno2);
        
        List<Turma> turmasAluno3 = new ArrayList<>();
        turmasAluno3.add(turmaZebraManaged);
        turmasAluno3.add(turmaLeaoManaged);
        aluno3.setTurmas(turmasAluno3);
        
        // Salvar alunos novamente para persistir as associações (agora só MERGE, não PERSIST)
        aluno1 = alunoRepository.save(aluno1);
        aluno2 = alunoRepository.save(aluno2);
        aluno3 = alunoRepository.save(aluno3);
        
        // Buscar novamente para usar nos contratos
        aluno1 = alunoRepository.findById(aluno1.getId()).orElse(aluno1);
        aluno2 = alunoRepository.findById(aluno2.getId()).orElse(aluno2);
        aluno3 = alunoRepository.findById(aluno3.getId()).orElse(aluno3);
        turmaZebra = turmaRepository.findById(turmaZebra.getId()).orElse(turmaZebra);
        turmaLeao = turmaRepository.findById(turmaLeao.getId()).orElse(turmaLeao);

        // Criar contratos para os alunos
        LocalDate hoje = LocalDate.now();
        LocalDate inicioVigencia = hoje; // Vigência inicia hoje
        LocalDate fimVigencia = hoje.plusMonths(6); // Vigência de 6 meses
        
        // Gerar números de contrato únicos
        String ano = String.valueOf(hoje.getYear());
        String mes = String.format("%02d", hoje.getMonthValue());
        long countInicial = contratoRepository.count();
        
        Contrato contrato1 = new Contrato();
        contrato1.setAluno(aluno1);
        contrato1.setTurma(turmaZebra);
        contrato1.setDataContrato(hoje);
        contrato1.setDataInicioVigencia(inicioVigencia);
        contrato1.setDataFimVigencia(fimVigencia);
        contrato1.setNumeroContrato(String.format("CTR%s%s%04d", ano, mes, countInicial + 1));
        contrato1.setValorMatricula(new java.math.BigDecimal("50.00"));
        contrato1.setValorMensalidade(new java.math.BigDecimal("150.00"));
        contrato1.setNumeroParcelas(6);
        contrato1.setSituacaoContrato(StatusContrato.ATIVO.name());
        contratoRepository.save(contrato1);

        Contrato contrato2 = new Contrato();
        contrato2.setAluno(aluno2);
        contrato2.setTurma(turmaLeao);
        contrato2.setDataContrato(hoje);
        contrato2.setDataInicioVigencia(inicioVigencia);
        contrato2.setDataFimVigencia(fimVigencia);
        contrato2.setNumeroContrato(String.format("CTR%s%s%04d", ano, mes, countInicial + 2));
        contrato2.setValorMatricula(new java.math.BigDecimal("60.00"));
        contrato2.setValorMensalidade(new java.math.BigDecimal("180.00"));
        contrato2.setNumeroParcelas(6);
        contrato2.setSituacaoContrato(StatusContrato.ATIVO.name());
        contratoRepository.save(contrato2);

        Contrato contrato3 = new Contrato();
        contrato3.setAluno(aluno3);
        contrato3.setTurma(turmaZebra);
        contrato3.setDataContrato(hoje);
        contrato3.setDataInicioVigencia(inicioVigencia);
        contrato3.setDataFimVigencia(fimVigencia);
        contrato3.setNumeroContrato(String.format("CTR%s%s%04d", ano, mes, countInicial + 3));
        contrato3.setValorMatricula(new java.math.BigDecimal("50.00"));
        contrato3.setValorMensalidade(new java.math.BigDecimal("150.00"));
        contrato3.setNumeroParcelas(6);
        contrato3.setSituacaoContrato(StatusContrato.ATIVO.name());
        contratoRepository.save(contrato3);

        // Flush final para garantir que todas as operações sejam persistidas
        entityManager.flush();
        
        System.out.println("✅ Dados iniciais carregados com sucesso!");
        System.out.println("📊 Alunos criados: " + alunoRepository.count());
        System.out.println("📊 Turmas criadas: " + turmaRepository.count());
        System.out.println("📊 Contratos criados: " + contratoRepository.count());
    }
}
