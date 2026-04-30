package com.gigaspaces.demo.rest;

import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP response wrapper with fluent assertion methods.
 */
public class Response {
    private final int statusCode;
    private final String body;

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body != null ? body : "";
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isStatus(int expected) {
        return statusCode == expected;
    }

    public boolean isStatusIn(int... expected) {
        return Arrays.stream(expected).anyMatch(s -> s == statusCode);
    }

    /**
     * Assert the response has the expected status code.
     */
    public Response assertStatus(int expected) {
        assertEquals(expected, statusCode,
                String.format("Expected status %d but got %d. Body: %s", expected, statusCode, truncateBody()));
        return this;
    }


    /**
     * Assert the response has one of the expected status codes.
     */
    public Response assertStatusIn(int... expected) {
        return assertStatusIn("", expected);
    }

    public Response assertStatusIn(String assertMessage, int... expected) {
        assertTrue(isStatusIn(expected),
                String.format("%s. Expected status in %s but got %d. Body: %s",
                        assertMessage, Arrays.toString(expected), statusCode, truncateBody()));
        return this;
    }
    /**
     * Assert the response body contains the expected text.
     */
    public Response assertBodyContains(String text) {
        assertTrue(body.contains(text),
                String.format("Expected body to contain '%s'. Status: %d, Body: %s", text, statusCode, truncateBody()));
        return this;
    }


    /**
     * Assert the response body does not contain the specified text.
     * not used
    public Response assertBodyNotContains(String text) {
        assertFalse(body.contains(text),
                String.format("Expected body to NOT contain '%s'. Status: %d, Body: %s", text, statusCode, truncateBody()));
        return this;
    }
     */

    private String truncateBody() {
        if (body.length() <= 500) {
            return body;
        }
        return body.substring(0, 500) + "... (truncated)";
    }

    @Override
    public String toString() {
        return String.format("Response{statusCode=%d, body=%s}", statusCode, truncateBody());
    }
}

