package com.example.lab6.dao;

import com.example.lab6.entity.Student;
import java.util.List;

public interface StudentDAO {
    List<Student> findAll();
    Student findById(int id);
    Student save(Student entity);
    void delete(Student entity);
}
