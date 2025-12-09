package com.efcon.passenger.service;

import java.util.List;

public interface CRUDService<T, K> {
    List<K> getAll();
    K get(Long id);
    K create(T dto);
    K update(Long id, T dto);
    void delete(Long id);
}
