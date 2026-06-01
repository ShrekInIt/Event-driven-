package com.example.events.event.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankText() {
        assertThat(validator.validate(new EventRequest(" "))).isNotEmpty();
    }

    @Test
    void acceptsNonBlankText() {
        assertThat(validator.validate(new EventRequest("message"))).isEmpty();
    }
}
