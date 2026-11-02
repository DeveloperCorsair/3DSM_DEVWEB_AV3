package com.autobots.automanager.controles;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.Set;

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
import com.autobots.automanager.entitades.Venda;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
@RequestMapping("/empresas")
public class EmpresaControle extends RecursoControle<Empresa> {

	@Autowired
	private RepositorioEmpresa repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioMercadoria repositorioMercadoria;
	@Autowired
	private RepositorioServico repositorioServico;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private Resolvedor resolvedor;

	public EmpresaControle() {
		super(EmpresaControle.class);
	}

	@Override
	protected JpaRepository<Empresa, Long> repositorio() {
		return repositorio;
	}

	@Override
	protected String nome() {
		return "Empresa";
	}

	@Override
	protected Long idDe(Empresa empresa) {
		return empresa.getId();
	}

	@Override
	protected void definirId(Empresa empresa, Long id) {
		empresa.setId(id);
	}

	@Override
	protected void antesDeCriar(Empresa empresa) {
		if (empresa.getCadastro() == null) {
			empresa.setCadastro(new Date());
		}
		// usuários, mercadorias, serviços e vendas são associados depois, pelos
		// endpoints /empresas/{id}/usuarios, /mercadorias, /servicos e /vendas
		empresa.setUsuarios(new HashSet<>());
		empresa.setMercadorias(new HashSet<>());
		empresa.setServicos(new HashSet<>());
		empresa.setVendas(new HashSet<>());
	}

	@Override
	protected void copiarCampos(Empresa destino, Empresa origem) {
		if (origem.getRazaoSocial() != null) {
			destino.setRazaoSocial(origem.getRazaoSocial());
		}
		if (origem.getNomeFantasia() != null) {
			destino.setNomeFantasia(origem.getNomeFantasia());
		}
	}

	@Override
	protected void adicionarLinks(EntityModel<Empresa> modelo, WebMvcLinkBuilder item) {
		modelo.add(item.slash("telefones").withRel("telefones"));
		modelo.add(item.slash("endereco").withRel("endereco"));
		modelo.add(item.slash("usuarios").withRel("usuarios"));
		modelo.add(item.slash("mercadorias").withRel("mercadorias"));
		modelo.add(item.slash("servicos").withRel("servicos"));
		modelo.add(item.slash("vendas").withRel("vendas"));
	}

	// ---- usuários da empresa --------------------------------------------------

	@GetMapping("/{id}/usuarios")
	public ResponseEntity<CollectionModel<EntityModel<Usuario>>> usuarios(@PathVariable("id") Long id) {
		Empresa empresa = buscar(id);
		return ResponseEntity.ok(Modelos.colecao(empresa.getUsuarios(), UsuarioControle.class, Usuario::getId,
				raiz().slash(id).slash("usuarios")));
	}

	@PutMapping("/{id}/usuarios/{usuarioId}")
	@Transactional
	public ResponseEntity<EntityModel<Empresa>> associarUsuario(@PathVariable("id") Long id,
			@PathVariable("usuarioId") Long usuarioId) {
		return associar(id, usuarioId, "Usuário", repositorioUsuario, repositorio.findByUsuariosId(usuarioId),
				Empresa::getUsuarios);
	}

	@DeleteMapping("/{id}/usuarios/{usuarioId}")
	@Transactional
	public ResponseEntity<EntityModel<Empresa>> desassociarUsuario(@PathVariable("id") Long id,
			@PathVariable("usuarioId") Long usuarioId) {
		return desassociar(id, usuarioId, Empresa::getUsuarios, Usuario::getId);
	}

	// ---- mercadorias da empresa -----------------------------------------------

	@GetMapping("/{id}/mercadorias")
	public ResponseEntity<CollectionModel<EntityModel<Mercadoria>>> mercadorias(@PathVariable("id") Long id) {
		Empresa empresa = buscar(id);
		return ResponseEntity.ok(Modelos.colecao(empresa.getMercadorias(), MercadoriaControle.class,
				Mercadoria::getId, raiz().slash(id).slash("mercadorias")));
	}

	@PutMapping("/{id}/mercadorias/{mercadoriaId}")
	@Transactional
	public ResponseEntity<EntityModel<Empresa>> associarMercadoria(@PathVariable("id") Long id,
			@PathVariable("mercadoriaId") Long mercadoriaId) {
		return associar(id, mercadoriaId, "Mercadoria", repositorioMercadoria,
				repositorio.findByMercadoriasId(mercadoriaId), Empresa::getMercadorias);
	}

	@DeleteMapping("/{id}/mercadorias/{mercadoriaId}")
	@Transactional
	public ResponseEntity<EntityModel<Empresa>> desassociarMercadoria(@PathVariable("id") Long id,
			@PathVariable("mercadoriaId") Long mercadoriaId) {
		return desassociar(id, mercadoriaId, Empresa::getMercadorias, Mercadoria::getId);
	}

	// ---- serviços da empresa --------------------------------------------------

	@GetMapping("/{id}/servicos")
	public ResponseEntity<CollectionModel<EntityModel<Servico>>> servicos(@PathVariable("id") Long id) {
		Empresa empresa = buscar(id);
		return ResponseEntity.ok(Modelos.colecao(empresa.getServicos(), ServicoControle.class, Servico::getId,
				raiz().slash(id).slash("servicos")));
	}

	@PutMapping("/{id}/servicos/{servicoId}")
	@Transactional
	public ResponseEntity<EntityModel<Empresa>> associarServico(@PathVariable("id") Long id,
			@PathVariable("servicoId") Long servicoId) {
		return associar(id, servicoId, "Serviço", repositorioServico, repositorio.findByServicosId(servicoId),
				Empresa::getServicos);
	}

	@DeleteMapping("/{id}/servicos/{servicoId}")
	@Transactional
	public ResponseEntity<EntityModel<Empresa>> desassociarServico(@PathVariable("id") Long id,
			@PathVariable("servicoId") Long servicoId) {
		return desassociar(id, servicoId, Empresa::getServicos, Servico::getId);
	}

	// ---- vendas da empresa ----------------------------------------------------

	@GetMapping("/{id}/vendas")
	public ResponseEntity<CollectionModel<EntityModel<Venda>>> vendas(@PathVariable("id") Long id) {
		Empresa empresa = buscar(id);
		return ResponseEntity.ok(Modelos.colecao(empresa.getVendas(), VendaControle.class, Venda::getId,
				raiz().slash(id).slash("vendas")));
	}

	@PutMapping("/{id}/vendas/{vendaId}")
	@Transactional
	public ResponseEntity<EntityModel<Empresa>> associarVenda(@PathVariable("id") Long id,
			@PathVariable("vendaId") Long vendaId) {
		return associar(id, vendaId, "Venda", repositorioVenda, repositorio.findByVendasId(vendaId),
				Empresa::getVendas);
	}

	@DeleteMapping("/{id}/vendas/{vendaId}")
	@Transactional
	public ResponseEntity<EntityModel<Empresa>> desassociarVenda(@PathVariable("id") Long id,
			@PathVariable("vendaId") Long vendaId) {
		return desassociar(id, vendaId, Empresa::getVendas, Venda::getId);
	}

	// ---- apoio ----------------------------------------------------------------

	private <I> ResponseEntity<EntityModel<Empresa>> associar(Long empresaId, Long itemId, String nomeDoItem,
			JpaRepository<I, Long> repositorioDoItem, List<Empresa> empresasDoItem,
			Function<Empresa, Set<I>> colecao) {
		Empresa empresa = buscar(empresaId);
		I item = resolvedor.obter(repositorioDoItem, itemId, nomeDoItem);
		for (Empresa dona : empresasDoItem) {
			if (!dona.getId().equals(empresaId)) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						nomeDoItem + " " + itemId + " já está associado(a) à empresa " + dona.getId());
			}
		}
		colecao.apply(empresa).add(item);
		return ResponseEntity.ok(modelo(repositorio.save(empresa)));
	}

	private <I> ResponseEntity<EntityModel<Empresa>> desassociar(Long empresaId, Long itemId,
			Function<Empresa, Set<I>> colecao, Function<I, Long> idDe) {
		Empresa empresa = buscar(empresaId);
		boolean removido = colecao.apply(empresa).removeIf(item -> itemId.equals(idDe.apply(item)));
		if (!removido) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"O item " + itemId + " não está associado à empresa " + empresaId);
		}
		return ResponseEntity.ok(modelo(repositorio.save(empresa)));
	}
}
