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

	// Métodos com escopo de empresa (tenant): usados na gestão de usuários,
	// para que um admin só enxergue e altere usuários da própria empresa.
	List<Usuario> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

	long countByEmpresaId(Long empresaId);

	Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

	long countByEmpresaIdAndNivelAndAtivoTrue(Long empresaId, NivelUsuario nivel);
}
