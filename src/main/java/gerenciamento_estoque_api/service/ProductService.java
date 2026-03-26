package gerenciamento_estoque_api.service;

import gerenciamento_estoque_api.dto.request.CreateProductRequest;
import gerenciamento_estoque_api.dto.response.ProductResponse;
import gerenciamento_estoque_api.exception.ResourceNotFoundException;
import gerenciamento_estoque_api.model.Product;
import gerenciamento_estoque_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("Product with name '" + request.name() + "' already exists");
        }
        Product product = productRepository.save(Product.builder().name(request.name()).build());
        return toResponse(product);
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName());
    }
}