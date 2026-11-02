package com.autobots.automanager.controles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Busca entidades já cadastradas a partir de um id, devolvendo o erro HTTP
 * adequado quando não existem.
 */
@Component
public class Resolvedor {

	/** URL: se não existir, 404. */
	public <T> T obter(JpaRepository<T, Long> repositorio, Long id, String nome) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, nome + " " + id + " não encontrado(a)"));
	}

	/** Recurso citado dentro do JSON enviado: se faltar ou não existir, 400. */
	public <T> T referenciar(JpaRepository<T, Long> repositorio, Long id, String campo) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o id de '" + campo + "'");
		}
		return repositorio.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
				"'" + campo + "' com id " + id + " não existe"));
	}
}
