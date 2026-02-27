package br.com.arirang.plataforma.util;

import java.util.regex.Pattern;

public class FormatadorUtil {

    /**
     * Formata CPF no padrão XXX.XXX.XXX-XX
     */
    public static String formatarCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return cpf;
        }
        
        String cpfLimpo = cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() == 11) {
            return String.format("%s.%s.%s-%s", 
                cpfLimpo.substring(0, 3),
                cpfLimpo.substring(3, 6),
                cpfLimpo.substring(6, 9),
                cpfLimpo.substring(9, 11));
        }
        return cpf;
    }

    /**
     * Remove formatação do CPF, deixando apenas números
     */
    public static String limparCPF(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("\\D", "");
    }

    /**
     * Formata telefone no padrão (XX) XXXXX-XXXX ou (XX) XXXX-XXXX
     */
    public static String formatarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return telefone;
        }
        
        String telefoneLimpo = telefone.replaceAll("[^\\d+]", "");
        
        // Se for número internacional (começa com +)
        if (telefoneLimpo.startsWith("+")) {
            return telefoneLimpo;
        }
        
        if (telefoneLimpo.length() == 10) {
            // Telefone fixo: (XX) XXXX-XXXX
            return String.format("(%s) %s-%s",
                telefoneLimpo.substring(0, 2),
                telefoneLimpo.substring(2, 6),
                telefoneLimpo.substring(6, 10));
        } else if (telefoneLimpo.length() == 11) {
            // Celular: (XX) XXXXX-XXXX
            return String.format("(%s) %s-%s",
                telefoneLimpo.substring(0, 2),
                telefoneLimpo.substring(2, 7),
                telefoneLimpo.substring(7, 11));
        }
        return telefone;
    }

    /**
     * Remove formatação do telefone, deixando apenas números
     */
    public static String limparTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }
        return telefone.replaceAll("[^\\d+]", "");
    }

    /**
     * Formata CEP no padrão XXXXX-XXX
     */
    public static String formatarCEP(String cep) {
        if (cep == null || cep.trim().isEmpty()) {
            return cep;
        }
        
        String cepLimpo = cep.replaceAll("\\D", "");
        if (cepLimpo.length() == 8) {
            return String.format("%s-%s",
                cepLimpo.substring(0, 5),
                cepLimpo.substring(5, 8));
        }
        return cep;
    }

    /**
     * Remove formatação do CEP, deixando apenas números
     */
    public static String limparCEP(String cep) {
        if (cep == null) {
            return null;
        }
        return cep.replaceAll("\\D", "");
    }

    /**
     * Formata RG no padrão Manaus - XX.XXX.XXX (8 dígitos)
     */
    public static String formatarRG(String rg) {
        if (rg == null || rg.trim().isEmpty()) {
            return rg;
        }
        
        String rgLimpo = rg.replaceAll("\\D", "");
        if (rgLimpo.length() >= 8) {
            // Padrão Manaus: 8 dígitos no formato XX.XXX.XXX
            if (rgLimpo.length() == 8) {
                return String.format("%s.%s.%s",
                    rgLimpo.substring(0, 2),
                    rgLimpo.substring(2, 5),
                    rgLimpo.substring(5, 8));
            } else {
                // Se tiver mais de 8 dígitos, pegar apenas os 8 primeiros
                rgLimpo = rgLimpo.substring(0, 8);
                return String.format("%s.%s.%s",
                    rgLimpo.substring(0, 2),
                    rgLimpo.substring(2, 5),
                    rgLimpo.substring(5, 8));
            }
        }
        return rg;
    }

    /**
     * Remove formatação do RG, deixando apenas números
     */
    public static String limparRG(String rg) {
        if (rg == null) {
            return null;
        }
        return rg.replaceAll("\\D", "");
    }

    /**
     * Valida se uma string contém apenas números
     */
    public static boolean contemApenasNumeros(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches("\\d+", texto);
    }

    /**
     * Capitaliza a primeira letra de cada palavra
     */
    public static String capitalizarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return nome;
        }
        
        String[] palavras = nome.trim().toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        
        for (int i = 0; i < palavras.length; i++) {
            if (palavras[i].length() > 0) {
                palavras[i] = palavras[i].substring(0, 1).toUpperCase() + 
                             palavras[i].substring(1);
            }
            
            if (i > 0) {
                resultado.append(" ");
            }
            resultado.append(palavras[i]);
        }
        
        return resultado.toString();
    }

    /**
     * Remove acentos de uma string
     */
    public static String removerAcentos(String texto) {
        if (texto == null) {
            return null;
        }
        
        return texto
            .replaceAll("[áàâãä]", "a")
            .replaceAll("[éèêë]", "e")
            .replaceAll("[íìîï]", "i")
            .replaceAll("[óòôõö]", "o")
            .replaceAll("[úùûü]", "u")
            .replaceAll("[ç]", "c")
            .replaceAll("[ñ]", "n")
            .replaceAll("[ÁÀÂÃÄ]", "A")
            .replaceAll("[ÉÈÊË]", "E")
            .replaceAll("[ÍÌÎÏ]", "I")
            .replaceAll("[ÓÒÔÕÖ]", "O")
            .replaceAll("[ÚÙÛÜ]", "U")
            .replaceAll("[Ç]", "C")
            .replaceAll("[Ñ]", "N");
    }
}
