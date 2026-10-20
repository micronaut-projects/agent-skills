package com.example.calc;

import jakarta.inject.Singleton;

@Singleton
public class WindowsCalculator implements Calculator {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
