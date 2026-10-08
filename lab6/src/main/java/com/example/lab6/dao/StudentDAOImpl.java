package com.example.lab6.dao;

import com.example.lab6.entity.Student;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StudentDAOImpl implements StudentDAO {
    private final EntityManager entityManager;

    @Override
    public List<Student> findAll() {
        return entityManager.createQuery("from Student order by id", Student.class).getResultList();
    }

    @Override
    public Student findById(int id) {
        return entityManager.find(Student.class, id);
    }

    @Override
    public Student save(Student entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        }
        return entityManager.merge(entity);
    }

    @Override
    public void delete(Student entity) {
        entityManager.remove(entity);
    }
}
