package com.example.contacts;

import java.util.List;

public interface ContactRepository {
    List<Contact> findAll();
}
