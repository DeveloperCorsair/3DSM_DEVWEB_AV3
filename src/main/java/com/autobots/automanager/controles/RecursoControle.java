package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/**
 * CRUD completo para uma entidade. Cada controlador concreto só
 * informa o repositório e as regras específicas da entidade.
 *
 * GET /recurso, GET /recurso/{id}, POST /recurso, PUT /recurso/{id} e DELETE
 * /recurso/{id}.
 */
public abstract class RecursoControle<T> {

	private final Class<?> controlador;

	protected RecursoControle(Class<?> controlador) {
		this.controlador = controlador;
	}

	// pontos de extensão 

	protected abstract JpaRepository<T, Long> repositorio();

	protected abstract String nome();

	protected abstract Long idDe(T entidade);

	protected abstract void definirId(T entidade, Long id);

	/** Copia os campos enviados no PUT para a entidade já cadastrada. */
	protected abstract void copiarCampos(T destino, T origem);

	/** Valida/completa a entidade antes de gravar o POST. */
	protected void antesDeCriar(T nova) {
	}

	/** Verifica regras e desfaz associações antes de excluir. */
	protected void antesDeExcluir(T existente) {
	}

	/** Links extras (navegação para recursos relacionados). */
	protected void adicionarLinks(EntityModel<T> modelo, WebMvcLinkBuilder item) {
	}

	protected List<T> listarEntidades(Map<String, String> filtros) {
		return repositorio().findAll();
	}

	// apoio 

	protected WebMvcLinkBuilder raiz() {
		return linkTo(controlador);
	}

	protected T buscar(Long id) {
		return repositorio().findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, nome() + " " + id + " não encontrado(a)"));
	}

	protected EntityModel<T> modelo(T entidade) {
		WebMvcLinkBuilder item = raiz().slash(idDe(entidade));
		EntityModel<T> modelo = EntityModel.of(entidade);
		modelo.add(item.withSelfRel());
		modelo.add(raiz().withRel("todos"));
		adicionarLinks(modelo, item);
		return modelo;
	}

	// endpoints 

	@GetMapping
	public ResponseEntity<CollectionModel<EntityModel<T>>> listar(@RequestParam Map<String, String> filtros) {
		List<EntityModel<T>> modelos = new ArrayList<>();
		for (T entidade : listarEntidades(filtros)) {
			modelos.add(modelo(entidade));
		}
		CollectionModel<EntityModel<T>> colecao = CollectionModel.of(modelos);
		colecao.add(raiz().withSelfRel());
		return ResponseEntity.ok(colecao);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<T>> obter(@PathVariable("id") Long id) {
		return ResponseEntity.ok(modelo(buscar(id)));
	}

	@PostMapping
	@Transactional
	public ResponseEntity<EntityModel<T>> criar(@RequestBody T nova) {
		definirId(nova, null);
		antesDeCriar(nova);
		T salva = repositorio().save(nova);
		EntityModel<T> modelo = modelo(salva);
		return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
	}

	@PutMapping("/{id}")
	@Transactional
	public ResponseEntity<EntityModel<T>> atualizar(@PathVariable("id") Long id, @RequestBody T dados) {
		T existente = buscar(id);
		copiarCampos(existente, dados);
		return ResponseEntity.ok(modelo(repositorio().save(existente)));
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
		T existente = buscar(id);
		antesDeExcluir(existente);
		repositorio().delete(existente);
		return ResponseEntity.noContent().build();
	}
}
