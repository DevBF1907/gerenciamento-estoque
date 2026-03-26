package gerenciamento_estoque_api.repository;

import gerenciamento_estoque_api.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface
InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<Inventory> findByProductId(Long productId);

    List<Inventory> findByWarehouseId(Long warehouseId);

    @Query("SELECT i.product.id, SUM(i.quantity) FROM Inventory i GROUP BY i.product.id HAVING SUM(i.quantity) < :threshold")
    List<Object[]> findProductsWithTotalQuantityBelow(@Param("threshold") int threshold);
}