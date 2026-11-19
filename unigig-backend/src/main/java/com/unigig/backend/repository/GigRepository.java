package com.unigig.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unigig.backend.entity.Gig;
import com.unigig.backend.entity.GigStatus;
@Repository 
public interface  GigRepository extends JpaRepository<Gig, Long> {

    List<Gig> findByStatusOrderByCreatedAtDesc(GigStatus status);

    List<Gig> findByStatusAndCategoryIdOrderByCreatedAtDesc(GigStatus status, Long categoryId); 

    List<Gig> findByClientIdOrderByCreatedAtDesc(Long clientId);
    
}
