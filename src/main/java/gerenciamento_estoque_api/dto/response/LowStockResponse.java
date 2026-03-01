package gerenciamento_estoque_api.dto.response;

public record LowStockResponse(long ProductId, String ProductName, int totalQuantity) {
}
