package gerenciamento_estoque_api.repository;

import gerenciamento_estoque_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
}
