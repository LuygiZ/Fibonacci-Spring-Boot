package com.example.fibonacci.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fibonacci.entity.Fibonacci;

@Repository
public interface FibonacciRepository extends JpaRepository<Fibonacci, Long> {
    Fibonacci findByNumber(Integer number);
}
