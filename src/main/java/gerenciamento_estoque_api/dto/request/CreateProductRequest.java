package gerenciamento_estoque_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(@NotBlank String name) {
}
