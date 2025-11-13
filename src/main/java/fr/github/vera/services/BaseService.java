package fr.github.vera.services;

import fr.github.vera.repository.IRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID, R extends IRepository<T, ID>> {
    protected final R repository;

    public BaseService(R repository) {
        this.repository = repository;
    }

    public List<T> getAll(int limit, int offset) {
        return repository.findAll(limit, offset);
    }

    public Optional<T> getById(ID id) {
        return repository.findById(id);
    }

    public T create(T entity) {
        return repository.save(entity);
    }


    public T update(ID id, T entity) {
        setId(entity, id);
        return repository.save(entity);
    }


    public boolean delete(ID id) {
        return repository.delete(id);
    }


    public int count() {
        return repository.count();
    }

    private void setId(T entity, ID id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not set ID on entity", e);
        }
    }
}
