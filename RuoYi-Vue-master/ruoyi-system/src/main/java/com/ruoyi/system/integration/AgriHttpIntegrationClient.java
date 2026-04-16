package com.ruoyi.system.integration;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.system.domain.AgriMarketForecast;
import com.ruoyi.system.domain.AgriPestIdentifyTask;
import com.ruoyi.system.domain.AgriQualityInspectTask;
import com.ruoyi.system.domain.AgriYieldForecastTask;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 统一外部HTTP接入客户端。
 */
@Component
public class AgriHttpIntegrationClient
{
    private static final String DEFAULT_MODEL = "deepseek-chat";

    private final AgriIntegrationProperties properties;

    public AgriHttpIntegrationClient(AgriIntegrationProperties properties)
    {
        this.properties = properties;
    }

    public PestResult invokePest(AgriPestIdentifyTask task) throws IOException, InterruptedException
    {
        JSONObject data = callDeepSeek(properties.getAi().getPestPath(), buildPestPrompt(task));

        PestResult result = new PestResult();
        result.setIdentifyResult(readString(data, "identifyResult", "result", "label"));
        result.setModelVersion(readString(data, "modelVersion", "version", "model"));
        BigDecimal confidence = readDecimal(data, "confidence", "score");
        result.setConfidence(confidence == null ? BigDecimal.ZERO : confidence);
        result.setSuccess(true);
        return result;
    }

    public YieldResult invokeYield(AgriYieldForecastTask task) throws IOException, InterruptedException
    {
        JSONObject data = callDeepSeek(properties.getAi().getYieldPath(), buildYieldPrompt(task));

        YieldResult result = new YieldResult();
        result.setForecastYieldKg(readDecimal(data, "forecastYieldKg", "predictedYield", "yieldKg"));
        result.setModelVersion(readString(data, "modelVersion", "version", "model"));
        result.setSuccess(true);
        return result;
    }

    public QualityResult invokeQuality(AgriQualityInspectTask task) throws IOException, InterruptedException
    {
        JSONObject data = callDeepSeek(properties.getAi().getQualityPath(), buildQualityPrompt(task));

        QualityResult result = new QualityResult();
        result.setInspectResult(readString(data, "inspectResult", "result", "conclusion"));
        result.setQualityGrade(readString(data, "qualityGrade", "grade"));
        result.setDefectRate(readDecimal(data, "defectRate", "defectRatio"));
        result.setModelVersion(readString(data, "modelVersion", "version", "model"));
        result.setSuccess(true);
        return result;
    }

    public MarketForecastResult invokeMarketForecast(AgriMarketForecast task) throws IOException, InterruptedException
    {
        JSONObject data = callDeepSeek(properties.getAi().getMarketPath(), buildMarketForecastPrompt(task));

        MarketForecastResult result = new MarketForecastResult();
        result.setForecastSalesKg(readDecimal(data, "forecastSalesKg", "predictedSales", "salesKg"));
        result.setForecastPrice(readDecimal(data, "forecastPrice", "predictedPrice", "price"));
        result.setConfidenceRate(readDecimal(data, "confidenceRate", "confidence", "score"));
        result.setModelVersion(readString(data, "modelVersion", "version", "model"));
        result.setForecastSummary(readString(data, "forecastSummary", "summary", "result", "analysis"));
        result.setSuccess(true);
        return result;
    }

    public OutputSalesInsightResult invokeOutputSalesInsight(String contextJson) throws IOException, InterruptedException
    {
        JSONObject data = callDeepSeek(properties.getAi().getOutputSalesPath(), buildOutputSalesPrompt(contextJson));

        OutputSalesInsightResult result = new OutputSalesInsightResult();
        result.setInsightSummary(readString(data, "insightSummary", "summary", "analysis", "result"));
        result.setRiskLevel(readString(data, "riskLevel", "risk"));
        result.setSuggestion(readString(data, "suggestion", "advice", "action"));
        result.setModelVersion(readString(data, "modelVersion", "version", "model"));
        BigDecimal confidenceRate = readDecimal(data, "confidenceRate", "confidence", "score");
        result.setConfidenceRate(confidenceRate == null ? BigDecimal.ZERO : confidenceRate);
        result.setSuccess(true);
        return result;
    }

    public ThirdApiResult probe(String endpointUrl, Integer timeoutSec) throws IOException, InterruptedException
    {
        int timeout = timeoutSec == null || timeoutSec <= 0 ? 5 : timeoutSec;
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeout))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpointUrl))
            .timeout(Duration.ofSeconds(timeout))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ThirdApiResult result = new ThirdApiResult();
        result.setHttpStatus(response.statusCode());
        result.setSuccess(response.statusCode() >= 200 && response.statusCode() < 300);
        String body = response.body() == null ? "" : response.body();
        if (body.length() > 300)
        {
            body = body.substring(0, 300);
        }
        result.setResponseSnippet(body);
        return result;
    }

    public WeatherProbeResult probeWeather(String endpointUrl, Integer timeoutSec) throws IOException, InterruptedException
    {
        int timeout = timeoutSec == null || timeoutSec <= 0 ? 8 : timeoutSec;
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeout))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpointUrl))
            .timeout(Duration.ofSeconds(timeout))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        WeatherProbeResult result = new WeatherProbeResult();
        result.setHttpStatus(response.statusCode());
        result.setSuccess(response.statusCode() >= 200 && response.statusCode() < 300);
        String body = response.body() == null ? "" : response.body();
        result.setResponseSnippet(trimToLength(body, 400));
        if (result.isSuccess() && StringUtils.isNotBlank(body))
        {
            result.setWeatherSummary(buildWeatherSummary(body));
        }
        return result;
    }

    public MapProbeResult probeMap(String endpointUrl, Integer timeoutSec) throws IOException, InterruptedException
    {
        AgriIntegrationProperties.Map map = properties.getMap();
        if (!map.isEnabled() && StringUtils.isBlank(map.getBaseUrl()))
        {
            throw new IllegalStateException("地图接入已禁用，请在配置中启用 agri.integration.map.enabled");
        }
        if (StringUtils.isBlank(map.getBaseUrl()))
        {
            throw new IllegalStateException("地图接入缺少 baseUrl 配置");
        }
        if (StringUtils.isBlank(map.getApiKey()))
        {
            throw new IllegalStateException("地图接入缺少 apiKey 配置，请先配置 AGRI_MAP_API_KEY 并重启后端");
        }

        int timeout = timeoutSec == null || timeoutSec <= 0 ? 8 : timeoutSec;
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeout))
            .build();

        String requestUrl = buildMapRequestUrl(endpointUrl);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .timeout(Duration.ofSeconds(timeout))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body = response.body() == null ? "" : response.body();
        MapProbeResult result = new MapProbeResult();
        result.setHttpStatus(response.statusCode());
        result.setSuccess(response.statusCode() >= 200 && response.statusCode() < 300);
        result.setResponseSnippet(trimToLength(body, 400));
        if (result.isSuccess() && StringUtils.isNotBlank(body))
        {
            JSONObject root = JSON.parseObject(body);
            result.setMapSummary(buildMapSummary(root));
            String status = readString(root, "status");
            if (!"1".equals(status))
            {
                result.setSuccess(false);
                String info = firstNonBlank(readString(root, "info"), readString(root, "infocode"));
                if (StringUtils.isNotBlank(info))
                {
                    result.setMapSummary("地图接口返回异常: " + info);
                }
            }
        }
        return result;
    }

    private JSONObject callDeepSeek(String path, String prompt) throws IOException, InterruptedException
    {
        AgriIntegrationProperties.Ai ai = properties.getAi();
        if (!ai.isEnabled() && StringUtils.isBlank(ai.getBaseUrl()))
        {
            throw new IllegalStateException("AI接入已禁用，请在配置中启用agri.integration.ai.enabled");
        }
        if (StringUtils.isBlank(ai.getBaseUrl()))
        {
            throw new IllegalStateException("AI接入缺少baseUrl配置");
        }

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(Math.max(ai.getConnectTimeoutMs(), 1000)))
            .build();

        String url = ai.getBaseUrl();
        if (StringUtils.isNotBlank(path))
        {
            if (url.endsWith("/") && path.startsWith("/"))
            {
                url = url.substring(0, url.length() - 1);
            }
            else if (!url.endsWith("/") && !path.startsWith("/"))
            {
                url = url + "/";
            }
            url = url + path;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", StringUtils.defaultIfBlank(ai.getModel(), DEFAULT_MODEL));
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(buildMessage("system", buildSystemPrompt()));
        messages.add(buildMessage("user", prompt));
        payload.put("messages", messages);
        payload.put("temperature", 0.2);
        payload.put("stream", false);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(Math.max(ai.getReadTimeoutMs(), 2000)))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");

        if (StringUtils.isNotBlank(ai.getApiKey()))
        {
            requestBuilder.header("Authorization", "Bearer " + ai.getApiKey());
        }
        else
        {
            throw new IllegalStateException("DeepSeek API Key 为空，请先配置 AGRI_AI_API_KEY 并重启后端");
        }

        HttpRequest request = requestBuilder
            .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(payload), StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300)
        {
            if (response.statusCode() == 401)
            {
                throw new IllegalStateException("DeepSeek鉴权失败(401)，请确认 AGRI_AI_API_KEY 是 DeepSeek 控制台申请的有效Key，并且后端已重启加载最新配置。响应内容: " + trimToLength(response.body(), 300));
            }
            throw new IllegalStateException("DeepSeek服务返回失败状态: " + response.statusCode() + "，响应内容: " + trimToLength(response.body(), 500));
        }
        if (StringUtils.isBlank(response.body()))
        {
            throw new IllegalStateException("DeepSeek服务返回空响应");
        }
        JSONObject responseJson = JSON.parseObject(response.body());
        String content = extractMessageContent(responseJson);
        return parseJsonObject(content);
    }

    private Map<String, String> buildMessage(String role, String content)
    {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String buildSystemPrompt()
    {
        return "你是农产品AI接入服务，请严格按用户要求返回JSON对象，不要输出markdown、解释性文字或代码块。";
    }

    private String buildPestPrompt(AgriPestIdentifyTask task)
    {
        return "请根据以下病虫害识别任务返回JSON对象，格式必须严格为：{\"identifyResult\":\"...\",\"confidence\":0.0,\"modelVersion\":\"...\"}。" +
            "任务信息：taskId=" + task.getTaskId() +
            ", plotCode=" + nullSafe(task.getPlotCode()) +
            ", cropName=" + nullSafe(task.getCropName()) +
            ", imageUrl=" + nullSafe(task.getImageUrl()) +
            "。identifyResult 请给出简洁中文结论，confidence 为0到1之间的小数。";
    }

    private String buildYieldPrompt(AgriYieldForecastTask task)
    {
        return "请根据以下产量预测任务返回JSON对象，格式必须严格为：{\"forecastYieldKg\":0.0,\"modelVersion\":\"...\"}。" +
            "任务信息：forecastId=" + task.getForecastId() +
            ", plotCode=" + nullSafe(task.getPlotCode()) +
            ", cropName=" + nullSafe(task.getCropName()) +
            ", season=" + nullSafe(task.getSeason()) +
            ", areaMu=" + nullSafe(task.getAreaMu()) +
            ", sowDate=" + nullSafe(task.getSowDate()) +
            "。forecastYieldKg 为预测产量，单位千克，modelVersion 给出模型版本标识。";
    }

    private String buildQualityPrompt(AgriQualityInspectTask task)
    {
        return "请根据以下视觉质检任务返回JSON对象，格式必须严格为：{\"inspectResult\":\"...\",\"qualityGrade\":\"...\",\"defectRate\":0.0,\"modelVersion\":\"...\"}。" +
            "任务信息：inspectId=" + task.getInspectId() +
            ", processBatchNo=" + nullSafe(task.getProcessBatchNo()) +
            ", sampleCode=" + nullSafe(task.getSampleCode()) +
            ", imageUrl=" + nullSafe(task.getImageUrl()) +
            "。inspectResult 请给出简洁中文结论，qualityGrade 为等级，defectRate 为0到1之间的小数。";
    }

    private String buildMarketForecastPrompt(AgriMarketForecast task)
    {
        return "请根据以下市场预测分析任务返回JSON对象，格式必须严格为：{\"forecastSalesKg\":0.0,\"forecastPrice\":0.0,\"confidenceRate\":0.0,\"modelVersion\":\"...\",\"forecastSummary\":\"...\"}。" +
            "任务信息：forecastId=" + task.getForecastId() +
            ", marketArea=" + nullSafe(task.getMarketArea()) +
            ", productCode=" + nullSafe(task.getProductCode()) +
            ", productName=" + nullSafe(task.getProductName()) +
            ", periodStart=" + nullSafe(task.getPeriodStart()) +
            ", periodEnd=" + nullSafe(task.getPeriodEnd()) +
            ", historicalSalesKg=" + nullSafe(task.getHistoricalSalesKg()) +
            "。forecastSummary 请用中文给出完整文字分析结论，说明预测销量、预测价格、置信度及原因，内容尽量完整。";
    }

    private String buildOutputSalesPrompt(String contextJson)
    {
        String safeContext = StringUtils.defaultIfBlank(contextJson, "{}");
        return "你将收到农业系统中的产销历史、地块、病虫害和农事数据，请输出趋势预测洞察。"
            + "请严格返回JSON对象，格式必须为："
            + "{\"insightSummary\":\"...\",\"riskLevel\":\"低|中|高\",\"suggestion\":\"...\",\"confidenceRate\":0.0,\"modelVersion\":\"...\"}。"
            + "其中 insightSummary 用中文总结未来趋势与关键驱动因子，suggestion 给出2-3条可执行建议，confidenceRate 取值0到1。"
            + "输入数据如下：" + safeContext;
    }

    private JSONObject parseJsonObject(String content)
    {
        if (StringUtils.isBlank(content))
        {
            throw new IllegalStateException("DeepSeek服务返回内容为空");
        }

        String normalized = normalizeJsonText(content);
        try
        {
            return JSON.parseObject(normalized);
        }
        catch (Exception ex)
        {
            int start = normalized.indexOf('{');
            int end = normalized.lastIndexOf('}');
            if (start >= 0 && end > start)
            {
                return JSON.parseObject(normalized.substring(start, end + 1));
            }
            throw new IllegalStateException("DeepSeek服务返回的内容不是有效JSON: " + trimToLength(normalized, 500), ex);
        }
    }

    private String extractMessageContent(JSONObject response)
    {
        if (response == null)
        {
            throw new IllegalStateException("DeepSeek服务返回空响应对象");
        }

        JSONArray choices = response.getJSONArray("choices");
        if (choices != null && !choices.isEmpty())
        {
            JSONObject firstChoice = choices.getJSONObject(0);
            if (firstChoice != null)
            {
                JSONObject message = firstChoice.getJSONObject("message");
                if (message != null)
                {
                    String content = message.getString("content");
                    if (StringUtils.isNotBlank(content))
                    {
                        return content;
                    }
                }
                String text = firstChoice.getString("text");
                if (StringUtils.isNotBlank(text))
                {
                    return text;
                }
            }
        }

        String directContent = response.getString("content");
        if (StringUtils.isNotBlank(directContent))
        {
            return directContent;
        }
        throw new IllegalStateException("DeepSeek服务响应中未找到消息内容: " + trimToLength(response.toJSONString(), 500));
    }

    private String normalizeJsonText(String content)
    {
        String normalized = content.trim();
        if (normalized.startsWith("```"))
        {
            int firstLineBreak = normalized.indexOf('\n');
            if (firstLineBreak >= 0)
            {
                normalized = normalized.substring(firstLineBreak + 1).trim();
            }
            if (normalized.endsWith("```"))
            {
                normalized = normalized.substring(0, normalized.length() - 3).trim();
            }
        }
        return normalized;
    }

    private String trimToLength(String value, int maxLength)
    {
        if (value == null)
        {
            return null;
        }
        if (value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String buildWeatherSummary(String body)
    {
        try
        {
            JSONObject root = JSON.parseObject(body);
            if (root == null)
            {
                return null;
            }

            JSONObject now = firstObject(root, "now", "current", "realtime", "weather", "data");
            JSONArray daily = root.getJSONArray("daily");
            JSONObject firstDaily = daily == null || daily.isEmpty() ? null : daily.getJSONObject(0);
            JSONArray hourly = root.getJSONArray("hourly");
            JSONObject firstHourly = hourly == null || hourly.isEmpty() ? null : hourly.getJSONObject(0);

            String text = firstNonBlank(
                readString(now, "text", "weatherText", "condition", "description"),
                readString(firstDaily, "textDay", "text", "weatherText", "conditionDay"),
                readString(firstHourly, "text", "weatherText", "condition")
            );
            BigDecimal temperature = firstDecimal(
                readDecimal(now, "temp", "temperature", "feelsLike"),
                readDecimal(firstDaily, "tempMax", "tempMin", "temperature"),
                readDecimal(firstHourly, "temp", "temperature")
            );
            BigDecimal humidity = firstDecimal(
                readDecimal(now, "humidity"),
                readDecimal(firstDaily, "humidity")
            );
            BigDecimal precipitation = firstDecimal(
                readDecimal(now, "precip", "rain", "precipitation"),
                readDecimal(firstDaily, "precip", "rain", "precipitation")
            );
            BigDecimal rainProbability = firstDecimal(
                readDecimal(firstDaily, "precipProbability", "pop", "rainProbability"),
                readDecimal(firstHourly, "precipProbability", "pop", "rainProbability")
            );
            String forecastTime = firstNonBlank(
                readString(now, "obsTime", "fxTime", "updateTime"),
                readString(firstDaily, "fxDate", "date"),
                readString(firstHourly, "fxTime", "dateTime")
            );

            List<String> parts = new ArrayList<>();
            if (StringUtils.isNotBlank(text))
            {
                parts.add("天气=" + text);
            }
            if (temperature != null)
            {
                parts.add("温度=" + temperature + "℃");
            }
            if (humidity != null)
            {
                parts.add("湿度=" + humidity + "%");
            }
            if (precipitation != null)
            {
                parts.add("降水=" + precipitation);
            }
            if (rainProbability != null)
            {
                parts.add("降雨概率=" + rainProbability + "%");
            }
            if (StringUtils.isNotBlank(forecastTime))
            {
                parts.add("时间=" + forecastTime);
            }
            return String.join(" | ", parts);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private String buildMapRequestUrl(String endpointUrl)
    {
        AgriIntegrationProperties.Map map = properties.getMap();
        String baseUrl = StringUtils.defaultIfBlank(endpointUrl, map.getBaseUrl() + map.getDrivingPath());
        String location = StringUtils.defaultIfBlank(map.getLocation(), "118.7853,31.9998");
        String[] parts = StringUtils.split(location, ',');
        if (parts == null || parts.length != 2)
        {
            throw new IllegalStateException("地图 location 配置格式不正确，请使用 lng,lat 形式");
        }

        BigDecimal longitude = new BigDecimal(parts[0].trim());
        BigDecimal latitude = new BigDecimal(parts[1].trim());
        String origin = longitude.setScale(6, RoundingMode.HALF_UP) + "," + latitude.setScale(6, RoundingMode.HALF_UP);
        String destination = longitude.add(new BigDecimal("0.01")).setScale(6, RoundingMode.HALF_UP)
            + "," + latitude.add(new BigDecimal("0.01")).setScale(6, RoundingMode.HALF_UP);

        String requestUrl = appendQueryParam(baseUrl, "key", map.getApiKey());
        requestUrl = appendQueryParam(requestUrl, "origin", origin);
        requestUrl = appendQueryParam(requestUrl, "destination", destination);
        requestUrl = appendQueryParam(requestUrl, "extensions", "base");
        return requestUrl;
    }

    private String buildMapSummary(JSONObject root)
    {
        if (root == null)
        {
            return null;
        }

        JSONObject route = root.getJSONObject("route");
        JSONObject firstPath = null;
        JSONArray paths = route == null ? null : route.getJSONArray("paths");
        if (paths != null && !paths.isEmpty())
        {
            firstPath = paths.getJSONObject(0);
        }

        BigDecimal distance = firstDecimal(
            readDecimal(route, "distance"),
            readDecimal(firstPath, "distance")
        );
        BigDecimal duration = firstDecimal(
            readDecimal(route, "duration"),
            readDecimal(firstPath, "duration")
        );
        String info = firstNonBlank(readString(root, "info"), readString(root, "infocode"));

        List<String> parts = new ArrayList<>();
        if (distance != null)
        {
            parts.add("路线距离=" + formatDistance(distance));
        }
        if (duration != null)
        {
            parts.add("预计时长=" + formatDuration(duration));
        }
        if (StringUtils.isNotBlank(info))
        {
            parts.add("接口信息=" + info);
        }
        return parts.isEmpty() ? null : String.join(" | ", parts);
    }

    private String appendQueryParam(String url, String key, String value)
    {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + key + "=" + URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8);
    }

    private String formatDistance(BigDecimal distance)
    {
        if (distance == null)
        {
            return null;
        }
        if (distance.compareTo(new BigDecimal("1000")) >= 0)
        {
            return distance.divide(new BigDecimal("1000"), 1, RoundingMode.HALF_UP) + "km";
        }
        return distance.setScale(0, RoundingMode.HALF_UP) + "m";
    }

    private String formatDuration(BigDecimal duration)
    {
        if (duration == null)
        {
            return null;
        }
        if (duration.compareTo(new BigDecimal("60")) >= 0)
        {
            return duration.divide(new BigDecimal("60"), 1, RoundingMode.HALF_UP) + "min";
        }
        return duration.setScale(0, RoundingMode.HALF_UP) + "s";
    }

    private JSONObject firstObject(JSONObject root, String... keys)
    {
        if (root == null || keys == null)
        {
            return null;
        }
        for (String key : keys)
        {
            JSONObject value = root.getJSONObject(key);
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    private String nullSafe(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNonBlank(String... values)
    {
        if (values == null)
        {
            return null;
        }
        for (String value : values)
        {
            if (StringUtils.isNotBlank(value))
            {
                return value;
            }
        }
        return null;
    }

    private BigDecimal firstDecimal(BigDecimal... values)
    {
        if (values == null)
        {
            return null;
        }
        for (BigDecimal value : values)
        {
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    private String readString(JSONObject json, String... keys)
    {
        for (String key : keys)
        {
            String value = json.getString(key);
            if (StringUtils.isNotBlank(value))
            {
                return value;
            }
        }
        return null;
    }

    private BigDecimal readDecimal(JSONObject json, String... keys)
    {
        for (String key : keys)
        {
            Object value = json.get(key);
            if (value == null)
            {
                continue;
            }
            if (value instanceof BigDecimal bigDecimal)
            {
                return bigDecimal;
            }
            try
            {
                return new BigDecimal(String.valueOf(value));
            }
            catch (NumberFormatException ignore)
            {
                // try next key
            }
        }
        return null;
    }

    public static class PestResult
    {
        private boolean success;

        private String identifyResult;

        private BigDecimal confidence;

        private String modelVersion;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public String getIdentifyResult()
        {
            return identifyResult;
        }

        public void setIdentifyResult(String identifyResult)
        {
            this.identifyResult = identifyResult;
        }

        public BigDecimal getConfidence()
        {
            return confidence;
        }

        public void setConfidence(BigDecimal confidence)
        {
            this.confidence = confidence;
        }

        public String getModelVersion()
        {
            return modelVersion;
        }

        public void setModelVersion(String modelVersion)
        {
            this.modelVersion = modelVersion;
        }
    }

    public static class YieldResult
    {
        private boolean success;

        private BigDecimal forecastYieldKg;

        private String modelVersion;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public BigDecimal getForecastYieldKg()
        {
            return forecastYieldKg;
        }

        public void setForecastYieldKg(BigDecimal forecastYieldKg)
        {
            this.forecastYieldKg = forecastYieldKg;
        }

        public String getModelVersion()
        {
            return modelVersion;
        }

        public void setModelVersion(String modelVersion)
        {
            this.modelVersion = modelVersion;
        }
    }

    public static class QualityResult
    {
        private boolean success;

        private String qualityGrade;

        private BigDecimal defectRate;

        private String inspectResult;

        private String modelVersion;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public String getQualityGrade()
        {
            return qualityGrade;
        }

        public void setQualityGrade(String qualityGrade)
        {
            this.qualityGrade = qualityGrade;
        }

        public BigDecimal getDefectRate()
        {
            return defectRate;
        }

        public void setDefectRate(BigDecimal defectRate)
        {
            this.defectRate = defectRate;
        }

        public String getInspectResult()
        {
            return inspectResult;
        }

        public void setInspectResult(String inspectResult)
        {
            this.inspectResult = inspectResult;
        }

        public String getModelVersion()
        {
            return modelVersion;
        }

        public void setModelVersion(String modelVersion)
        {
            this.modelVersion = modelVersion;
        }
    }

    public static class MarketForecastResult
    {
        private boolean success;

        private BigDecimal forecastSalesKg;

        private BigDecimal forecastPrice;

        private BigDecimal confidenceRate;

        private String modelVersion;

        private String forecastSummary;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public BigDecimal getForecastSalesKg()
        {
            return forecastSalesKg;
        }

        public void setForecastSalesKg(BigDecimal forecastSalesKg)
        {
            this.forecastSalesKg = forecastSalesKg;
        }

        public BigDecimal getForecastPrice()
        {
            return forecastPrice;
        }

        public void setForecastPrice(BigDecimal forecastPrice)
        {
            this.forecastPrice = forecastPrice;
        }

        public BigDecimal getConfidenceRate()
        {
            return confidenceRate;
        }

        public void setConfidenceRate(BigDecimal confidenceRate)
        {
            this.confidenceRate = confidenceRate;
        }

        public String getModelVersion()
        {
            return modelVersion;
        }

        public void setModelVersion(String modelVersion)
        {
            this.modelVersion = modelVersion;
        }

        public String getForecastSummary()
        {
            return forecastSummary;
        }

        public void setForecastSummary(String forecastSummary)
        {
            this.forecastSummary = forecastSummary;
        }
    }

    public static class ThirdApiResult
    {
        private boolean success;

        private int httpStatus;

        private String responseSnippet;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public int getHttpStatus()
        {
            return httpStatus;
        }

        public void setHttpStatus(int httpStatus)
        {
            this.httpStatus = httpStatus;
        }

        public String getResponseSnippet()
        {
            return responseSnippet;
        }

        public void setResponseSnippet(String responseSnippet)
        {
            this.responseSnippet = responseSnippet;
        }
    }

    public static class WeatherProbeResult
    {
        private boolean success;

        private int httpStatus;

        private String responseSnippet;

        private String weatherSummary;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public int getHttpStatus()
        {
            return httpStatus;
        }

        public void setHttpStatus(int httpStatus)
        {
            this.httpStatus = httpStatus;
        }

        public String getResponseSnippet()
        {
            return responseSnippet;
        }

        public void setResponseSnippet(String responseSnippet)
        {
            this.responseSnippet = responseSnippet;
        }

        public String getWeatherSummary()
        {
            return weatherSummary;
        }

        public void setWeatherSummary(String weatherSummary)
        {
            this.weatherSummary = weatherSummary;
        }
    }

    public static class MapProbeResult
    {
        private boolean success;

        private int httpStatus;

        private String responseSnippet;

        private String mapSummary;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public int getHttpStatus()
        {
            return httpStatus;
        }

        public void setHttpStatus(int httpStatus)
        {
            this.httpStatus = httpStatus;
        }

        public String getResponseSnippet()
        {
            return responseSnippet;
        }

        public void setResponseSnippet(String responseSnippet)
        {
            this.responseSnippet = responseSnippet;
        }

        public String getMapSummary()
        {
            return mapSummary;
        }

        public void setMapSummary(String mapSummary)
        {
            this.mapSummary = mapSummary;
        }
    }

    public static class OutputSalesInsightResult
    {
        private boolean success;

        private String insightSummary;

        private String riskLevel;

        private String suggestion;

        private BigDecimal confidenceRate;

        private String modelVersion;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public String getInsightSummary()
        {
            return insightSummary;
        }

        public void setInsightSummary(String insightSummary)
        {
            this.insightSummary = insightSummary;
        }

        public String getRiskLevel()
        {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel)
        {
            this.riskLevel = riskLevel;
        }

        public String getSuggestion()
        {
            return suggestion;
        }

        public void setSuggestion(String suggestion)
        {
            this.suggestion = suggestion;
        }

        public BigDecimal getConfidenceRate()
        {
            return confidenceRate;
        }

        public void setConfidenceRate(BigDecimal confidenceRate)
        {
            this.confidenceRate = confidenceRate;
        }

        public String getModelVersion()
        {
            return modelVersion;
        }

        public void setModelVersion(String modelVersion)
        {
            this.modelVersion = modelVersion;
        }
    }
}
