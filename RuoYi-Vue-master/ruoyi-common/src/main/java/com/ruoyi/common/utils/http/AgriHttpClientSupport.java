package com.ruoyi.common.utils.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Map;

/**
 * Agri 场景外部 HTTP 调用支持类。
 */
public final class AgriHttpClientSupport
{
    private AgriHttpClientSupport()
    {
    }

    public static HttpResponse<String> sendGet(String url, String param, Charset responseCharset, Duration connectTimeout,
        Duration readTimeout, Map<String, String> headers) throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl(url, param)))
            .timeout(readTimeout)
            .GET();
        applyHeaders(requestBuilder, headers);
        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(responseCharset));
    }

    public static HttpResponse<String> sendJsonPost(String url, String body, Charset responseCharset, Duration connectTimeout,
        Duration readTimeout, Map<String, String> headers) throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(readTimeout)
            .POST(HttpRequest.BodyPublishers.ofString(body, responseCharset));
        applyHeaders(requestBuilder, headers);
        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(responseCharset));
    }

    private static String buildUrl(String url, String param)
    {
        return param == null || param.isBlank() ? url : url + "?" + param;
    }

    private static void applyHeaders(HttpRequest.Builder requestBuilder, Map<String, String> headers)
    {
        if (headers == null)
        {
            return;
        }
        headers.forEach(requestBuilder::header);
    }
}