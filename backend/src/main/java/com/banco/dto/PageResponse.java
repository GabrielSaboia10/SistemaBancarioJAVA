package com.banco.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Página em formato estável e enxuto (o JSON do PageImpl do Spring não é garantido entre versões). */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageResponse<T> de(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
