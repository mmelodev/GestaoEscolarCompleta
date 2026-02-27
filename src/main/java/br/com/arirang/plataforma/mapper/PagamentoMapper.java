package br.com.arirang.plataforma.mapper;

import br.com.arirang.plataforma.dto.PagamentoDTO;
import br.com.arirang.plataforma.entity.Pagamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagamentoMapper {

    @Mapping(source = "receita.id", target = "receitaId")
    @Mapping(source = "receita.descricao", target = "receitaDescricao")
    @Mapping(target = "alunoId", expression = "java(pagamento.getReceita() != null && pagamento.getReceita().getAluno() != null ? pagamento.getReceita().getAluno().getId() : null)")
    @Mapping(target = "alunoNome", expression = "java(pagamento.getReceita() != null && pagamento.getReceita().getAluno() != null ? pagamento.getReceita().getAluno().getNomeCompleto() : null)")
    @Mapping(target = "isParcial", expression = "java(pagamento.isParcial())")
    @Mapping(target = "isIntegral", expression = "java(pagamento.isIntegral())")
    @Mapping(target = "valorRestante", expression = "java(pagamento.getValorRestante())")
    PagamentoDTO toDto(Pagamento pagamento);

    @Mapping(target = "receita", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Pagamento toEntity(PagamentoDTO pagamentoDTO);
}
