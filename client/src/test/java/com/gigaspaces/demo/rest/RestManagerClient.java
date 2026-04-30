package com.gigaspaces.demo.rest;


/**
 * Fluent REST client for test HTTP operations.
 *
 * <p>Usage examples:</p>
 * <pre>
 * // Simple GET
 * Response response = RestManagerClient.get(url).execute();
 *
 * // GET with retry until status 200
 * Response response = RestManagerClient.get(url)
 *     .retryUntilStatus(200)
 *     .maxAttempts(60)
 *     .delayMs(2000)
 *     .execute();
 *
 * // POST JSON with retry on failure
 * Response response = RestManagerClient.post(url)
 *     .jsonBody(json)
 *     .retryOnFailure()
 *     .maxAttempts(30)
 *     .delayMs(2000)
 *     .execute();
 *
 * // PUT multipart file upload
 * Response response = RestManagerClient.put(url)
 *     .multipartFile("file", jarFile, "demo-pu.jar")
 *     .timeout(120000)
 *     .execute();
 *
 * // DELETE with retry
 * Response response = RestManagerClient.delete(url)
 *     .retryOnFailure()
 *     .maxAttempts(10)
 *     .delayMs(2000)
 *     .execute();
 *
 * // Wait for resource to disappear (404)
 * Response response = RestManagerClient.get(url)
 *     .retryUntilStatus(404)
 *     .maxAttempts(15)
 *     .delayMs(20000)
 *     .execute()
 *     .assertStatus(404);
 * </pre>
 */
public class RestManagerClient {

    private RestManagerClient() {
        // Static factory methods only
    }

    /**
     * Start building a GET request.
     */
    public static RequestBuilder get(String url) {
        return new RequestBuilder(url, RequestBuilder.Method.GET);
    }

    /**
     * Start building a POST request.
     */
    public static RequestBuilder post(String url) {
        return new RequestBuilder(url, RequestBuilder.Method.POST);
    }

    /**
     * Start building a PUT request.
     */
    public static RequestBuilder put(String url) {
        return new RequestBuilder(url, RequestBuilder.Method.PUT);
    }

    /**
     * Start building a DELETE request.
     */
    public static RequestBuilder delete(String url) {
        return new RequestBuilder(url, RequestBuilder.Method.DELETE);
    }
}
