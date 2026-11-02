package com.autobots.automanager.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entitades.Veiculo;

public interface RepositorioVeiculo extends JpaRepository<Veiculo, Long> {
	List<Veiculo> findByProprietarioId(Long usuarioId);
}
