package br.com.arirang.plataforma.mapper;

import br.com.arirang.plataforma.dto.AlunoDTO;
import br.com.arirang.plataforma.entity.Aluno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlunoMapper {

    @Mapping(target = "turmaIds", expression = "java(aluno.getTurmas() != null ? aluno.getTurmas().stream().map(br.com.arirang.plataforma.entity.Turma::getId).collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList())")
    @Mapping(target = "turmaNomes", expression = "java(aluno.getTurmas() != null ? aluno.getTurmas().stream().map(br.com.arirang.plataforma.entity.Turma::getNomeTurma).collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList())")
    @Mapping(target = "nomeResponsavel", expression = "java(aluno.getResponsavel() != null ? aluno.getResponsavel().getNomeCompleto() : null)")
    @Mapping(target = "cpfResponsavel", expression = "java(aluno.getResponsavel() != null ? aluno.getResponsavel().getCpf() : null)")
    @Mapping(target = "telefoneResponsavel", expression = "java(aluno.getResponsavel() != null ? aluno.getResponsavel().getTelefone() : null)")
    @Mapping(target = "emailResponsavel", expression = "java(aluno.getResponsavel() != null ? aluno.getResponsavel().getEmail() : null)")
    AlunoDTO toDto(Aluno aluno);

    @Mapping(target = "turmas", ignore = true)
    @Mapping(target = "responsavel", ignore = true)
    Aluno toEntity(AlunoDTO dto);
}


