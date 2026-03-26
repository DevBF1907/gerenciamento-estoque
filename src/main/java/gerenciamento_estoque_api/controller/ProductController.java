package gerenciamento_estoque_api.controller;

import gerenciamento_estoque_api.dto.request.CreateProductRequest;
import gerenciamento_estoque_api.dto.response.ProductResponse;
import gerenciamento_estoque_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gerenciamento_estoque_api.dto.request.CreateProductRequest;
import gerenciamento_estoque_api.dto.response.ProductResponse;
import gerenciamento_estoque_api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Products", description = "Gerenciamento de produtos")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Cadastrar produto")
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Nome inválido ou já existente")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @Operation(summary = "Listar todos os produtos")
    @ApiResponse(responseCode = "200", description = "Lista de produtos")
    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.findAll();
    }
}