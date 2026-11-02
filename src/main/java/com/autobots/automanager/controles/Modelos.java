package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

public final class Modelos {

	private Modelos() {
	}

	/**
	 * Monta uma coleção HATEOAS em que cada item tem um link "self" apontando para o
	 * seu controlador de origem.
	 */
	public static <T> CollectionModel<EntityModel<T>> colecao(Collection<T> itens, Class<?> controladorDosItens,
			Function<T, Long> idDe, WebMvcLinkBuilder linkDaColecao) {
		List<EntityModel<T>> modelos = new ArrayList<>();
		for (T item : itens) {
			EntityModel<T> modelo = EntityModel.of(item);
			modelo.add(linkTo(controladorDosItens).slash(idDe.apply(item)).withSelfRel());
			modelos.add(modelo);
		}
		CollectionModel<EntityModel<T>> colecao = CollectionModel.of(modelos);
		colecao.add(linkDaColecao.withSelfRel());
		return colecao;
	}
}
