package org.codeup.parknexus.repository.specification;

import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.Role;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> withFilters(Role role, Boolean active) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude soft-deleted users
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            // Filter by role if provided
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            // Filter by active status if provided
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

