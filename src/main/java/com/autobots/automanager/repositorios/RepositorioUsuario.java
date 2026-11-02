package com.autobots.automanager.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
	@Query("select u from Usuario u where :perfil member of u.perfis")
	List<Usuario> buscarPorPerfil(@Param("perfil") PerfilUsuario perfil);

	List<Usuario> findByMercadoriasId(Long mercadoriaId);
}
