package com.example.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "h2")
class BookRepositoryTest {
    @Test
    void savesABook() {
    }
}
