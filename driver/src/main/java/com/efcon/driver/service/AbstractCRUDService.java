package com.efcon.driver.service;

import com.efcon.driver.mapper.CRUDMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.NoSuchElementException;

public abstract class AbstractCRUDService<E, T, K> implements CRUDService<T, K> {
    protected abstract JpaRepository<E, Long> getRepository();
    protected abstract CRUDMapper<E, T, K> getMapper();
    protected abstract String getEntityName();

    @Override
    public List<K> getAll() {
        return getMapper().toResponseDtoList(getRepository().findAll());
    }

    @Override
    public K get(Long id) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new NoSuchElementException(getEntityName() + " with id " + id + " not found"));
        return getMapper().toResponseDto(entity);
    }

    @Override
    public K create(T dto) {
        E newEntity = getMapper().fromRequestDto(dto);
        return getMapper().toResponseDto(getRepository().save(newEntity));
    }

    @Override
    public K update(Long id, T dto) {
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new NoSuchElementException(getEntityName() + " with id " + id + " not found"));
        getMapper().updateEntityFromDto(dto, entity);
        return getMapper().toResponseDto(getRepository().save(entity));
    }

    @Override
    public void delete(Long id) {
        getRepository().deleteById(id);
    }
}
