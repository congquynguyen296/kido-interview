package com.kido.corporation.auth.dto.common;

import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageRequestParam {
    @Min(1)
    @Builder.Default
    int page = 1;

    @Min(1)
    @Builder.Default
    int size = 10;
    
    String sort;
    
    public Pageable toPageable() {
        return toPageable("createdAt", Sort.Direction.DESC);
    }

    public Pageable toPageable(String defaultSortField, Sort.Direction defaultDirection) {
        int pageNumber = Math.max(0, page - 1);
        if (sort != null && !sort.trim().isEmpty()) {
            String[] parts = sort.split(",");
            String property = parts[0];
            Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc") 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(pageNumber, size, Sort.by(direction, property));
        }
        return PageRequest.of(pageNumber, size, Sort.by(defaultDirection, defaultSortField));
    }
}
