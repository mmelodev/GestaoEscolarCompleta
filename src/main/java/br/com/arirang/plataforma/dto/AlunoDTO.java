package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Endereco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import br.com.arirang.plataforma.validation.CPF;
import br.com.arirang.plataforma.validation.Telefone;
import java.time.LocalDate;
import java.util.List;

public record AlunoDTO(
		Long id,
		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
		String nomeCompleto,
		@Email(message = "Email inválido")
		@Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
		String email,
		@CPF(message = "CPF inválido")
		@Size(max = 14, message = "CPF deve ter no máximo 14 caracteres")
		String cpf,
		@Size(max = 20)
		String rg,
		@Size(max = 200, message = "Órgão expedidor deve ter no máximo 200 caracteres")
		String orgaoExpeditorRg,
		@Size(max = 60)
		String nacionalidade,
		@Size(max = 2)
		String uf,
		@Telefone(message = "Telefone inválido")
		@Size(max = 20)
		String telefone,
		@NotNull(message = "Data de nascimento é obrigatória")
		LocalDate dataNascimento,
		@Size(max = 150)
		String nomeSocial,
		@Size(max = 100)
		String apelido,
		@Size(max = 30)
		String genero,
		@Size(max = 30)
		String situacao,
		@Size(max = 60)
		String ultimoNivel,
		@NotNull(message = "Endereço é obrigatório")
		Endereco endereco,
		@Size(max = 60)
		String grauParentesco,
		boolean responsavelFinanceiro,
		@Size(max = 150)
		String nomeResponsavel,
		@Size(max = 14, message = "CPF do responsável deve ter no máximo 14 caracteres")
		String cpfResponsavel,
		@Size(max = 20)
		String telefoneResponsavel,
		@Email(message = "Email do responsável inválido")
		@Size(max = 150, message = "Email do responsável deve ter no máximo 150 caracteres")
		String emailResponsavel,
		List<Long> turmaIds,
		List<String> turmaNomes,
		List<String> idiomas
) {
	public Aluno toEntity() {
		Aluno aluno = new Aluno();
		aluno.setId(this.id);
		aluno.setNomeCompleto(this.nomeCompleto);
		aluno.setEmail(this.email);
		aluno.setCpf(this.cpf);
		aluno.setRg(this.rg);
		aluno.setOrgaoExpeditorRg(this.orgaoExpeditorRg);
		aluno.setNacionalidade(this.nacionalidade);
		aluno.setUf(this.uf);
		aluno.setTelefone(this.telefone);
		aluno.setDataNascimento(this.dataNascimento);
		aluno.setNomeSocial(this.nomeSocial);
		aluno.setGenero(this.genero);
		aluno.setSituacao(this.situacao);
		aluno.setUltimoNivel(this.ultimoNivel);
		aluno.setEndereco(this.endereco);
		// Configurar responsável e turmas conforme necessário (ex.: via service)
		return aluno;
	}
}