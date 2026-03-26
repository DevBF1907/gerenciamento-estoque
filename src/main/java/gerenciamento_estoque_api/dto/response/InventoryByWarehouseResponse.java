package gerenciamento_estoque_api.dto.response;

public record InventoryByWarehouseResponse(Long warehouseId, String warehouseName, int quantity) {
}
