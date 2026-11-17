package com.unigig.backend.repository;

import com.unigig.backend.entity.FreelancerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreelancerRepository extends JpaRepository<FreelancerProfile, Long> {


}