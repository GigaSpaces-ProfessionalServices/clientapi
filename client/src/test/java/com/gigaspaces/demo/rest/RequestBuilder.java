package com.gigaspaces.demo.rest;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Fluent builder for HTTP requests with retry and polling support.
 */
public class RequestBuilder {

    public enum Method {GET, POST, PUT, DELETE}

    private final String url;
    private final Method method;

    // Request configuration
    private String jsonBody;
    private File multipartFile;
    private String multipartFieldName;
    private String multipartFileName;
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 10000;

    // Retry configuration
    private boolean retryOnFailure = false;
    private int[] retryUntilStatuses = null;
    private int maxAttempts = 1;
    private int delayMs = 1000;

    RequestBuilder(String url, Method method) {
        this.url = url;
        this.method = method;
    }

    /**
     * Set JSON body for POST/PUT requests.
     */
    public RequestBuilder jsonBody(String json) {
        this.jsonBody = json;
        return this;
    }

    /**
     * Set multipart file for PUT requests.
     */
    public RequestBuilder multipartFile(String fieldName, File file, String fileName) {
        this.multipartFieldName = fieldName;
        this.multipartFile = file;
        this.multipartFileName = fileName;
        return this;
    }

    /**
     * Set connection and read timeout in milliseconds.
     * not used
    public RequestBuilder timeout(int timeoutMs) {
        this.connectTimeoutMs = timeoutMs;
        this.readTimeoutMs = timeoutMs;
        return this;
    }
     */

    /**
     * Set separate connect and read timeouts.
     */
    public RequestBuilder timeout(int connectTimeoutMs, int readTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        return this;
    }

    /**
     * Retry on connection failure or non-2xx status.
     */
    public RequestBuilder retryOnFailure() {
        this.retryOnFailure = true;
        return this;
    }

    /**
     * Retry until the specified status code is received.
     */
    public RequestBuilder retryUntilStatus(int statusCode) {
        this.retryUntilStatuses = new int[]{statusCode};
        return this;
    }

    /**
     * Retry until one of the specified status codes is received.
     * not used
    public RequestBuilder retryUntilStatusIn(int... statusCodes) {
        this.retryUntilStatuses = statusCodes;
        return this;
    }
     */

    /**
     * Maximum number of retry attempts.
     */
    public RequestBuilder maxAttempts(int attempts) {
        this.maxAttempts = attempts;
        return this;
    }

    /**
     * Delay between retry attempts in milliseconds.
     */
    public RequestBuilder delayMs(int delayMs) {
        this.delayMs = delayMs;
        return this;
    }

    /**
     * Execute the HTTP request.
     */
    public Response execute() {
        Response lastResponse = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                lastResponse = executeOnce();

                // Check if we got the status we're waiting for
                if (retryUntilStatuses != null) {
                    if (isStatusIn(lastResponse.statusCode(), retryUntilStatuses)) {
                        return lastResponse;
                    }
                    // Continue retrying
                } else if (retryOnFailure) {
                    if (lastResponse.isSuccess()) {
                        return lastResponse;
                    }
                    // Continue retrying
                } else {
                    // No retry configured, return immediately
                    return lastResponse;
                }
            } catch (Exception e) {
                lastException = e;
                if (!retryOnFailure && retryUntilStatuses == null) {
                    throw new RuntimeException("Request failed: " + url, e);
                }
            }

            // Wait before next attempt (unless this was the last attempt)
            if (attempt < maxAttempts) {
                sleep(delayMs);
            }
        }

        // All retries exhausted
        if (lastResponse != null) {
            return lastResponse;
        }
        throw new RuntimeException(
                String.format("Request failed after %d attempts: %s", maxAttempts, url), lastException);
    }

    private boolean isStatusIn(int status, int[] statuses) {
        return Arrays.stream(statuses).anyMatch(s -> s == status);
    }

    private Response executeOnce() throws Exception {
        if (multipartFile != null) {
            return executeMultipart();
        } else if (jsonBody != null) {
            return executeWithJsonBody();
        } else {
            return executeSimple();
        }
    }

    private Response executeSimple() throws Exception {
        HttpURLConnection connection = openConnection();
        connection.setRequestMethod(method.name());
        connection.setRequestProperty("Accept", "*/*");

        int statusCode = connection.getResponseCode();
        String body = readResponseBody(connection);
        connection.disconnect();

        return new Response(statusCode, body);
    }

    private Response executeWithJsonBody() throws Exception {
        HttpURLConnection connection = openConnection();
        connection.setRequestMethod(method.name());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setDoOutput(true);

        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            out.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        int statusCode = connection.getResponseCode();
        String body = readResponseBody(connection);
        connection.disconnect();

        return new Response(statusCode, body);
    }

    private Response executeMultipart() throws Exception {
        String boundary = "----FormBoundary" + System.currentTimeMillis();
        String lineEnd = "\r\n";

        HttpURLConnection connection = openConnection();
        connection.setRequestMethod(method.name());
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Accept", "text/plain");

        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            // Write file part
            out.writeBytes("--" + boundary + lineEnd);
            out.writeBytes("Content-Disposition: form-data; name=\"" + multipartFieldName +
                    "\"; filename=\"" + multipartFileName + "\"" + lineEnd);
            out.writeBytes("Content-Type: application/octet-stream" + lineEnd);
            out.writeBytes(lineEnd);

            // Write file content
            byte[] fileBytes = Files.readAllBytes(multipartFile.toPath());
            out.write(fileBytes);
            out.writeBytes(lineEnd);

            // Write closing boundary
            out.writeBytes("--" + boundary + "--" + lineEnd);
            out.flush();
        }

        int statusCode = connection.getResponseCode();
        String body = readResponseBody(connection);
        connection.disconnect();

        return new Response(statusCode, body);
    }

    private HttpURLConnection openConnection() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);
        return connection;
    }

    private String readResponseBody(HttpURLConnection connection) {
        try {
            InputStream stream;
            if (connection.getResponseCode() >= 400) {
                stream = connection.getErrorStream();
            } else {
                stream = connection.getInputStream();
            }
            if (stream == null) {
                return "";
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            return "";
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during retry delay", e);
        }
    }
}

