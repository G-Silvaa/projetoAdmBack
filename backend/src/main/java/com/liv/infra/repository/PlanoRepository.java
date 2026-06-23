package com.liv.infra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liv.domain.Plano;

public interface PlanoRepository extends JpaRepository<Plano, Long> {

	Optional<Plano> findByCodigo(String codigo);

	List<Plano> findAllByAtivoTrueOrderByPrecoAsc();
}
