package com.example.lab6.controller;

import com.example.lab6.entity.Discipline;
import com.example.lab6.model.ApiResponse;
import com.example.lab6.service.DisciplineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/disciplines")
@RequiredArgsConstructor
public class DisciplineController {
    private final DisciplineService service;

    @GetMapping
    public ApiResponse<List<Discipline>> findAll() {
        return ApiResponse.ok("Список получен", service.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Discipline> findById(@PathVariable int id) {
        return ApiResponse.ok("Запись получена", service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Discipline>> create(@Valid @RequestBody Discipline entity) {
        Discipline saved = service.create(entity);
        return ResponseEntity.created(URI.create("/api/disciplines/" + saved.getId()))
                .body(ApiResponse.ok("Запись создана", saved));
    }

    @PutMapping
    public ApiResponse<Discipline> update(@Valid @RequestBody Discipline entity) {
        return ApiResponse.ok("Запись изменена", service.update(entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable int id) {
        service.delete(id);
        return ApiResponse.ok("Запись удалена", null);
    }
}
