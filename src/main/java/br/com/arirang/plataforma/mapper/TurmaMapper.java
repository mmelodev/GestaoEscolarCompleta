package br.com.arirang.plataforma.mapper;

import br.com.arirang.plataforma.dto.TurmaDTO;
import br.com.arirang.plataforma.entity.Turma;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface TurmaMapper {

    @Mapping(target = "alunoIds", expression = "java(turma.getAlunos() != null ? turma.getAlunos().stream().map(br.com.arirang.plataforma.entity.Aluno::getId).collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList())")
    TurmaDTO toDto(Turma turma);

    @Mapping(target = "alunos", ignore = true)
    Turma toEntity(TurmaDTO dto);
}


