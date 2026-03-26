package gerenciamento_estoque_api.controller;

import gerenciamento_estoque_api.dto.request.CreateWarehouseRequest;
import gerenciamento_estoque_api.dto.response.WarehouseResponse;
import gerenciamento_estoque_api.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Warehouses", description = "Gerenciamento de armazéns")
@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "Cadastrar armazém")
    @ApiResponse(responseCode = "201", description = "Armazém criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Nome inválido ou já existente")
    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.create(request));
    }

    @Operation(summary = "Listar todos os armazéns")
    @ApiResponse(responseCode = "200", description = "Lista de armazéns")
    @GetMapping
    public List<WarehouseResponse> findAll() {
        return warehouseService.findAll();
    }
}