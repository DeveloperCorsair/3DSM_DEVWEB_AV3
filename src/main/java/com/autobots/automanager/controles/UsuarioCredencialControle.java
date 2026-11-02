package com.autobots.automanager.controles;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Credencial;
import com.autobots.automanager.entitades.CredencialCodigoBarra;
import com.autobots.automanager.entitades.CredencialUsuarioSenha;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioCredencial;

@RestController
@RequestMapping("/usuarios/{paiId}/credenciais")
public class UsuarioCredencialControle extends FilhosControle<Usuario, Credencial> {

	@Autowired
	private RepositorioUsuario repositorioPai;
	@Autowired
	private RepositorioCredencial repositorioFilho;

	public UsuarioCredencialControle() {
		super(UsuarioControle.class, "credenciais");
	}

	@Override
	protected JpaRepository<Usuario, Long> repositorioPai() {
		return repositorioPai;
	}

	@Override
	protected JpaRepository<Credencial, Long> repositorioFilho() {
		return repositorioFilho;
	}

	@Override
	protected String nomePai() {
		return "Usuário";
	}

	@Override
	protected Set<Credencial> filhos(Usuario pai) {
		return pai.getCredenciais();
	}

	@Override
	protected Long idDe(Credencial filho) {
		return filho.getId();
	}

	@Override
	protected void definirId(Credencial filho, Long id) {
		filho.setId(id);
	}

	@Override
	protected void antesDeCriar(Credencial nova) {
		if (nova.getCriacao() == null) {
			nova.setCriacao(new Date());
		}
	}

	@Override
	protected void copiarCampos(Credencial destino, Credencial origem) {
		if (origem.getUltimoAcesso() != null) {
			destino.setUltimoAcesso(origem.getUltimoAcesso());
		}
		destino.setInativo(origem.isInativo());
		if (destino instanceof CredencialUsuarioSenha && origem instanceof CredencialUsuarioSenha) {
			CredencialUsuarioSenha atual = (CredencialUsuarioSenha) destino;
			CredencialUsuarioSenha nova = (CredencialUsuarioSenha) origem;
			if (nova.getNomeUsuario() != null) {
				atual.setNomeUsuario(nova.getNomeUsuario());
			}
			if (nova.getSenha() != null) {
				atual.setSenha(nova.getSenha());
			}
		} else if (destino instanceof CredencialCodigoBarra && origem instanceof CredencialCodigoBarra) {
			((CredencialCodigoBarra) destino).setCodigo(((CredencialCodigoBarra) origem).getCodigo());
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"O tipo da credencial enviada é diferente do cadastrado");
		}
	}
}
