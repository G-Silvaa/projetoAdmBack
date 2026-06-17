package com.liv.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liv.domain.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
