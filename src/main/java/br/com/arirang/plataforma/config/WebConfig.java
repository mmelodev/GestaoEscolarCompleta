package br.com.arirang.plataforma.config;

import br.com.arirang.plataforma.converter.StringToFormatoConverter;
import br.com.arirang.plataforma.converter.StringToModalidadeConverter;
import br.com.arirang.plataforma.converter.StringToTurnoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private StringToTurnoConverter stringToTurnoConverter;
    
    @Autowired
    private StringToFormatoConverter stringToFormatoConverter;
    
    @Autowired
    private StringToModalidadeConverter stringToModalidadeConverter;
    
    @Autowired
    private ThemeInterceptor themeInterceptor;

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        // Configurar formato ISO para LocalDate (yyyy-MM-dd) - padrão do input type="date"
        registrar.setDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
        registrar.setTimeFormatter(DateTimeFormatter.ISO_LOCAL_TIME);
        registrar.setDateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        registrar.registerFormatters(registry);
        
        // Registrar conversores de String para Enums
        registry.addConverter(stringToTurnoConverter);
        registry.addConverter(stringToFormatoConverter);
        registry.addConverter(stringToModalidadeConverter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(themeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/api/**", "/error");
    }
}