package com.example.fibonacci.controller;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.example.fibonacci.service.FibonacciService;

@Controller
public class FibonacciController {

    @Autowired
    private FibonacciService service;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/calculate")
    public String calculateFibonacci(@RequestParam Integer number, Model model) {
        try {
            BigInteger result = service.getFibonacci(number);
            model.addAttribute("result", result);
            model.addAttribute("number", number);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "The number must be non-negative.");
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while calculating the Fibonacci number.");
        }
        return "result";
    }
}
