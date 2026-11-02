package com.autobots.automanager.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entitades.Empresa;

public interface RepositorioEmpresa extends JpaRepository<Empresa, Long> {
	List<Empresa> findByUsuariosId(Long usuarioId);

	List<Empresa> findByMercadoriasId(Long mercadoriaId);

	List<Empresa> findByServicosId(Long servicoId);

	List<Empresa> findByVendasId(Long vendaId);
}
