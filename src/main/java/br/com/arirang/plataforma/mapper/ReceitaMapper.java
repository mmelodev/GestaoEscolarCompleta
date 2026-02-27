package br.com.arirang.plataforma.mapper;

import br.com.arirang.plataforma.dto.ReceitaDTO;
import br.com.arirang.plataforma.entity.Receita;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReceitaMapper {

    @Mapping(target = "contratoId", source = "contrato.id")
    @Mapping(target = "contratoNumero", source = "contrato.numeroContrato")
    @Mapping(target = "alunoId", source = "aluno.id")
    @Mapping(target = "alunoNome", source = "aluno.nomeCompleto")
    @Mapping(target = "diasAtraso", expression = "java(receita.getDiasAtraso())")
    @Mapping(target = "valorRestante", expression = "java(receita.getValorFinal().subtract(receita.getValorDesconto()))")
    ReceitaDTO toDto(Receita receita);

    @Mapping(target = "contrato", ignore = true)
    @Mapping(target = "aluno", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Receita toEntity(ReceitaDTO receitaDTO);
}
