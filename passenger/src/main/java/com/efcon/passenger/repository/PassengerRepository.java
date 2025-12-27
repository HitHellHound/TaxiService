package com.efcon.passenger.repository;

import com.efcon.passenger.model.Passenger;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface PassengerRepository extends ListCrudRepository<Passenger, Long> {

}
