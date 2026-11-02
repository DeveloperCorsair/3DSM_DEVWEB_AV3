package com.autobots.automanager.controles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entitades.Empresa;
import com.autobots.automanager.entitades.Endereco;
import com.autobots.automanager.repositorios.RepositorioEmpresa;

@RestController
@RequestMapping("/empresas/{paiId}/endereco")
public class EmpresaEnderecoControle extends EnderecoDoPaiControle<Empresa> {

	@Autowired
	private RepositorioEmpresa repositorioPai;

	public EmpresaEnderecoControle() {
		super(EmpresaControle.class);
	}

	@Override
	protected JpaRepository<Empresa, Long> repositorioPai() {
		return repositorioPai;
	}

	@Override
	protected String nomePai() {
		return "Empresa";
	}

	@Override
	protected Endereco endereco(Empresa pai) {
		return pai.getEndereco();
	}

	@Override
	protected void definirEndereco(Empresa pai, Endereco endereco) {
		pai.setEndereco(endereco);
	}
}
