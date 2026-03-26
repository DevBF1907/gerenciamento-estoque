package gerenciamento_estoque_api.service;

import gerenciamento_estoque_api.dto.request.CreateWarehouseRequest;
import gerenciamento_estoque_api.dto.response.WarehouseResponse;
import gerenciamento_estoque_api.exception.ResourceNotFoundException;
import gerenciamento_estoque_api.model.Warehouse;
import gerenciamento_estoque_api.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseResponse create(CreateWarehouseRequest request) {
        if (warehouseRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("Warehouse with name '" + request.name() + "' already exists");
        }
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder().name(request.name()).build());
        return toResponse(warehouse);
    }

    public List<WarehouseResponse> findAll() {
        return warehouseRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Warehouse findById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    private WarehouseResponse toResponse(Warehouse w) {
        return new WarehouseResponse(w.getId(), w.getName());
    }
}
