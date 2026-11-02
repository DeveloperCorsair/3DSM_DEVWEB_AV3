package com.autobots.automanager.entitades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

/**
 * Credencial é abstrata, então o JSON precisa informar qual é o tipo concreto.
 * Exemplo: { "tipo": "USUARIO_SENHA", "nomeUsuario": "joao", "senha": "123" }
 */
@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
@JsonSubTypes({
		@JsonSubTypes.Type(value = CredencialUsuarioSenha.class, name = "USUARIO_SENHA"),
		@JsonSubTypes.Type(value = CredencialCodigoBarra.class, name = "CODIGO_BARRA") })
public abstract class Credencial {
	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private Date criacao;
	@Column()
	private Date ultimoAcesso;
	@Column(nullable = false)
	private boolean inativo;
}
