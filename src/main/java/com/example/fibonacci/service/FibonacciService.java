package com.example.fibonacci.service;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.fibonacci.entity.Fibonacci;
import com.example.fibonacci.repository.FibonacciRepository;

@Service
public class FibonacciService {

    @Autowired
    private FibonacciRepository repository;

    // HashMap para armazenar resultados de Fibonacci já calculados
    private Map<Integer, BigInteger> fibonacciCache = new HashMap<>();

    public BigInteger getFibonacci(Integer number) {
        if (number == null || number < 0) {
            throw new IllegalArgumentException("The number must be non-negative.");
        }

        // Verifica se o resultado já está no cache
        if (fibonacciCache.containsKey(number)) {
            return fibonacciCache.get(number);
        }

        // Verifica se o resultado já está no banco de dados
        try {
            Fibonacci fibonacci = repository.findByNumber(number);
            if (fibonacci != null) {
                BigInteger result = new BigInteger(fibonacci.getResult());
                fibonacciCache.put(number, result);  // Armazena no cache
                return result;
            }
        } catch (Exception e) {
            System.err.println("Error accessing the database: " + e.getMessage());
            throw new RuntimeException("Database access error", e);
        }

        // Calcula o número de Fibonacci se não estiver no cache ou no banco de dados
        BigInteger result = fib(BigInteger.valueOf(number));

        // Armazena o resultado no banco de dados e no cache
        Fibonacci newFibonacci = new Fibonacci();
        newFibonacci.setNumber(number);
        newFibonacci.setResult(result.toString());

        try {
            repository.save(newFibonacci);
            fibonacciCache.put(number, result);
        } catch (Exception e) {
            System.err.println("Error saving to the database: " + e.getMessage());
            throw new RuntimeException("Database save error", e);
        }

        return result;
    }

    // Optimized Fibonacci calculation using dynamic programming
    private BigInteger fib(BigInteger n) {
        if (n.equals(BigInteger.ZERO))
            return BigInteger.ZERO;
        if (n.equals(BigInteger.ONE) || n.equals(BigInteger.valueOf(2)))
            return BigInteger.ONE;
        
        BigInteger index = n;
        
        // Use TreeMap to keep keys sorted
        Map<BigInteger,BigInteger> termsToCalculate = new TreeMap<BigInteger,BigInteger>();
        
        // Populate map with terms required for calculation
        populateMapWithTerms(termsToCalculate, index);
        
        termsToCalculate.put(n, null); // Add n to map
        
        Iterator<Map.Entry<BigInteger, BigInteger>> it = termsToCalculate.entrySet().iterator();
        it.next(); // it = key number 1, contains fib(1);
        it.next(); // it = key number 2, contains fib(2);
        
        // Map is ordered
        while (it.hasNext()) {
            Map.Entry<BigInteger, BigInteger> pair = it.next(); // First it = key number 3
            index = pair.getKey();
            
            if(index.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
                // Index is divisible by 2
                // F(2k) = F(k)[2F(k+1)−F(k)]
                pair.setValue(termsToCalculate.get(index.divide(BigInteger.valueOf(2))).multiply(
                        (((BigInteger.valueOf(2)).multiply(
                                termsToCalculate.get(index.divide(BigInteger.valueOf(2)).add(BigInteger.ONE)))).subtract(
                                        termsToCalculate.get(index.divide(BigInteger.valueOf(2)))))));
            }
            else {
                // Index is odd
                // F(2k+1) = F(k+1)^2+F(k)^2
                pair.setValue((termsToCalculate.get(index.divide(BigInteger.valueOf(2)).add(BigInteger.ONE)).multiply(
                        termsToCalculate.get(index.divide(BigInteger.valueOf(2)).add(BigInteger.ONE)))).add(
                                (termsToCalculate.get(index.divide(BigInteger.valueOf(2))).multiply(
                                termsToCalculate.get(index.divide(BigInteger.valueOf(2))))))
                        );
            }
        }
        
        // fib(n) was calculated in the while loop
        return termsToCalculate.get(n);
    }

    // Helper method to populate map with terms required for Fibonacci calculation
    private void populateMapWithTerms(Map<BigInteger, BigInteger> termsToCalculate, BigInteger index) {
        if (index.equals(BigInteger.ONE)) { // Stop
            termsToCalculate.put(BigInteger.ONE, BigInteger.ONE);
            return;
            
        } else if(index.equals(BigInteger.valueOf(2))){
            termsToCalculate.put(BigInteger.valueOf(2), BigInteger.ONE);
            return;
            
        } else if(index.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            // Index is divisible by 2
            // FORMULA: F(2k) = F(k)[2F(k+1)−F(k)]
                        
            // Add F(k) key to termsToCalculate (key is replaced if already present)
            termsToCalculate.put(index.divide(BigInteger.valueOf(2)), null);
            populateMapWithTerms(termsToCalculate, index.divide(BigInteger.valueOf(2)));

            // Add F(k+1) to termsToCalculate
            termsToCalculate.put(index.divide(BigInteger.valueOf(2)).add(BigInteger.ONE), null);
            populateMapWithTerms(termsToCalculate, index.divide(BigInteger.valueOf(2)).add(BigInteger.ONE));
            
        } else {
            // Index is odd
            // FORMULA: F(2k+1) = F(k+1)^2+F(k)^2

            // Add F(k+1) to termsToCalculate
            termsToCalculate.put(((index.subtract(BigInteger.ONE)).divide(BigInteger.valueOf(2)).add(BigInteger.ONE)), null);
            populateMapWithTerms(termsToCalculate, ((index.subtract(BigInteger.ONE)).divide(BigInteger.valueOf(2)).add(BigInteger.ONE)));

            // Add F(k) to termsToCalculate
            termsToCalculate.put((index.subtract(BigInteger.ONE)).divide(BigInteger.valueOf(2)), null);
            populateMapWithTerms(termsToCalculate, (index.subtract(BigInteger.ONE)).divide(BigInteger.valueOf(2)));
        }
    }
}
