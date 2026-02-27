package br.com.arirang.plataforma.entity;

import br.com.arirang.plataforma.validation.CPF;
import br.com.arirang.plataforma.validation.Telefone;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "responsaveis")
public class Responsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nomeCompleto;

    @Email
    @Column(length = 150)
    private String email;

    @Telefone(message = "Telefone inválido")
    @Column(length = 20)
    private String telefone;

    @CPF(message = "CPF inválido")
    @Size(max = 14)
    @Column(length = 14)
    private String cpf;

    @Size(max = 20)
    @Column(length = 20)
    private String rg;

    @OneToMany(mappedBy = "responsavel")
    private List<Aluno> alunos;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public List<Aluno> getAlunos() { return alunos; }
    public void setAlunos(List<Aluno> alunos) { this.alunos = alunos; }
}