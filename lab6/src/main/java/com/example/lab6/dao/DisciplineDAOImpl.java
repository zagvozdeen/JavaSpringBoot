package com.example.lab6.dao;

import com.example.lab6.entity.Discipline;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DisciplineDAOImpl implements DisciplineDAO {
    private final EntityManager entityManager;

    @Override
    public List<Discipline> findAll() {
        return entityManager.createQuery("from Discipline order by id", Discipline.class).getResultList();
    }

    @Override
    public Discipline findById(int id) {
        return entityManager.find(Discipline.class, id);
    }

    @Override
    public Discipline save(Discipline entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        }
        return entityManager.merge(entity);
    }

    @Override
    public void delete(Discipline entity) {
        entityManager.remove(entity);
    }
}
