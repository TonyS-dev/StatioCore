package org.codeup.parknexus.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.codeup.parknexus.domain.ActivityLog;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActivityLogSpecification {
    
    public static Specification<ActivityLog> withFilters(
            UUID userId, 
            String action, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Eager load user relationship to avoid LazyInitializationException
            if (query != null) {
                root.fetch("user", JoinType.LEFT);
            }
            
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }
            
            if (action != null && !action.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }
            
            if (startDate != null) {
                OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }
            
            if (endDate != null) {
                OffsetDateTime endDateTime = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), endDateTime));
            }
            
            // Default ordering by createdAt descending
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
