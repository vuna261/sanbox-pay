package com.cloudnut.payment.infrastructure.repository;

import com.cloudnut.payment.infrastructure.entity.PromoEntityDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PromoEntityRepo extends PagingAndSortingRepository<PromoEntityDB, Long>,
        JpaSpecificationExecutor<PromoEntityDB> {
    @Modifying
    @Query("DELETE FROM PromoEntityDB e WHERE e.id = ?1")
    void deleteById(Long id);

    Page<PromoEntityDB> findAllByNameContainingIgnoreCaseOrPromoCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String code, String des, Pageable pageable);

    List<PromoEntityDB> findAllByActiveIs(Boolean active);

    Optional<PromoEntityDB> findById(Long id);

    Optional<PromoEntityDB> findByPromoCode(String code);

    List<PromoEntityDB> findAllByExpiredDateAfter(Date date);

    List<PromoEntityDB> findAllByExpiredDateBeforeAndActiveIs(Date date, Boolean active);
}
