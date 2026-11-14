package com.udea.demomcp.service;

import com.udea.demomcp.model.Tarea;
import com.udea.demomcp.repository.TareaRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

@Component
public class TareaToolsService {

    private final TareaRepository repo;

    private static final DateTimeFormatter ISO_DATE_STRICT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    public TareaToolsService(TareaRepository repo) {
        this.repo = repo;
    }

    private void validarFechaIso(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            throw new IllegalArgumentException("La fecha es obligatoria y debe tener formato ISO yyyy-MM-dd.");
        }
        try {
            LocalDate.parse(fecha.trim(), ISO_DATE_STRICT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Formato de fecha inválido: '" + fecha + "'. Debe cumplir el estándar ISO yyyy-MM-dd (ej. 2026-11-20).");
        }
    }

    // ---------- READ ----------

    @Tool(description = "Devuelve la lista completa de tareas almacenadas.")
    public List<Tarea> listarTareas() {
        return repo.findAll();
    }

    @Tool(name = "listPendientes", description = "Devuelve únicamente las tareas pendientes (completado = false).")
    public List<Tarea> listarPendientes() {
        return repo.findByCompletado(false);
    }

    @Tool(name = "listarCompletadas", description = "Devuelve únicamente las tareas que ya fueron completadas (completado = true).")
    public List<Tarea> listarCompletadas() {
        return repo.findByCompletado(true);
    }

    @Tool(name = "contarTareas", description = "Devuelve el número total de tareas registradas en la base de datos.")
    public long contarTareas() {
        return repo.count();
    }

    @Tool(name = "findTareas", description = "Busca tareas cuyo título contiene el fragmento indicado (case-insensitive).")
    public List<Tarea> buscarTareas(
            @ToolParam(description = "Fragmento de texto a buscar en el título") String fragmento) {
        return repo.findByTituloContainingIgnoreCase(fragmento);
    }

    // ---------- CREATE ----------

    @Tool(name = "crearTarea", description = "Crea una nueva tarea con título y fecha en formato ISO yyyy-MM-dd; queda pendiente por defecto.")
    @Transactional
    public Tarea crearTarea(
            @ToolParam(description = "Título descriptivo de la tarea") String titulo,
            @ToolParam(description = "Fecha obligatoria en formato ISO yyyy-MM-dd (ej. 2026-11-20)") String fecha) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        validarFechaIso(fecha);
        Tarea t = new Tarea(titulo.trim(), fecha.trim(), Boolean.FALSE);
        return repo.save(t);
    }

    // ---------- UPDATE ----------

    @Tool(name = "marcarCompletado", description = "Marca como completada la tarea con el id indicado.")
    @Transactional
    public Tarea marcarCompletada(
            @ToolParam(description = "Identificador único de la tarea") Long id) {
        Tarea t = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la tarea " + id));
        t.setCompletado(Boolean.TRUE);
        return repo.save(t);
    }

    @Tool(name = "updateTarea", description = "Actualiza el título o la fecha (ISO yyyy-MM-dd) de una tarea existente. Los parámetros nulos no se modifican.")
    @Transactional
    public Tarea actualizarTarea(
            @ToolParam(description = "Id de la tarea a actualizar") Long id,
            @ToolParam(description = "Nuevo título (opcional, null = no cambiar)") String titulo,
            @ToolParam(description = "Nueva fecha en formato ISO yyyy-MM-dd (opcional, null = no cambiar)") String fecha) {
        Tarea t = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la tarea " + id));
        if (titulo != null && !titulo.isBlank()) {
            t.setTitulo(titulo.trim());
        }
        if (fecha != null && !fecha.isBlank()) {
            validarFechaIso(fecha);
            t.setFecha(fecha.trim());
        }
        return repo.save(t);
    }

    // ---------- DELETE ----------

    @Tool(name = "eliminarTarea", description = "Elimina la tarea con el id indicado. Devuelve un mensaje de confirmación.")
    @Transactional
    public String eliminarTarea(
            @ToolParam(description = "Id de la tarea a eliminar") Long id) {
        if (!repo.existsById(id)) {
            return "La tarea " + id + " no existe.";
        }
        repo.deleteById(id);
        return "Tarea " + id + " eliminada correctamente.";
    }
}