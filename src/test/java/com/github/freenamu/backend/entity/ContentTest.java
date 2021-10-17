package com.github.freenamu.backend.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContentTest {
    @Test
    public void should_have_create_date_when_created_with_no_argument() {
        // Given
        Content content = new Content();

        // When
        String createDateTime = content.getCreateDateTime();

        // Then
        assertNotNull(createDateTime);
    }

    @Test
    public void should_have_create_date_when_created_with_argument() {
        // Given
        Content content = new Content("test", "test", "test");

        // When
        String createDateTime = content.getCreateDateTime();

        // Then
        assertNotNull(createDateTime);
    }

    @Test
    public void should_trim_content_body_when_set_content_body_with_redirect() {
        // Given
        Content content = new Content("#redirect test\ntest", "test", "test");
        String expected = "#redirect test";

        // When
        String actual = content.getContentBody();

        // Then
        assertEquals(expected, actual);
    }
}
