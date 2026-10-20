package com.example.topics;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Response-only representation of a topic. It is written by the API and never read from a request.
 */
@Serdeable.Serializable
public record TopicView(String id, String name, int questionCount) {
}
