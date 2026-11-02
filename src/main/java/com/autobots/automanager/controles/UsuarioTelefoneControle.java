package com.autobots.automanager.controles;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Telefone;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioTelefone;

@RestController
@RequestMapping("/usuarios/{paiId}/telefones")
public class UsuarioTelefoneControle extends FilhosControle<Usuario, Telefone> {

	@Autowired
	private RepositorioUsuario repositorioPai;
	@Autowired
	private RepositorioTelefone repositorioFilho;

	public UsuarioTelefoneControle() {
		super(UsuarioControle.class, "telefones");
	}

	@Override
	protected JpaRepository<Usuario, Long> repositorioPai() {
		return repositorioPai;
	}

	@Override
	protected JpaRepository<Telefone, Long> repositorioFilho() {
		return repositorioFilho;
	}

	@Override
	protected String nomePai() {
		return "Usuário";
	}

	@Override
	protected Set<Telefone> filhos(Usuario pai) {
		return pai.getTelefones();
	}

	@Override
	protected Long idDe(Telefone filho) {
		return filho.getId();
	}

	@Override
	protected void definirId(Telefone filho, Long id) {
		filho.setId(id);
	}

	@Override
	protected void copiarCampos(Telefone destino, Telefone origem) {
		if (origem.getDdd() != null) {
			destino.setDdd(origem.getDdd());
		}
		if (origem.getNumero() != null) {
			destino.setNumero(origem.getNumero());
		}
	}
}
