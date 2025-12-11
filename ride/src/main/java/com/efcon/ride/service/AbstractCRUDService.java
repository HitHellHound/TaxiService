package com.efcon.ride.service;

import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.mapper.CRUDMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class AbstractCRUDService<E, T, K> implements CRUDService<T, K> {
    protected abstract JpaRepository<E, Long> getRepository();
    protected abstract CRUDMapper<E, T, K> getMapper();
    protected abstract String getEntityName();

    @Override
    public List<K> getAll() {
        return getMapper().toResponseList(getRepository().findAll());
    }

    @Override
    public K get(Long id) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityName() + " with id " + id + " not found"));
        return getMapper().toResponse(entity);
    }

    @Override
    public K create(T request) {
        E newEntity = getMapper().fromRequest(request);
        return getMapper().toResponse(getRepository().save(newEntity));
    }

    @Override
    public K update(Long id, T request) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityName() + " with id " + id + " not found"));
        getMapper().updateEntityFromRequest(request, entity);
        return getMapper().toResponse(getRepository().save(entity));
    }

    @Override
    public void delete(Long id) {
        getRepository().deleteById(id);
    }
}
