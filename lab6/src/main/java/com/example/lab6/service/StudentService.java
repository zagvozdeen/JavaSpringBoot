package com.example.lab6.service;

import com.example.lab6.entity.Student;
import java.util.List;

public interface StudentService {
    List<Student> findAll();
    Student findById(int id);
    Student create(Student entity);
    Student update(Student entity);
    void delete(int id);
}
