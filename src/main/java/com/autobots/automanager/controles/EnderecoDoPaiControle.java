package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import com.autobots.automanager.entitades.Endereco;
import com.autobots.automanager.repositorios.RepositorioEndereco;

/**
 * Endereço único de uma entidade: GET, PUT (cria ou atualiza) e DELETE em
 * /pais/{paiId}/endereco.
 */
public abstract class EnderecoDoPaiControle<P> {

	private final Class<?> controladorPai;

	@Autowired
	private RepositorioEndereco repositorioEndereco;

	protected EnderecoDoPaiControle(Class<?> controladorPai) {
		this.controladorPai = controladorPai;
	}

	protected abstract JpaRepository<P, Long> repositorioPai();

	protected abstract String nomePai();

	protected abstract Endereco endereco(P pai);

	protected abstract void definirEndereco(P pai, Endereco endereco);

	private P buscarPai(Long paiId) {
		return repositorioPai().findById(paiId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, nomePai() + " " + paiId + " não encontrado(a)"));
	}

	private EntityModel<Endereco> modelo(Long paiId, Endereco endereco) {
		WebMvcLinkBuilder pai = linkTo(controladorPai).slash(paiId);
		EntityModel<Endereco> modelo = EntityModel.of(endereco);
		modelo.add(pai.slash("endereco").withSelfRel());
		modelo.add(pai.withRel("pai"));
		return modelo;
	}

	@GetMapping
	public ResponseEntity<EntityModel<Endereco>> obter(@PathVariable("paiId") Long paiId) {
		Endereco endereco = endereco(buscarPai(paiId));
		if (endereco == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não cadastrado");
		}
		return ResponseEntity.ok(modelo(paiId, endereco));
	}

	@PutMapping
	@Transactional
	public ResponseEntity<EntityModel<Endereco>> salvar(@PathVariable("paiId") Long paiId,
			@RequestBody Endereco dados) {
		P pai = buscarPai(paiId);
		Endereco atual = endereco(pai);
		if (atual == null) {
			dados.setId(null);
			atual = repositorioEndereco.save(dados);
			definirEndereco(pai, atual);
			repositorioPai().save(pai);
		} else {
			if (dados.getEstado() != null) {
				atual.setEstado(dados.getEstado());
			}
			if (dados.getCidade() != null) {
				atual.setCidade(dados.getCidade());
			}
			if (dados.getBairro() != null) {
				atual.setBairro(dados.getBairro());
			}
			if (dados.getRua() != null) {
				atual.setRua(dados.getRua());
			}
			if (dados.getNumero() != null) {
				atual.setNumero(dados.getNumero());
			}
			if (dados.getCodigoPostal() != null) {
				atual.setCodigoPostal(dados.getCodigoPostal());
			}
			if (dados.getInformacoesAdicionais() != null) {
				atual.setInformacoesAdicionais(dados.getInformacoesAdicionais());
			}
			repositorioEndereco.save(atual);
		}
		return ResponseEntity.ok(modelo(paiId, atual));
	}

	@DeleteMapping
	@Transactional
	public ResponseEntity<Void> excluir(@PathVariable("paiId") Long paiId) {
		P pai = buscarPai(paiId);
		if (endereco(pai) == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não cadastrado");
		}
		definirEndereco(pai, null);
		repositorioPai().save(pai);
		return ResponseEntity.noContent().build();
	}
}
