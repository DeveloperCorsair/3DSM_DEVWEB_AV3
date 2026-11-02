package com.autobots.automanager.controles;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Email;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioEmail;

@RestController
@RequestMapping("/usuarios/{paiId}/emails")
public class UsuarioEmailControle extends FilhosControle<Usuario, Email> {

	@Autowired
	private RepositorioUsuario repositorioPai;
	@Autowired
	private RepositorioEmail repositorioFilho;

	public UsuarioEmailControle() {
		super(UsuarioControle.class, "emails");
	}

	@Override
	protected JpaRepository<Usuario, Long> repositorioPai() {
		return repositorioPai;
	}

	@Override
	protected JpaRepository<Email, Long> repositorioFilho() {
		return repositorioFilho;
	}

	@Override
	protected String nomePai() {
		return "Usuário";
	}

	@Override
	protected Set<Email> filhos(Usuario pai) {
		return pai.getEmails();
	}

	@Override
	protected Long idDe(Email filho) {
		return filho.getId();
	}

	@Override
	protected void definirId(Email filho, Long id) {
		filho.setId(id);
	}

	@Override
	protected void copiarCampos(Email destino, Email origem) {
		if (origem.getEndereco() != null) {
			destino.setEndereco(origem.getEndereco());
		}
	}
}
