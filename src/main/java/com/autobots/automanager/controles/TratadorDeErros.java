package com.autobots.automanager.controles;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class TratadorDeErros {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, Object>> tratarStatus(ResponseStatusException excecao) {
		HttpStatus status = excecao.getStatus();
		return ResponseEntity.status(status).body(corpo(status, excecao.getReason()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> tratarIntegridade(DataIntegrityViolationException excecao) {
		String detalhe = excecao.getMostSpecificCause().getMessage();
		return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo(HttpStatus.CONFLICT,
				"Os dados violam uma regra do banco (campo obrigatório ausente, valor duplicado ou registro em uso): "
						+ detalhe));
	}

	private Map<String, Object> corpo(HttpStatus status, String mensagem) {
		Map<String, Object> corpo = new LinkedHashMap<>();
		corpo.put("timestamp", new Date());
		corpo.put("status", status.value());
		corpo.put("erro", status.getReasonPhrase());
		corpo.put("mensagem", mensagem);
		return corpo;
	}
}
