package com.udea.demomcp.service;

import com.udea.demomcp.model.Tarea;
import com.udea.demomcp.repository.TareaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaToolsServiceTest {

    @Mock
    private TareaRepository repo;

    @InjectMocks
    private TareaToolsService service;

    @Test
    void contarTareas_debeRetornarTotalDelRepositorio() {
        when(repo.count()).thenReturn(3L);

        long total = service.contarTareas();

        assertEquals(3L, total);
        verify(repo, times(1)).count();
    }

    @Test
    void listarCompletadas_debeConsultarSoloCompletadasEnTrue() {
        Tarea t1 = new Tarea("Probar demo en vivo", "2026-10-26", true);
        when(repo.findByCompletado(true)).thenReturn(List.of(t1));

        List<Tarea> completadas = service.listarCompletadas();

        assertEquals(1, completadas.size());
        assertTrue(completadas.get(0).getCompletado());
        verify(repo).findByCompletado(true);
    }

    @Test
    void crearTarea_conFechaIsoValida_debeGuardarTareaPendiente() {
        when(repo.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        Tarea creada = service.crearTarea("Entregar laboratorio MCP", "2026-11-15");

        assertEquals("Entregar laboratorio MCP", creada.getTitulo());
        assertEquals("2026-11-15", creada.getFecha());
        assertFalse(creada.getCompletado());
    }

    @Test
    void crearTarea_conFechaInvalida_debeLanzarExcepcion() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearTarea("Tarea con fecha mala", "15/11/2026")
        );

        assertTrue(ex.getMessage().contains("yyyy-MM-dd"));
        verify(repo, never()).save(any());
    }

    @Test
    void crearTarea_conFechaInexistenteEnCalendario_debeLanzarExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.crearTarea("Fecha imposible", "2026-02-30")
        );
        verify(repo, never()).save(any());
    }
}