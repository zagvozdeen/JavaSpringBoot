package com.example.lab6.service;

import com.example.lab6.dao.StudentDAO;
import com.example.lab6.entity.Student;
import com.example.lab6.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {
    private final StudentDAO dao;

    @Override
    public List<Student> findAll() {
        return dao.findAll();
    }

    @Override
    public Student findById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("id должен быть положительным");
        }
        Student entity = dao.findById(id);
        if (entity == null) {
            throw new NotFoundException("Студент не найден");
        }
        return entity;
    }

    @Override
    @Transactional
    public Student create(Student entity) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("При создании id не задается");
        }
        return dao.save(entity);
    }

    @Override
    @Transactional
    public Student update(Student entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Для изменения нужен id");
        }
        findById(entity.getId());
        return dao.save(entity);
    }

    @Override
    @Transactional
    public void delete(int id) {
        dao.delete(findById(id));
    }
}
