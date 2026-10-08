package com.example.lab6.service;

import com.example.lab6.dao.DisciplineDAO;
import com.example.lab6.entity.Discipline;
import com.example.lab6.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisciplineServiceImpl implements DisciplineService {
    private final DisciplineDAO dao;

    @Override
    public List<Discipline> findAll() {
        return dao.findAll();
    }

    @Override
    public Discipline findById(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("id должен быть положительным");
        }
        Discipline entity = dao.findById(id);
        if (entity == null) {
            throw new NotFoundException("Дисциплина не найдена");
        }
        return entity;
    }

    @Override
    @Transactional
    public Discipline create(Discipline entity) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("При создании id не задается");
        }
        return dao.save(entity);
    }

    @Override
    @Transactional
    public Discipline update(Discipline entity) {
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
