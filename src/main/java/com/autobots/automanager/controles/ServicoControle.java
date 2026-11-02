package com.autobots.automanager.controles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.autobots.automanager.entitades.Empresa;
import com.autobots.automanager.entitades.Servico;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
@RequestMapping("/servicos")
public class ServicoControle extends RecursoControle<Servico> {

	@Autowired
	private RepositorioServico repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioVenda repositorioVenda;

	public ServicoControle() {
		super(ServicoControle.class);
	}

	@Override
	protected JpaRepository<Servico, Long> repositorio() {
		return repositorio;
	}

	@Override
	protected String nome() {
		return "Serviço";
	}

	@Override
	protected Long idDe(Servico servico) {
		return servico.getId();
	}

	@Override
	protected void definirId(Servico servico, Long id) {
		servico.setId(id);
	}

	/** PUT envia o serviço completo: o valor sempre é substituído. */
	@Override
	protected void copiarCampos(Servico destino, Servico origem) {
		if (origem.getNome() != null) {
			destino.setNome(origem.getNome());
		}
		if (origem.getDescricao() != null) {
			destino.setDescricao(origem.getDescricao());
		}
		destino.setValor(origem.getValor());
	}

	@Override
	protected void antesDeExcluir(Servico servico) {
		Long id = servico.getId();
		if (repositorioVenda.existsByServicosId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"O serviço consta em vendas registradas e não pode ser excluído");
		}
		for (Empresa empresa : repositorioEmpresa.findByServicosId(id)) {
			empresa.getServicos().removeIf(item -> id.equals(item.getId()));
		}
	}
}
