package com.autobots.automanager.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entitades.Venda;

public interface RepositorioVenda extends JpaRepository<Venda, Long> {
	List<Venda> findByClienteId(Long usuarioId);

	List<Venda> findByFuncionarioId(Long usuarioId);

	List<Venda> findByVeiculoId(Long veiculoId);

	boolean existsByClienteId(Long usuarioId);

	boolean existsByFuncionarioId(Long usuarioId);

	boolean existsByVeiculoId(Long veiculoId);

	boolean existsByVeiculoProprietarioId(Long usuarioId);

	boolean existsByMercadoriasId(Long mercadoriaId);

	boolean existsByServicosId(Long servicoId);
}
