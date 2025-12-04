package fr.github.vera.repository;

import java.util.List;
import java.util.Optional;

// Interface générique pour toutes les entités
public interface IRepository<T, I> {
    Optional<T> findById(I id);

    T save(T entity);

    boolean delete(I id);

    List<T> findAll(int limit, int offset);

    int count();
}