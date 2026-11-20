package com.unigig.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unigig.backend.entity.Gig;
import com.unigig.backend.entity.GigStatus;

@Repository
public interface GigRepository extends JpaRepository<Gig, Long> {

    List<Gig> findByStatusOrderByCreatedAtDesc(GigStatus status);

    List<Gig> findByStatusAndCategoryIdOrderByCreatedAtDesc(GigStatus status, Long categoryId);

    List<Gig> findByClientIdOrderByCreatedAtDesc(Long clientId);

    @Query("""
            SELECT g
            FROM Gig g
            WHERE g.status = :status
              AND (:categoryId IS NULL OR g.category.id = :categoryId)
              AND (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(g.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            ORDER BY g.createdAt DESC
            """)
    Page<Gig> searchByStatusAndFilters(
            @Param("status") GigStatus status,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}