package gerenciamento_estoque_api.dto.response;

import java.util.List;

public record WarehouseStockResponse(Long warehouseId, String warehouseName, List<WarehouseStockItemResponse> items) {
}
