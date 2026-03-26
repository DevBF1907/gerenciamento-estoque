package gerenciamento_estoque_api.controller;


import gerenciamento_estoque_api.dto.request.StockRequest;
import gerenciamento_estoque_api.dto.response.LowStockResponse;
import gerenciamento_estoque_api.dto.response.ProductStockResponse;
import gerenciamento_estoque_api.dto.response.WarehouseStockResponse;
import gerenciamento_estoque_api.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory", description = "Controle de estoque")
@RestController
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Adicionar estoque")
    @ApiResponse(responseCode = "204", description = "Estoque adicionado com sucesso")
    @ApiResponse(responseCode = "404", description = "Produto ou armazém não encontrado")
    @PostMapping("/products/{productId}/stock/add")
    public ResponseEntity<Void> addStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockRequest request) {
        inventoryService.addStock(productId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remover estoque")
    @ApiResponse(responseCode = "204", description = "Estoque removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Produto ou armazém não encontrado")
    @ApiResponse(responseCode = "422", description = "Estoque insuficiente")
    @PostMapping("/products/{productId}/stock/remove")
    public ResponseEntity<Void> removeStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockRequest request) {
        inventoryService.removeStock(productId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Estoque total de um produto por armazém")
    @ApiResponse(responseCode = "200", description = "Estoque do produto")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    @GetMapping("/products/{productId}/stock")
    public ProductStockResponse getProductStock(@PathVariable Long productId) {
        return inventoryService.getProductStock(productId);
    }

    @Operation(summary = "Todos os produtos de um armazém")
    @ApiResponse(responseCode = "200", description = "Estoque do armazém")
    @ApiResponse(responseCode = "404", description = "Armazém não encontrado")
    @GetMapping("/warehouses/{warehouseId}/stock")
    public WarehouseStockResponse getWarehouseStock(@PathVariable Long warehouseId) {
        return inventoryService.getWarehouseStock(warehouseId);
    }

    @Operation(
            summary = "Produtos com estoque baixo",
            description = "Retorna produtos cujo total de unidades está abaixo do threshold. Padrão: 5 unidades."
    )
    @ApiResponse(responseCode = "200", description = "Lista de produtos com estoque baixo")
    @GetMapping("/stock/low")
    public List<LowStockResponse> getLowStock(
            @Parameter(description = "Limite mínimo de estoque. Se não informado, usa o padrão de 5.")
            @RequestParam(required = false) Integer threshold) {
        return inventoryService.getLowStockProducts(threshold);
    }
}