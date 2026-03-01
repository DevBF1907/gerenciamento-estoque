package gerenciamento_estoque_api.repository;

import gerenciamento_estoque_api.model.Product;
import gerenciamento_estoque_api.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Product> findByName(String name);
}