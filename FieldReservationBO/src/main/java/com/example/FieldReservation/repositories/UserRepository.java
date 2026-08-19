package com.example.FieldReservation.repositories;



import com.example.FieldReservation.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,String> {}
