package com.example.contacts;

import jakarta.inject.Singleton;

import java.util.List;

@Singleton
class JdbcContactRepository implements ContactRepository {
    @Override
    public List<Contact> findAll() {
        throw new UnsupportedOperationException("talks to a real database");
    }
}
