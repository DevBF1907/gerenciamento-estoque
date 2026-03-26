package gerenciamento_estoque_api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
        @NotNull Long warehouseId,
        @NotNull @Min(value = 1, message = "Quantity must be positive") Integer quantity
) {}
