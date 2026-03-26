package gerenciamento_estoque_api;

import gerenciamento_estoque_api.dto.request.CreateWarehouseRequest;
import gerenciamento_estoque_api.dto.response.WarehouseResponse;
import gerenciamento_estoque_api.exception.ResourceNotFoundException;
import gerenciamento_estoque_api.model.Warehouse;
import gerenciamento_estoque_api.repository.WarehouseRepository;
import gerenciamento_estoque_api.service.WarehouseService;
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
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    @Test
    void create_deveSalvarERetornarArmazem() {
        when(warehouseRepository.findByName("Armazém SP")).thenReturn(Optional.empty());
        when(warehouseRepository.save(any())).thenReturn(Warehouse.builder().id(1L).name("Armazém SP").build());

        WarehouseResponse response = warehouseService.create(new CreateWarehouseRequest("Armazém SP"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Armazém SP");
    }

    @Test
    void create_deveLancarExcecaoParaNomeDuplicado() {
        when(warehouseRepository.findByName("Armazém SP"))
                .thenReturn(Optional.of(Warehouse.builder().id(1L).name("Armazém SP").build()));

        assertThatThrownBy(() -> warehouseService.create(new CreateWarehouseRequest("Armazém SP")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Armazém SP");
    }

    @Test
    void findAll_deveRetornarListaDeArmazens() {
        when(warehouseRepository.findAll()).thenReturn(List.of(
                Warehouse.builder().id(1L).name("Armazém SP").build(),
                Warehouse.builder().id(2L).name("Armazém RJ").build()
        ));

        List<WarehouseResponse> response = warehouseService.findAll();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).name()).isEqualTo("Armazém SP");
        assertThat(response.get(1).name()).isEqualTo("Armazém RJ");
    }

    @Test
    void findAll_deveRetornarListaVaziaQuandoNaoHaArmazens() {
        when(warehouseRepository.findAll()).thenReturn(List.of());

        List<WarehouseResponse> response = warehouseService.findAll();

        assertThat(response).isEmpty();
    }

    @Test
    void findById_deveRetornarArmazemQuandoExistir() {
        when(warehouseRepository.findById(1L))
                .thenReturn(Optional.of(Warehouse.builder().id(1L).name("Armazém SP").build()));

        Warehouse warehouse = warehouseService.findById(1L);

        assertThat(warehouse.getId()).isEqualTo(1L);
        assertThat(warehouse.getName()).isEqualTo("Armazém SP");
    }

    @Test
    void findById_deveLancarExcecaoQuandoNaoEncontrar() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}