package com.example.topics;

import jakarta.validation.constraints.NotBlank;

public interface TopicRepository {
    void save(@NotBlank String name);
}
