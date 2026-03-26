package gerenciamento_estoque_api.service;

import gerenciamento_estoque_api.dto.request.StockRequest;
import gerenciamento_estoque_api.dto.response.*;
import gerenciamento_estoque_api.exception.InsufficientStockException;
import gerenciamento_estoque_api.model.Inventory;
import gerenciamento_estoque_api.model.Product;
import gerenciamento_estoque_api.model.Warehouse;
import gerenciamento_estoque_api.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductService productService;
    private final WarehouseService warehouseService;

    @Value("${stock.low-threshold.default:5}")
    private int defaultLowThreshold;

    @Transactional
    public void addStock(Long productId, StockRequest request) {
        Product product = productService.findById(productId);
        Warehouse warehouse = warehouseService.findById(request.warehouseId());

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(productId, request.warehouseId())
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity(0)
                        .build());

        inventory.setQuantity(inventory.getQuantity() + request.quantity());
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void removeStock(Long productId, StockRequest request) {
        Product product = productService.findById(productId);
        Warehouse warehouse = warehouseService.findById(request.warehouseId());

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(productId, request.warehouseId())
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity(0)
                        .build());

        if (inventory.getQuantity() < request.quantity()) {
            throw new InsufficientStockException(
                    String.format("Cannot remove %d units. Current stock for product '%s' in warehouse '%s' is %d.",
                            request.quantity(),
                            product.getName(),
                            warehouse.getName(),
                            inventory.getQuantity())
            );
        }

        inventory.setQuantity(inventory.getQuantity() - request.quantity());
        inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public ProductStockResponse getProductStock(Long productId) {
        Product product = productService.findById(productId);
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);

        int total = inventories.stream().mapToInt(Inventory::getQuantity).sum();

        List<InventoryByWarehouseResponse> byWarehouse = inventories.stream()
                .map(i -> new InventoryByWarehouseResponse(
                        i.getWarehouse().getId(),
                        i.getWarehouse().getName(),
                        i.getQuantity()))
                .toList();

        return new ProductStockResponse(product.getId(), product.getName(), total, byWarehouse);
    }

    @Transactional(readOnly = true)
    public WarehouseStockResponse getWarehouseStock(Long warehouseId) {
        Warehouse warehouse = warehouseService.findById(warehouseId);
        List<Inventory> inventories = inventoryRepository.findByWarehouseId(warehouseId);

        List<WarehouseStockItemResponse> items = inventories.stream()
                .map(i -> new WarehouseStockItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity()))
                .toList();

        return new WarehouseStockResponse(warehouse.getId(), warehouse.getName(), items);
    }

    @Transactional(readOnly = true)
    public List<LowStockResponse> getLowStockProducts(Integer threshold) {
        int effective = (threshold != null) ? threshold : defaultLowThreshold;

        return inventoryRepository.findProductsWithTotalQuantityBelow(effective).stream()
                .map(row -> {
                    Long productId = (Long) row[0];
                    int total = ((Number) row[1]).intValue();
                    Product product = productService.findById(productId);
                    return new LowStockResponse(product.getId(), product.getName(), total);
                }).toList();
    }
}