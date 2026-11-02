package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.Map;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ponto de entrada da API: GET / lista os links de todos os recursos. */
@RestController
public class RaizControle {

	@GetMapping("/")
	public ResponseEntity<EntityModel<Map<String, String>>> raiz() {
		EntityModel<Map<String, String>> modelo = EntityModel.of(Map.of("aplicacao", "automanager"));
		modelo.add(linkTo(EmpresaControle.class).withRel("empresas"));
		modelo.add(linkTo(UsuarioControle.class).withRel("usuarios"));
		modelo.add(linkTo(VeiculoControle.class).withRel("veiculos"));
		modelo.add(linkTo(MercadoriaControle.class).withRel("mercadorias"));
		modelo.add(linkTo(ServicoControle.class).withRel("servicos"));
		modelo.add(linkTo(VendaControle.class).withRel("vendas"));
		return ResponseEntity.ok(modelo);
	}
}
