package gerenciamento_estoque_api.dto.response;

public record WarehouseStockItemResponse(long productId,String productName, int quantity) {
}
