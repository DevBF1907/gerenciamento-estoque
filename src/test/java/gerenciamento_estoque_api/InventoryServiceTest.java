package gerenciamento_estoque_api;

import gerenciamento_estoque_api.dto.request.StockRequest;
import gerenciamento_estoque_api.dto.response.*;
import gerenciamento_estoque_api.exception.InsufficientStockException;
import gerenciamento_estoque_api.model.Inventory;
import gerenciamento_estoque_api.model.Product;
import gerenciamento_estoque_api.model.Warehouse;
import gerenciamento_estoque_api.repository.InventoryRepository;
import gerenciamento_estoque_api.service.InventoryService;
import gerenciamento_estoque_api.service.ProductService;
import gerenciamento_estoque_api.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ProductService productService;
    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inventoryService, "defaultLowThreshold", 5);
        product = Product.builder().id(1L).name("Widget A").build();
        warehouse = Warehouse.builder().id(1L).name("Armazém SP").build();
    }

    @Test
    void addStock_deveCriarInventarioSeNaoExistir() {
        when(productService.findById(1L)).thenReturn(product);
        when(warehouseService.findById(1L)).thenReturn(warehouse);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.addStock(1L, new StockRequest(1L, 10));

        verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 10));
    }

    @Test
    void addStock_deveAcumularQuantidade() {
        Inventory existente = Inventory.builder().product(product).warehouse(warehouse).quantity(5).build();
        when(productService.findById(1L)).thenReturn(product);
        when(warehouseService.findById(1L)).thenReturn(warehouse);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existente));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.addStock(1L, new StockRequest(1L, 3));

        verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 8));
    }

    @Test
    void removeStock_deveLancarExcecaoQuandoEstoqueInsuficiente() {
        Inventory existente = Inventory.builder().product(product).warehouse(warehouse).quantity(2).build();
        when(productService.findById(1L)).thenReturn(product);
        when(warehouseService.findById(1L)).thenReturn(warehouse);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> inventoryService.removeStock(1L, new StockRequest(1L, 5)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Cannot remove 5 units");
    }

    @Test
    void removeStock_deveSubtrairQuantidadeCorretamente() {
        Inventory existente = Inventory.builder().product(product).warehouse(warehouse).quantity(10).build();
        when(productService.findById(1L)).thenReturn(product);
        when(warehouseService.findById(1L)).thenReturn(warehouse);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existente));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.removeStock(1L, new StockRequest(1L, 4));

        verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 6));
    }

    @Test
    void removeStock_devePermitirRemoverTodoOEstoque() {
        Inventory existente = Inventory.builder().product(product).warehouse(warehouse).quantity(5).build();
        when(productService.findById(1L)).thenReturn(product);
        when(warehouseService.findById(1L)).thenReturn(warehouse);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(existente));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.removeStock(1L, new StockRequest(1L, 5));

        verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 0));
    }

    @Test
    void getProductStock_deveRetornarTotalEDistribuicaoPorArmazem() {
        Warehouse w2 = Warehouse.builder().id(2L).name("Armazém RJ").build();
        List<Inventory> inventarios = List.of(
                Inventory.builder().product(product).warehouse(warehouse).quantity(10).build(),
                Inventory.builder().product(product).warehouse(w2).quantity(5).build()
        );
        when(productService.findById(1L)).thenReturn(product);
        when(inventoryRepository.findByProductId(1L)).thenReturn(inventarios);

        ProductStockResponse response = inventoryService.getProductStock(1L);

        assertThat(response.totalQuantity()).isEqualTo(15);
        assertThat(response.inventoryByWarehouse()).hasSize(2);
    }

    @Test
    void getProductStock_deveRetornarZeroQuandoSemEstoque() {
        when(productService.findById(1L)).thenReturn(product);
        when(inventoryRepository.findByProductId(1L)).thenReturn(List.of());

        ProductStockResponse response = inventoryService.getProductStock(1L);

        assertThat(response.totalQuantity()).isEqualTo(0);
        assertThat(response.inventoryByWarehouse()).isEmpty();
    }

    @Test
    void getLowStockProducts_deveUsarThresholdPadraoQuandoNulo() {
        when(inventoryRepository.findProductsWithTotalQuantityBelow(5)).thenReturn(List.of());

        inventoryService.getLowStockProducts(null);

        verify(inventoryRepository).findProductsWithTotalQuantityBelow(5);
    }

    @Test
    void getLowStockProducts_deveUsarThresholdInformado() {
        when(inventoryRepository.findProductsWithTotalQuantityBelow(10)).thenReturn(List.of());

        inventoryService.getLowStockProducts(10);

        verify(inventoryRepository).findProductsWithTotalQuantityBelow(10);
    }
}