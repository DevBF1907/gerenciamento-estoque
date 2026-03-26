package gerenciamento_estoque_api;

import gerenciamento_estoque_api.dto.request.CreateProductRequest;
import gerenciamento_estoque_api.dto.response.ProductResponse;
import gerenciamento_estoque_api.exception.ResourceNotFoundException;
import gerenciamento_estoque_api.model.Product;
import gerenciamento_estoque_api.repository.ProductRepository;
import gerenciamento_estoque_api.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_deveSalvarERetornarProduto() {
        when(productRepository.findByName("Widget A")).thenReturn(Optional.empty());
        when(productRepository.save(any())).thenReturn(Product.builder().id(1L).name("Widget A").build());

        ProductResponse response = productService.create(new CreateProductRequest("Widget A"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Widget A");
    }

    @Test
    void create_deveLancarExcecaoParaNomeDuplicado() {
        when(productRepository.findByName("Widget A"))
                .thenReturn(Optional.of(Product.builder().id(1L).name("Widget A").build()));

        assertThatThrownBy(() -> productService.create(new CreateProductRequest("Widget A")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Widget A");
    }

    @Test
    void findAll_deveRetornarListaDeProdutos() {
        when(productRepository.findAll()).thenReturn(List.of(
                Product.builder().id(1L).name("Widget A").build(),
                Product.builder().id(2L).name("Gadget B").build()
        ));

        List<ProductResponse> response = productService.findAll();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).name()).isEqualTo("Widget A");
        assertThat(response.get(1).name()).isEqualTo("Gadget B");
    }

    @Test
    void findAll_deveRetornarListaVaziaQuandoNaoHaProdutos() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductResponse> response = productService.findAll();

        assertThat(response).isEmpty();
    }

    @Test
    void findById_deveRetornarProdutoQuandoExistir() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(Product.builder().id(1L).name("Widget A").build()));

        Product product = productService.findById(1L);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Widget A");
    }

    @Test
    void findById_deveLancarExcecaoQuandoNaoEncontrar() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}