package gerenciamento_estoque_api.dto.response;

import java.util.List;

public record ProductStockResponse(Long productId, String productName, int totalQuantity, List<InventoryByWarehouseResponse> inventoryByWarehouse) {
}
