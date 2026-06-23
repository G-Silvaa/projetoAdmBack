package com.liv.infra.repository;

import javax.util.ddd.infra.jpa.RepositoryJpaImpl;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DDDRepository extends RepositoryJpaImpl {
	
	@Override
    @Modifying
    @Transactional
    public int add(String jpql, Object... params) {
        return super.add(jpql, params); 
    }

}
