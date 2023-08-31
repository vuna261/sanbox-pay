package com.cloudnut.payment.infrastructure.repository;

import com.cloudnut.payment.infrastructure.entity.PriceEntityDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface PriceEntityRepo extends PagingAndSortingRepository<PriceEntityDB, Long>,
        JpaSpecificationExecutor<PriceEntityDB> {

    Optional<PriceEntityDB> findById(Long id);
    Optional<PriceEntityDB> findByName(String name);

    @Modifying
    @Query("DELETE FROM PriceEntityDB e WHERE e.id = ?1")
    void deleteById(Long id);

    List<PriceEntityDB> findAll();

    Page<PriceEntityDB> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
