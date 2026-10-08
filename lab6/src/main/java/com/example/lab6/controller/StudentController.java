package com.example.lab6.controller;

import com.example.lab6.entity.Student;
import com.example.lab6.model.ApiResponse;
import com.example.lab6.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService service;

    @GetMapping
    public ApiResponse<List<Student>> findAll() {
        return ApiResponse.ok("Список получен", service.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Student> findById(@PathVariable int id) {
        return ApiResponse.ok("Запись получена", service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Student>> create(@Valid @RequestBody Student entity) {
        Student saved = service.create(entity);
        return ResponseEntity.created(URI.create("/api/students/" + saved.getId()))
                .body(ApiResponse.ok("Запись создана", saved));
    }

    @PutMapping
    public ApiResponse<Student> update(@Valid @RequestBody Student entity) {
        return ApiResponse.ok("Запись изменена", service.update(entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable int id) {
        service.delete(id);
        return ApiResponse.ok("Запись удалена", null);
    }
}
