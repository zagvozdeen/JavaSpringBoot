package com.example.lab6.service;

import com.example.lab6.entity.Discipline;
import java.util.List;

public interface DisciplineService {
    List<Discipline> findAll();
    Discipline findById(int id);
    Discipline create(Discipline entity);
    Discipline update(Discipline entity);
    void delete(int id);
}
