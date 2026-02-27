package br.com.arirang.plataforma.mapper;

import br.com.arirang.plataforma.dto.ContratoDTO;
import br.com.arirang.plataforma.entity.Contrato;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContratoMapper {

    @Mapping(target = "alunoId", source = "aluno.id")
    @Mapping(target = "alunoNome", source = "aluno.nomeCompleto")
    @Mapping(target = "turmaId", source = "turma.id")
    @Mapping(target = "turmaNome", source = "turma.nomeTurma")
    ContratoDTO toDto(Contrato contrato);

    @Mapping(target = "aluno", ignore = true)
    @Mapping(target = "turma", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Contrato toEntity(ContratoDTO contratoDTO);

    /**
     * Converte para DTO sem carregar relacionamentos (para performance)
     */
    @Mapping(target = "alunoId", source = "aluno.id")
    @Mapping(target = "alunoNome", expression = "java(contrato.getAluno() != null ? contrato.getAluno().getNomeCompleto() : null)")
    @Mapping(target = "turmaId", source = "turma.id")
    @Mapping(target = "turmaNome", expression = "java(contrato.getTurma() != null ? contrato.getTurma().getNomeTurma() : null)")
    ContratoDTO toDtoLazy(Contrato contrato);
}
