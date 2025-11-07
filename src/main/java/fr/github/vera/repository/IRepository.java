package fr.github.vera.repository;

import java.util.List;
import java.util.Optional;

// Interface générique pour toutes les entités
public interface IRepository<T, ID> {
    Optional<T> findById(ID id);

    T save(T entity);

    boolean delete(ID id);

    List<T> findAll(int limit, int offset);

    int count();
}