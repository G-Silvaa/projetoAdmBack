package com.liv.infra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liv.domain.NivelUsuario;
import com.liv.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByEmailIgnoreCase(String email);

	Optional<Usuario> findByIdAndAtivoTrue(Long id);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	long countByNivelAndAtivoTrue(NivelUsuario nivel);

	List<Usuario> findAllByOrderByNomeAsc();
}
