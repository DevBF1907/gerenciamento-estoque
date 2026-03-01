package gerenciamento_estoque_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateWarehouseRequest(@NotBlank String name) {
}
