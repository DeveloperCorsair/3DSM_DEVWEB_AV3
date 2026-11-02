package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.autobots.automanager.entitades.Empresa;
import com.autobots.automanager.entitades.Mercadoria;
import com.autobots.automanager.entitades.Servico;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Veiculo;
import com.autobots.automanager.entitades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.repositorios.RepositorioVenda;

/**
 * Exemplo de corpo para POST /vendas:
 * { "identificacao": "V-001", "cliente": {"id": 3}, "funcionario": {"id": 1},
 *   "veiculo": {"id": 1}, "mercadorias": [{"id": 1}], "servicos": [{"id": 1}] }
 */
@RestController
@RequestMapping("/vendas")
public class VendaControle extends RecursoControle<Venda> {

	@Autowired
	private RepositorioVenda repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioVeiculo repositorioVeiculo;
	@Autowired
	private RepositorioMercadoria repositorioMercadoria;
	@Autowired
	private RepositorioServico repositorioServico;
	@Autowired
	private Resolvedor resolvedor;

	public VendaControle() {
		super(VendaControle.class);
	}

	@Override
	protected JpaRepository<Venda, Long> repositorio() {
		return repositorio;
	}

	@Override
	protected String nome() {
		return "Venda";
	}

	@Override
	protected Long idDe(Venda venda) {
		return venda.getId();
	}

	@Override
	protected void definirId(Venda venda, Long id) {
		venda.setId(id);
	}

	// resolução das referências enviadas no JSON 
	private Usuario resolverUsuario(Usuario referencia, String campo) {
		Long id = referencia == null ? null : referencia.getId();
		return resolvedor.referenciar(repositorioUsuario, id, campo);
	}

	private Set<Mercadoria> resolverMercadorias(Set<Mercadoria> referencias) {
		Set<Mercadoria> resolvidas = new HashSet<>();
		if (referencias != null) {
			for (Mercadoria referencia : referencias) {
				resolvidas.add(resolvedor.referenciar(repositorioMercadoria, referencia.getId(), "mercadorias"));
			}
		}
		return resolvidas;
	}

	private Set<Servico> resolverServicos(Set<Servico> referencias) {
		Set<Servico> resolvidos = new HashSet<>();
		if (referencias != null) {
			for (Servico referencia : referencias) {
				resolvidos.add(resolvedor.referenciar(repositorioServico, referencia.getId(), "servicos"));
			}
		}
		return resolvidos;
	}

	private Veiculo resolverVeiculo(Veiculo referencia) {
		return resolvedor.referenciar(repositorioVeiculo, referencia.getId(), "veiculo");
	}

	/** Regras: cliente tem perfil CLIENTE, funcionário tem perfil FUNCIONARIO e o veículo é do cliente. */
	private void validar(Venda venda) {
		if (!venda.getCliente().getPerfis().contains(PerfilUsuario.CLIENTE)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O usuário informado como cliente não tem o perfil CLIENTE");
		}
		if (!venda.getFuncionario().getPerfis().contains(PerfilUsuario.FUNCIONARIO)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"O usuário informado como funcionário não tem o perfil FUNCIONARIO");
		}
		Veiculo veiculo = venda.getVeiculo();
		if (veiculo != null && (veiculo.getProprietario() == null
				|| !veiculo.getProprietario().getId().equals(venda.getCliente().getId()))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O veículo informado não pertence ao cliente da venda");
		}
	}

	@Override
	protected void antesDeCriar(Venda venda) {
		if (venda.getIdentificacao() == null || venda.getIdentificacao().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a identificação da venda");
		}
		if (venda.getCadastro() == null) {
			venda.setCadastro(new Date());
		}
		venda.setCliente(resolverUsuario(venda.getCliente(), "cliente"));
		venda.setFuncionario(resolverUsuario(venda.getFuncionario(), "funcionario"));
		if (venda.getVeiculo() != null) {
			venda.setVeiculo(resolverVeiculo(venda.getVeiculo()));
		}
		venda.setMercadorias(resolverMercadorias(venda.getMercadorias()));
		venda.setServicos(resolverServicos(venda.getServicos()));
		validar(venda);
	}

	/** No PUT, campos ausentes são mantidos; listas de mercadorias/serviços, quando enviadas, substituem as atuais. */
	@Override
	protected void copiarCampos(Venda destino, Venda origem) {
		if (origem.getIdentificacao() != null) {
			destino.setIdentificacao(origem.getIdentificacao());
		}
		if (origem.getCadastro() != null) {
			destino.setCadastro(origem.getCadastro());
		}
		if (origem.getCliente() != null) {
			destino.setCliente(resolverUsuario(origem.getCliente(), "cliente"));
		}
		if (origem.getFuncionario() != null) {
			destino.setFuncionario(resolverUsuario(origem.getFuncionario(), "funcionario"));
		}
		if (origem.getVeiculo() != null) {
			destino.setVeiculo(resolverVeiculo(origem.getVeiculo()));
		}
		if (origem.getMercadorias() != null && !origem.getMercadorias().isEmpty()) {
			Set<Mercadoria> novas = resolverMercadorias(origem.getMercadorias());
			destino.getMercadorias().clear();
			destino.getMercadorias().addAll(novas);
		}
		if (origem.getServicos() != null && !origem.getServicos().isEmpty()) {
			Set<Servico> novos = resolverServicos(origem.getServicos());
			destino.getServicos().clear();
			destino.getServicos().addAll(novos);
		}
		validar(destino);
	}

	@Override
	protected void antesDeExcluir(Venda venda) {
		Long id = venda.getId();
		for (Empresa empresa : repositorioEmpresa.findByVendasId(id)) {
			empresa.getVendas().removeIf(item -> id.equals(item.getId()));
		}
	}

	@Override
	protected void adicionarLinks(EntityModel<Venda> modelo, WebMvcLinkBuilder item) {
		Venda venda = modelo.getContent();
		if (venda == null) {
			return;
		}
		if (venda.getCliente() != null) {
			modelo.add(linkTo(UsuarioControle.class).slash(venda.getCliente().getId()).withRel("cliente"));
		}
		if (venda.getFuncionario() != null) {
			modelo.add(linkTo(UsuarioControle.class).slash(venda.getFuncionario().getId()).withRel("funcionario"));
		}
		if (venda.getVeiculo() != null) {
			modelo.add(linkTo(VeiculoControle.class).slash(venda.getVeiculo().getId()).withRel("veiculo"));
		}
		modelo.add(item.slash("mercadorias").withRel("mercadorias"));
		modelo.add(item.slash("servicos").withRel("servicos"));
	}

	// itens da venda 

	@GetMapping("/{id}/mercadorias")
	public ResponseEntity<CollectionModel<EntityModel<Mercadoria>>> mercadorias(@PathVariable("id") Long id) {
		Venda venda = buscar(id);
		return ResponseEntity.ok(Modelos.colecao(venda.getMercadorias(), MercadoriaControle.class,
				Mercadoria::getId, raiz().slash(id).slash("mercadorias")));
	}

	@PutMapping("/{id}/mercadorias/{mercadoriaId}")
	@Transactional
	public ResponseEntity<EntityModel<Venda>> adicionarMercadoria(@PathVariable("id") Long id,
			@PathVariable("mercadoriaId") Long mercadoriaId) {
		Venda venda = buscar(id);
		venda.getMercadorias().add(resolvedor.obter(repositorioMercadoria, mercadoriaId, "Mercadoria"));
		return ResponseEntity.ok(modelo(repositorio.save(venda)));
	}

	@DeleteMapping("/{id}/mercadorias/{mercadoriaId}")
	@Transactional
	public ResponseEntity<EntityModel<Venda>> removerMercadoria(@PathVariable("id") Long id,
			@PathVariable("mercadoriaId") Long mercadoriaId) {
		return removerItem(id, mercadoriaId, Venda::getMercadorias, Mercadoria::getId);
	}

	@GetMapping("/{id}/servicos")
	public ResponseEntity<CollectionModel<EntityModel<Servico>>> servicos(@PathVariable("id") Long id) {
		Venda venda = buscar(id);
		return ResponseEntity.ok(Modelos.colecao(venda.getServicos(), ServicoControle.class, Servico::getId,
				raiz().slash(id).slash("servicos")));
	}

	@PutMapping("/{id}/servicos/{servicoId}")
	@Transactional
	public ResponseEntity<EntityModel<Venda>> adicionarServico(@PathVariable("id") Long id,
			@PathVariable("servicoId") Long servicoId) {
		Venda venda = buscar(id);
		venda.getServicos().add(resolvedor.obter(repositorioServico, servicoId, "Serviço"));
		return ResponseEntity.ok(modelo(repositorio.save(venda)));
	}

	@DeleteMapping("/{id}/servicos/{servicoId}")
	@Transactional
	public ResponseEntity<EntityModel<Venda>> removerServico(@PathVariable("id") Long id,
			@PathVariable("servicoId") Long servicoId) {
		return removerItem(id, servicoId, Venda::getServicos, Servico::getId);
	}

	private <I> ResponseEntity<EntityModel<Venda>> removerItem(Long vendaId, Long itemId,
			Function<Venda, Set<I>> colecao, Function<I, Long> idDe) {
		Venda venda = buscar(vendaId);
		boolean removido = colecao.apply(venda).removeIf(item -> itemId.equals(idDe.apply(item)));
		if (!removido) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"O item " + itemId + " não consta na venda " + vendaId);
		}
		return ResponseEntity.ok(modelo(repositorio.save(venda)));
	}
}
