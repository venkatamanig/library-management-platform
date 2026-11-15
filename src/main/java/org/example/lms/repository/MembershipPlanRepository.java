package org.example.lms.repository;

import org.example.lms.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipPlanRepository
        extends JpaRepository<MembershipPlan, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<MembershipPlan> findByActiveTrue();
}
