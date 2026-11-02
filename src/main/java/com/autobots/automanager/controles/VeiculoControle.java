package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Veiculo;
import com.autobots.automanager.entitades.Venda;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
@RequestMapping("/veiculos")
public class VeiculoControle extends RecursoControle<Veiculo> {

	@Autowired
	private RepositorioVeiculo repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private Resolvedor resolvedor;

	public VeiculoControle() {
		super(VeiculoControle.class);
	}

	@Override
	protected JpaRepository<Veiculo, Long> repositorio() {
		return repositorio;
	}

	@Override
	protected String nome() {
		return "Veículo";
	}

	@Override
	protected Long idDe(Veiculo veiculo) {
		return veiculo.getId();
	}

	@Override
	protected void definirId(Veiculo veiculo, Long id) {
		veiculo.setId(id);
	}

	private Usuario resolverProprietario(Usuario referencia) {
		Long id = referencia == null ? null : referencia.getId();
		return resolvedor.referenciar(repositorioUsuario, id, "proprietario");
	}

	@Override
	protected void antesDeCriar(Veiculo veiculo) {
		veiculo.setProprietario(resolverProprietario(veiculo.getProprietario()));
	}

	@Override
	protected void copiarCampos(Veiculo destino, Veiculo origem) {
		if (origem.getTipo() != null) {
			destino.setTipo(origem.getTipo());
		}
		if (origem.getModelo() != null) {
			destino.setModelo(origem.getModelo());
		}
		if (origem.getPlaca() != null) {
			destino.setPlaca(origem.getPlaca());
		}
		if (origem.getProprietario() != null) {
			destino.setProprietario(resolverProprietario(origem.getProprietario()));
		}
	}

	@Override
	protected void antesDeExcluir(Veiculo veiculo) {
		if (repositorioVenda.existsByVeiculoId(veiculo.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"O veículo possui vendas registradas e não pode ser excluído");
		}
	}

	@Override
	protected void adicionarLinks(EntityModel<Veiculo> modelo, WebMvcLinkBuilder item) {
		Veiculo veiculo = modelo.getContent();
		if (veiculo != null && veiculo.getProprietario() != null) {
			modelo.add(linkTo(UsuarioControle.class).slash(veiculo.getProprietario().getId())
					.withRel("proprietario"));
		}
		modelo.add(item.slash("vendas").withRel("vendas"));
	}

	@GetMapping("/{id}/vendas")
	public ResponseEntity<CollectionModel<EntityModel<Venda>>> vendas(@PathVariable("id") Long id) {
		buscar(id);
		return ResponseEntity.ok(Modelos.colecao(repositorioVenda.findByVeiculoId(id), VendaControle.class,
				Venda::getId, raiz().slash(id).slash("vendas")));
	}
}
