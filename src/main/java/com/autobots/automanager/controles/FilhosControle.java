package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

/**
 * CRUD de uma coleção de entidades que pertencem a outra (telefones de um
 * usuário, e-mails, documentos...). A URL base fica /pais/{paiId}/recurso.
 */
public abstract class FilhosControle<P, F> {

	private final Class<?> controladorPai;
	private final String recurso;

	protected FilhosControle(Class<?> controladorPai, String recurso) {
		this.controladorPai = controladorPai;
		this.recurso = recurso;
	}

	protected abstract JpaRepository<P, Long> repositorioPai();

	protected abstract JpaRepository<F, Long> repositorioFilho();

	protected abstract String nomePai();

	protected abstract Set<F> filhos(P pai);

	protected abstract Long idDe(F filho);

	protected abstract void definirId(F filho, Long id);

	protected abstract void copiarCampos(F destino, F origem);

	protected void antesDeCriar(F novo) {
	}

	private P buscarPai(Long paiId) {
		return repositorioPai().findById(paiId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, nomePai() + " " + paiId + " não encontrado(a)"));
	}

	private F buscarFilho(P pai, Long id) {
		for (F filho : filhos(pai)) {
			if (id.equals(idDe(filho))) {
				return filho;
			}
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, recurso + " " + id + " não encontrado(a)");
	}

	private WebMvcLinkBuilder linkPai(Long paiId) {
		return linkTo(controladorPai).slash(paiId);
	}

	private WebMvcLinkBuilder linkColecao(Long paiId) {
		return linkPai(paiId).slash(recurso);
	}

	private EntityModel<F> modelo(Long paiId, F filho) {
		EntityModel<F> modelo = EntityModel.of(filho);
		modelo.add(linkColecao(paiId).slash(idDe(filho)).withSelfRel());
		modelo.add(linkColecao(paiId).withRel("todos"));
		modelo.add(linkPai(paiId).withRel("pai"));
		return modelo;
	}

	@GetMapping
	public ResponseEntity<CollectionModel<EntityModel<F>>> listar(@PathVariable("paiId") Long paiId) {
		P pai = buscarPai(paiId);
		List<EntityModel<F>> modelos = new ArrayList<>();
		for (F filho : filhos(pai)) {
			modelos.add(modelo(paiId, filho));
		}
		CollectionModel<EntityModel<F>> colecao = CollectionModel.of(modelos);
		colecao.add(linkColecao(paiId).withSelfRel());
		colecao.add(linkPai(paiId).withRel("pai"));
		return ResponseEntity.ok(colecao);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<F>> obter(@PathVariable("paiId") Long paiId, @PathVariable("id") Long id) {
		return ResponseEntity.ok(modelo(paiId, buscarFilho(buscarPai(paiId), id)));
	}

	@PostMapping
	@Transactional
	public ResponseEntity<EntityModel<F>> criar(@PathVariable("paiId") Long paiId, @RequestBody F novo) {
		P pai = buscarPai(paiId);
		definirId(novo, null);
		antesDeCriar(novo);
		F salvo = repositorioFilho().save(novo);
		filhos(pai).add(salvo);
		repositorioPai().save(pai);
		EntityModel<F> modelo = modelo(paiId, salvo);
		return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
	}

	@PutMapping("/{id}")
	@Transactional
	public ResponseEntity<EntityModel<F>> atualizar(@PathVariable("paiId") Long paiId, @PathVariable("id") Long id,
			@RequestBody F dados) {
		P pai = buscarPai(paiId);
		F existente = buscarFilho(pai, id);
		copiarCampos(existente, dados);
		repositorioPai().save(pai);
		return ResponseEntity.ok(modelo(paiId, existente));
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<Void> excluir(@PathVariable("paiId") Long paiId, @PathVariable("id") Long id) {
		P pai = buscarPai(paiId);
		buscarFilho(pai, id);
		filhos(pai).removeIf(filho -> id.equals(idDe(filho)));
		repositorioPai().save(pai);
		return ResponseEntity.noContent().build();
	}
}
