package com.example.lab6.dao;

import com.example.lab6.entity.Discipline;
import java.util.List;

public interface DisciplineDAO {
    List<Discipline> findAll();
    Discipline findById(int id);
    Discipline save(Discipline entity);
    void delete(Discipline entity);
}
