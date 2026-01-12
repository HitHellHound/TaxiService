package com.efcon.ride.service;

import java.util.List;

public interface CRUDService<T, K> {
    List<K> getAll();
    K get(Long id);
    K create(T request);
    K update(Long id, T request);
    void delete(Long id);
}
