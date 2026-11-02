package com.autobots.automanager.controles;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.autobots.automanager.entitades.Empresa;
import com.autobots.automanager.entitades.Mercadoria;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
@RequestMapping("/mercadorias")
public class MercadoriaControle extends RecursoControle<Mercadoria> {

	@Autowired
	private RepositorioMercadoria repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioVenda repositorioVenda;

	public MercadoriaControle() {
		super(MercadoriaControle.class);
	}

	@Override
	protected JpaRepository<Mercadoria, Long> repositorio() {
		return repositorio;
	}

	@Override
	protected String nome() {
		return "Mercadoria";
	}

	@Override
	protected Long idDe(Mercadoria mercadoria) {
		return mercadoria.getId();
	}

	@Override
	protected void definirId(Mercadoria mercadoria, Long id) {
		mercadoria.setId(id);
	}

	@Override
	protected void antesDeCriar(Mercadoria mercadoria) {
		if (mercadoria.getCadastro() == null) {
			mercadoria.setCadastro(new Date());
		}
	}

	/** PUT envia a mercadoria completa: quantidade e valor sempre são substituídos. */
	@Override
	protected void copiarCampos(Mercadoria destino, Mercadoria origem) {
		if (origem.getNome() != null) {
			destino.setNome(origem.getNome());
		}
		if (origem.getDescricao() != null) {
			destino.setDescricao(origem.getDescricao());
		}
		if (origem.getValidade() != null) {
			destino.setValidade(origem.getValidade());
		}
		if (origem.getFabricao() != null) {
			destino.setFabricao(origem.getFabricao());
		}
		destino.setQuantidade(origem.getQuantidade());
		destino.setValor(origem.getValor());
	}

	@Override
	protected void antesDeExcluir(Mercadoria mercadoria) {
		Long id = mercadoria.getId();
		if (repositorioVenda.existsByMercadoriasId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"A mercadoria consta em vendas registradas e não pode ser excluída");
		}
		for (Empresa empresa : repositorioEmpresa.findByMercadoriasId(id)) {
			empresa.getMercadorias().removeIf(item -> id.equals(item.getId()));
		}
		for (Usuario fornecedor : repositorioUsuario.findByMercadoriasId(id)) {
			fornecedor.getMercadorias().removeIf(item -> id.equals(item.getId()));
		}
	}
}
