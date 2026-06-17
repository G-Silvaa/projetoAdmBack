package com.liv.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liv.domain.Assinatura;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {

	Optional<Assinatura> findByEmpresaId(Long empresaId);
}
