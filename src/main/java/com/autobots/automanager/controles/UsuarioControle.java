package com.autobots.automanager.controles;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.autobots.automanager.entitades.Credencial;
import com.autobots.automanager.entitades.Empresa;
import com.autobots.automanager.entitades.Mercadoria;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Veiculo;
import com.autobots.automanager.entitades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControle extends RecursoControle<Usuario> {

	@Autowired
	private RepositorioUsuario repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioVeiculo repositorioVeiculo;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private RepositorioMercadoria repositorioMercadoria;
	@Autowired
	private Resolvedor resolvedor;

	public UsuarioControle() {
		super(UsuarioControle.class);
	}

	@Override
	protected JpaRepository<Usuario, Long> repositorio() {
		return repositorio;
	}

	@Override
	protected String nome() {
		return "Usuário";
	}

	@Override
	protected Long idDe(Usuario usuario) {
		return usuario.getId();
	}

	@Override
	protected void definirId(Usuario usuario, Long id) {
		usuario.setId(id);
	}

	/** GET /usuarios?perfil=CLIENTE|FUNCIONARIO|FORNECEDOR filtra pelo tipo de usuário. */
	@Override
	protected List<Usuario> listarEntidades(Map<String, String> filtros) {
		String perfil = filtros.get("perfil");
		if (perfil == null || perfil.isBlank()) {
			return repositorio.findAll();
		}
		try {
			return repositorio.buscarPorPerfil(PerfilUsuario.valueOf(perfil.trim().toUpperCase(Locale.ROOT)));
		} catch (IllegalArgumentException excecao) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Perfil inválido. Use CLIENTE, FUNCIONARIO ou FORNECEDOR");
		}
	}

	@Override
	protected void antesDeCriar(Usuario usuario) {
		if (usuario.getPerfis() == null || usuario.getPerfis().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Informe ao menos um perfil: CLIENTE, FUNCIONARIO ou FORNECEDOR");
		}
		// mercadorias do fornecedor são associadas depois, por /usuarios/{id}/mercadorias
		usuario.setMercadorias(new HashSet<>());
		if (usuario.getCredenciais() != null) {
			for (Credencial credencial : usuario.getCredenciais()) {
				if (credencial.getCriacao() == null) {
					credencial.setCriacao(new Date());
				}
			}
		}
	}

	@Override
	protected void copiarCampos(Usuario destino, Usuario origem) {
		if (origem.getNome() != null) {
			destino.setNome(origem.getNome());
		}
		if (origem.getNomeSocial() != null) {
			destino.setNomeSocial(origem.getNomeSocial());
		}
		if (origem.getPerfis() != null && !origem.getPerfis().isEmpty()) {
			destino.getPerfis().clear();
			destino.getPerfis().addAll(origem.getPerfis());
		}
	}

	@Override
	protected void antesDeExcluir(Usuario usuario) {
		Long id = usuario.getId();
		if (repositorioVenda.existsByClienteId(id) || repositorioVenda.existsByFuncionarioId(id)
				|| repositorioVenda.existsByVeiculoProprietarioId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"O usuário possui vendas registradas e não pode ser excluído");
		}
		for (Empresa empresa : repositorioEmpresa.findByUsuariosId(id)) {
			empresa.getUsuarios().removeIf(item -> id.equals(item.getId()));
		}
	}

	@Override
	protected void adicionarLinks(EntityModel<Usuario> modelo, WebMvcLinkBuilder item) {
		modelo.add(item.slash("telefones").withRel("telefones"));
		modelo.add(item.slash("emails").withRel("emails"));
		modelo.add(item.slash("documentos").withRel("documentos"));
		modelo.add(item.slash("credenciais").withRel("credenciais"));
		modelo.add(item.slash("endereco").withRel("endereco"));
		modelo.add(item.slash("veiculos").withRel("veiculos"));
		modelo.add(item.slash("vendas").withRel("vendas"));
		modelo.add(item.slash("mercadorias").withRel("mercadorias"));
	}

	// ---- veículos e vendas do usuário -----------------------------------------

	@GetMapping("/{id}/veiculos")
	public ResponseEntity<CollectionModel<EntityModel<Veiculo>>> veiculos(@PathVariable("id") Long id) {
		buscar(id);
		return ResponseEntity.ok(Modelos.colecao(repositorioVeiculo.findByProprietarioId(id), VeiculoControle.class,
				Veiculo::getId, raiz().slash(id).slash("veiculos")));
	}

	/** GET /usuarios/{id}/vendas?como=cliente (padrão) ou como=funcionario. */
	@GetMapping("/{id}/vendas")
	public ResponseEntity<CollectionModel<EntityModel<Venda>>> vendas(@PathVariable("id") Long id,
			@RequestParam(name = "como", defaultValue = "cliente") String como) {
		buscar(id);
		List<Venda> vendas;
		if ("cliente".equalsIgnoreCase(como)) {
			vendas = repositorioVenda.findByClienteId(id);
		} else if ("funcionario".equalsIgnoreCase(como)) {
			vendas = repositorioVenda.findByFuncionarioId(id);
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O parâmetro 'como' aceita cliente ou funcionario");
		}
		return ResponseEntity.ok(
				Modelos.colecao(vendas, VendaControle.class, Venda::getId, raiz().slash(id).slash("vendas")));
	}

	// ---- mercadorias fornecidas (usuários com perfil FORNECEDOR) ---------------

	@GetMapping("/{id}/mercadorias")
	public ResponseEntity<CollectionModel<EntityModel<Mercadoria>>> mercadorias(@PathVariable("id") Long id) {
		Usuario usuario = buscar(id);
		return ResponseEntity.ok(Modelos.colecao(usuario.getMercadorias(), MercadoriaControle.class,
				Mercadoria::getId, raiz().slash(id).slash("mercadorias")));
	}

	@PutMapping("/{id}/mercadorias/{mercadoriaId}")
	@Transactional
	public ResponseEntity<EntityModel<Usuario>> associarMercadoria(@PathVariable("id") Long id,
			@PathVariable("mercadoriaId") Long mercadoriaId) {
		Usuario usuario = buscar(id);
		Mercadoria mercadoria = resolvedor.obter(repositorioMercadoria, mercadoriaId, "Mercadoria");
		if (!usuario.getPerfis().contains(PerfilUsuario.FORNECEDOR)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Apenas usuários com perfil FORNECEDOR fornecem mercadorias");
		}
		for (Usuario fornecedor : repositorio.findByMercadoriasId(mercadoriaId)) {
			if (!fornecedor.getId().equals(id)) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"A mercadoria já é fornecida pelo usuário " + fornecedor.getId());
			}
		}
		usuario.getMercadorias().add(mercadoria);
		return ResponseEntity.ok(modelo(repositorio.save(usuario)));
	}

	@DeleteMapping("/{id}/mercadorias/{mercadoriaId}")
	@Transactional
	public ResponseEntity<EntityModel<Usuario>> desassociarMercadoria(@PathVariable("id") Long id,
			@PathVariable("mercadoriaId") Long mercadoriaId) {
		Usuario usuario = buscar(id);
		boolean removida = usuario.getMercadorias().removeIf(item -> mercadoriaId.equals(item.getId()));
		if (!removida) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"A mercadoria " + mercadoriaId + " não é fornecida pelo usuário " + id);
		}
		return ResponseEntity.ok(modelo(repositorio.save(usuario)));
	}
}
