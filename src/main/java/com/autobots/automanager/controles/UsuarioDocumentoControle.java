package com.autobots.automanager.controles;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Documento;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioDocumento;

@RestController
@RequestMapping("/usuarios/{paiId}/documentos")
public class UsuarioDocumentoControle extends FilhosControle<Usuario, Documento> {

	@Autowired
	private RepositorioUsuario repositorioPai;
	@Autowired
	private RepositorioDocumento repositorioFilho;

	public UsuarioDocumentoControle() {
		super(UsuarioControle.class, "documentos");
	}

	@Override
	protected JpaRepository<Usuario, Long> repositorioPai() {
		return repositorioPai;
	}

	@Override
	protected JpaRepository<Documento, Long> repositorioFilho() {
		return repositorioFilho;
	}

	@Override
	protected String nomePai() {
		return "Usuário";
	}

	@Override
	protected Set<Documento> filhos(Usuario pai) {
		return pai.getDocumentos();
	}

	@Override
	protected Long idDe(Documento filho) {
		return filho.getId();
	}

	@Override
	protected void definirId(Documento filho, Long id) {
		filho.setId(id);
	}

	@Override
	protected void copiarCampos(Documento destino, Documento origem) {
		if (origem.getTipo() != null) {
			destino.setTipo(origem.getTipo());
		}
		if (origem.getDataEmissao() != null) {
			destino.setDataEmissao(origem.getDataEmissao());
		}
		if (origem.getNumero() != null) {
			destino.setNumero(origem.getNumero());
		}
	}
}
