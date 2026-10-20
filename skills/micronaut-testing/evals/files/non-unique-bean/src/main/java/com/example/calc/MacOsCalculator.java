package com.example.calc;

import jakarta.inject.Singleton;

@Singleton
public class MacOsCalculator implements Calculator {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
