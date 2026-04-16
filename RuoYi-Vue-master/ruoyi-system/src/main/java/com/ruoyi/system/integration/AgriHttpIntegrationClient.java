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
import java.net.URI;
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

    private String nullSafe(Object value)
    {
        return value == null ? "" : String.valueOf(value);
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
