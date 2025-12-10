package fr.github.vera.services;

import fr.github.vera.model.Identifiable;
import fr.github.vera.repository.IRepository;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T extends Identifiable<I>, I, R extends IRepository<T, I>> {
    protected final R repository;

    protected BaseService(R repository) {
        this.repository = repository;
    }

    public List<T> getAll(int limit, int offset) {
        return repository.findAll(limit, offset);
    }

    public Optional<T> getById(I id) {
        return repository.findById(id);
    }

    public T create(T entity) {
        return repository.save(entity);
    }

    public T update(I id, T entity) {
        entity.setId(id);
        return repository.save(entity);
    }


    public boolean delete(I id) {
        return repository.delete(id);
    }


    public int count() {
        return repository.count();
    }
}
