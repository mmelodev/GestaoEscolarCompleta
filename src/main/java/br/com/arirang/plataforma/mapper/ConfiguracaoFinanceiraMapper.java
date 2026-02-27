package br.com.arirang.plataforma.mapper;

import br.com.arirang.plataforma.dto.ConfiguracaoFinanceiraDTO;
import br.com.arirang.plataforma.entity.ConfiguracaoFinanceira;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConfiguracaoFinanceiraMapper {

    ConfiguracaoFinanceiraDTO toDto(ConfiguracaoFinanceira configuracao);

    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "valorFromBoolean", ignore = true)
    @Mapping(target = "valorFromDecimal", ignore = true)
    @Mapping(target = "valorFromInteger", ignore = true)
    ConfiguracaoFinanceira toEntity(ConfiguracaoFinanceiraDTO configuracaoDTO);
}
