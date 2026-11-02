package com.autobots.automanager.controles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entitades.Endereco;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
@RequestMapping("/usuarios/{paiId}/endereco")
public class UsuarioEnderecoControle extends EnderecoDoPaiControle<Usuario> {

	@Autowired
	private RepositorioUsuario repositorioPai;

	public UsuarioEnderecoControle() {
		super(UsuarioControle.class);
	}

	@Override
	protected JpaRepository<Usuario, Long> repositorioPai() {
		return repositorioPai;
	}

	@Override
	protected String nomePai() {
		return "Usuário";
	}

	@Override
	protected Endereco endereco(Usuario pai) {
		return pai.getEndereco();
	}

	@Override
	protected void definirEndereco(Usuario pai, Endereco endereco) {
		pai.setEndereco(endereco);
	}
}
