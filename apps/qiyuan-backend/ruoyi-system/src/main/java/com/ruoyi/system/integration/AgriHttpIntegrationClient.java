package com.ruoyi.system.integration;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.system.domain.AgriMarketForecast;
import com.ruoyi.system.domain.AgriPestIdentifyTask;
import com.ruoyi.system.domain.AgriQualityInspectTask;
import com.ruoyi.system.domain.AgriYieldForecastTask;
import com.ruoyi.common.utils.http.AgriHttpClientSupport;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
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
        String rawContent = callDeepSeekContent(properties.getAi().getPestPath(), buildPestPrompt(task));
        JSONObject data = parseJsonObject(rawContent);

        PestResult result = new PestResult();
        result.setIdentifyResult(readString(data, "identifyResult", "result", "label"));
        result.setModelVersion(readString(data, "modelVersion", "version", "model"));
        BigDecimal confidence = readDecimal(data, "confidence", "score");
        result.setConfidence(confidence == null ? BigDecimal.ZERO : confidence);
        result.setRawContent(trimToLength(rawContent, 600));
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

    public YieldInsightResult invokeYieldInsight(String contextJson) throws IOException, InterruptedException
    {
        String rawContent = callDeepSeekContent(properties.getAi().getYieldPath(), buildYieldInsightPrompt(contextJson));
        JSONObject data = parseJsonObject(rawContent);

        YieldInsightResult result = new YieldInsightResult();
        result.setForecastYieldKg(readDecimal(data, "forecastYieldKg", "predictedYield", "yieldKg"));
        result.setInsightSummary(readString(data, "insightSummary", "summary", "analysis", "result"));
        result.setSuggestion(readString(data, "suggestion", "advice", "action"));
        result.setRiskLevel(readString(data, "riskLevel", "risk"));
        result.setModelVersion(readString(data, "modelVersion", "version", "model"));
        result.setRawContent(trimToLength(rawContent, 600));
        BigDecimal confidenceRate = readDecimal(data, "confidenceRate", "confidence", "score");
        result.setConfidenceRate(confidenceRate == null ? BigDecimal.ZERO : confidenceRate);
        result.setSuccess(true);
        return result;
    }

    public QualityResult invokeQuality(AgriQualityInspectTask task) throws IOException, InterruptedException
    {
        String rawContent = callDeepSeekContent(properties.getAi().getQualityPath(), buildQualityPrompt(task));
        JSONObject data = parseJsonObject(rawContent);

        QualityResult result = new QualityResult();
        result.setInspectResult(readString(data, "inspectResult", "result", "conclusion"));
        result.setQualityGrade(readString(data, "qualityGrade", "grade"));
        result.setDefectRate(readDecimal(data, "defectRate", "defectRatio"));
        result.setModelVersion(readString(data, "modelVersion", "version", "model"));
        result.setRawContent(trimToLength(rawContent, 600));
        result.setSuccess(true);
        return result;
    }

    public GeneralInsightResult invokeGeneralInsight(String scene, String contextJson) throws IOException, InterruptedException
    {
        String rawContent = callDeepSeekContent(properties.getAi().getMarketPath(), buildGeneralInsightPrompt(scene, contextJson));
        JSONObject data;
        try
        {
            data = parseJsonObject(rawContent);
        }
        catch (Exception ex)
        {
            data = new JSONObject();
        }

        GeneralInsightResult result = new GeneralInsightResult();
        result.setInsightSummary(firstNonBlank(
            readString(data, "insightSummary", "summary", "analysis", "result"),
            trimToLength(rawContent, 220)
        ));
        result.setSuggestion(readString(data, "suggestion", "advice", "action"));
        result.setRiskLevel(firstNonBlank(readString(data, "riskLevel", "risk"), inferRiskLevelFromText(result.getInsightSummary())));
        BigDecimal confidenceRate = readDecimal(data, "confidenceRate", "confidence", "score");
        result.setConfidenceRate(confidenceRate == null ? BigDecimal.ZERO : confidenceRate);
        result.setModelVersion(firstNonBlank(readString(data, "modelVersion", "version", "model"), StringUtils.defaultIfBlank(properties.getAi().getModel(), DEFAULT_MODEL)));
        result.setRawContent(trimToLength(rawContent, 600));
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

    public CarbonFootprintInsightResult invokeCarbonFootprintInsight(String contextJson) throws IOException, InterruptedException
    {
        String rawContent = callDeepSeekContent(properties.getAi().getCarbonFootprintPath(), buildCarbonFootprintPrompt(contextJson));
        JSONObject data;
        try
        {
            data = parseJsonObject(rawContent);
        }
        catch (Exception ex)
        {
            data = new JSONObject();
        }

        CarbonFootprintInsightResult result = new CarbonFootprintInsightResult();
        result.setInsightSummary(firstNonBlank(
            readString(data, "insightSummary", "summary", "analysis", "result"),
            trimToLength(rawContent, 220)
        ));
        result.setRiskLevel(readString(data, "riskLevel", "risk"));
        result.setSuggestion(readString(data, "suggestion", "advice", "action"));
        result.setModelVersion(firstNonBlank(readString(data, "modelVersion", "version", "model"), StringUtils.defaultIfBlank(properties.getAi().getModel(), DEFAULT_MODEL)));
        BigDecimal confidenceRate = readDecimal(data, "confidenceRate", "confidence", "score");
        result.setConfidenceRate(confidenceRate == null ? BigDecimal.ZERO : confidenceRate);
        BigDecimal estimatedEmission = readDecimal(data, "estimatedEmission", "carbonEmission", "emission");
        result.setEstimatedEmission(estimatedEmission == null ? BigDecimal.ZERO : estimatedEmission);
        result.setRawContent(trimToLength(rawContent, 600));
        result.setSuccess(true);
        return result;
    }

    public ThirdApiResult probe(String endpointUrl, Integer timeoutSec) throws IOException, InterruptedException
    {
        int timeout = timeoutSec == null || timeoutSec <= 0 ? 5 : timeoutSec;
        HttpResponse<String> response = AgriHttpClientSupport.sendGet(endpointUrl, null, StandardCharsets.UTF_8,
            Duration.ofSeconds(timeout), Duration.ofSeconds(timeout), Map.of("Accept", "application/json"));
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
        HttpResponse<String> response = AgriHttpClientSupport.sendGet(endpointUrl, null, StandardCharsets.UTF_8,
            Duration.ofSeconds(timeout), Duration.ofSeconds(timeout), Map.of("Accept", "application/json"));
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
        String requestUrl = buildMapRequestUrl(endpointUrl);
        HttpResponse<String> response = AgriHttpClientSupport.sendGet(requestUrl, null, StandardCharsets.UTF_8,
            Duration.ofSeconds(timeout), Duration.ofSeconds(timeout), Map.of("Accept", "application/json"));
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
        return parseJsonObject(callDeepSeekContent(path, prompt));
    }

    private String callDeepSeekContent(String path, String prompt) throws IOException, InterruptedException
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

        String apiKey = resolveAiApiKey(ai);
        if (StringUtils.isBlank(apiKey))
        {
            throw new IllegalStateException("DeepSeek API Key 为空，请先配置 AGRI_AI_API_KEY 并重启后端");
        }

        HttpResponse<String> response = AgriHttpClientSupport.sendJsonPost(url, JSON.toJSONString(payload),
            StandardCharsets.UTF_8, Duration.ofMillis(Math.max(ai.getConnectTimeoutMs(), 1000)),
            Duration.ofMillis(Math.max(ai.getReadTimeoutMs(), 2000)),
            Map.of("Content-Type", "application/json", "Accept", "application/json", "Authorization", "Bearer " + apiKey));
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
        return extractMessageContent(responseJson);
    }

    private String resolveAiApiKey(AgriIntegrationProperties.Ai ai)
    {
        if (StringUtils.isNotBlank(ai.getApiKey()))
        {
            return ai.getApiKey().trim();
        }
        String envApiKey = System.getenv("AGRI_AI_API_KEY");
        if (StringUtils.isNotBlank(envApiKey))
        {
            return envApiKey.trim();
        }
        String sysPropApiKey = System.getProperty("AGRI_AI_API_KEY");
        if (StringUtils.isNotBlank(sysPropApiKey))
        {
            return sysPropApiKey.trim();
        }
        String springPropApiKey = System.getProperty("agri.integration.ai.api-key");
        if (StringUtils.isNotBlank(springPropApiKey))
        {
            return springPropApiKey.trim();
        }
        return null;
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

    private String buildModelFieldGuide()
    {
        return "可用字段字典（供趋势洞察、碳足迹与综合分析复用）："
            + "环境传感器 agri_env_sensor_record[deviceCode, plotCode, temperature, humidity, co2Value, status, dataSource, collectTime]；"
            + "农事记录 agri_farm_operation_record[plotCode, operationType, operationContent, operatorName, inputName, inputAmount, inputUnit, operationTime, status]；"
            + "病虫害识别 agri_pest_identify_task[plotCode, cropName, imageUrl, identifyStatus, identifyResult, confidence, identifyTime, modelVersion, status]；"
            + "产量预测 agri_yield_forecast_task[plotCode, cropName, season, sowDate, areaMu, forecastYieldKg, modelVersion, forecastStatus, forecastTime, status]；"
            + "市场预测 agri_market_forecast[marketArea, productCode, productName, periodStart, periodEnd, historicalSalesKg, forecastSalesKg, forecastPrice, confidenceRate, modelVersion, forecastStatus, forecastTime, status]；"
            + "设备状态 agri_device_status_monitor[deviceCode, deviceName, deviceType, onlineStatus, batteryLevel, signalStrength, temperature, humidity, lastReportTime, warningLevel, status]；"
            + "碳足迹模型 agri_carbon_footprint_model[modelCode, modelName, productType, boundaryScope, emissionFactor, carbonEmission, calcStatus, calcTime, verifier, status]；"
            + "产销趋势 agri_output_sales_trend[statDate, outputValue, salesValue, targetOutput, targetSales, outputMomRate, salesMomRate, status]；"
            + "质量检测 agri_quality_inspect_task[processBatchNo, sampleCode, imageUrl, inspectStatus, inspectResult, qualityGrade, defectRate, inspectTime, modelVersion, status]；"
            + "质量报告 agri_quality_report[reportNo, inspectId, processBatchNo, qualityGrade, defectRate, reportSummary, reportStatus, reportTime, status]；"
            + "物流轨迹 agri_logistics_track[traceCode, orderNo, productBatchNo, vehicleNo, driverName, startLocation, currentLocation, targetLocation, trackStatus, eventDesc, eventTime, temperature, humidity, longitude, latitude, status]；"
            + "物流温湿度 agri_logistics_temp_humidity[traceCode, orderNo, deviceCode, collectLocation, temperature, humidity, tempUpperLimit, tempLowerLimit, humidityUpperLimit, humidityLowerLimit, alertFlag, alertMessage, collectTime, status]；"
            + "物流预警 agri_logistics_warning[traceCode, orderNo, warningType, warningLevel, warningStatus, sourceRecordId, warningTitle, warningContent, warningTime, handler, handleTime, handleRemark, status]；"
            + "风控指标 agri_finance_risk_metric[indicatorCode, indicatorName, riskDimension, riskScore, thresholdValue, riskLevel, evaluateStatus, evaluateTime, evaluator, status]；"
            + "供应链合约 agri_supply_chain_contract[contractNo, contractName, financeSubject, financeAmount, interestRate, startDate, endDate, contractStatus, riskLevel, status]；"
            + "存证校验 agri_data_attestation_verify[attestationNo, batchNo, dataType, originHash, chainHash, verifyStatus, verifyTime, verifyByUser, status]。"
            + "其余 agri_* 表中的业务编号、状态、时间、路线、哈希、告警与说明类字段也可作为辅助上下文；"
            + "系统/权限/菜单/审计/定时任务类表仅用于流程回溯，不建议作为核心特征。";
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

    private String buildYieldInsightPrompt(String contextJson)
    {
        String safeContext = StringUtils.defaultIfBlank(contextJson, "{}");
        return "你将收到产量预测任务及其关联数据（地块、传感器、病虫害、农事与历史预测），请输出智能预测和建议。"
            + "请严格返回JSON对象，格式必须为："
            + "{\"forecastYieldKg\":0.0,\"insightSummary\":\"...\",\"suggestion\":\"...\",\"riskLevel\":\"低|中|高\",\"confidenceRate\":0.0,\"modelVersion\":\"...\"}。"
            + "其中 insightSummary 为中文综合结论，suggestion 给出可执行建议，confidenceRate 取值0到1。"
            + buildModelFieldGuide()
            + "输入数据如下：" + safeContext;
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
            + buildModelFieldGuide()
            + "输入数据如下：" + safeContext;
    }

    private String buildCarbonFootprintPrompt(String contextJson)
    {
        String safeContext = StringUtils.defaultIfBlank(contextJson, "{}");
        return "你将收到农业碳足迹核算模型数据，请输出模型诊断与优化建议。"
            + "请严格返回JSON对象，格式必须为："
            + "{\"insightSummary\":\"...\",\"riskLevel\":\"低|中|高\",\"suggestion\":\"...\",\"estimatedEmission\":0.0,\"confidenceRate\":0.0,\"modelVersion\":\"...\"}。"
            + "其中 insightSummary 用中文总结当前模型碳排放特征与异常点，suggestion 给出2-3条可执行的减排/复核建议，estimatedEmission 代表模型推断的碳排放估算值，confidenceRate 取值0到1。"
            + buildModelFieldGuide()
            + "输入数据如下：" + safeContext;
    }

    private String buildGeneralInsightPrompt(String scene, String contextJson)
    {
        String safeScene = StringUtils.defaultIfBlank(scene, "通用智能分析");
        String safeContext = StringUtils.defaultIfBlank(contextJson, "{}");
        return "你将收到农业业务场景数据，请输出" + safeScene + "结果。"
            + "请严格返回JSON对象，格式必须为："
            + "{\"insightSummary\":\"...\",\"riskLevel\":\"低|中|高\",\"suggestion\":\"...\",\"confidenceRate\":0.0,\"modelVersion\":\"...\"}。"
            + "其中 insightSummary 为中文综合结论，suggestion 为可执行建议，confidenceRate 取值0到1。"
            + buildModelFieldGuide()
            + "输入数据如下：" + safeContext;
    }

    private String inferRiskLevelFromText(String summary)
    {
        if (StringUtils.isBlank(summary))
        {
            return "中";
        }
        String text = summary.toLowerCase();
        if (text.contains("高") || text.contains("严重") || text.contains("异常") || text.contains("风险"))
        {
            return "高";
        }
        if (text.contains("低") || text.contains("平稳") || text.contains("正常"))
        {
            return "低";
        }
        return "中";
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

        private String rawContent;

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

        public String getRawContent()
        {
            return rawContent;
        }

        public void setRawContent(String rawContent)
        {
            this.rawContent = rawContent;
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

    public static class YieldInsightResult
    {
        private boolean success;

        private BigDecimal forecastYieldKg;

        private String insightSummary;

        private String suggestion;

        private String riskLevel;

        private BigDecimal confidenceRate;

        private String modelVersion;

        private String rawContent;

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

        public String getInsightSummary()
        {
            return insightSummary;
        }

        public void setInsightSummary(String insightSummary)
        {
            this.insightSummary = insightSummary;
        }

        public String getSuggestion()
        {
            return suggestion;
        }

        public void setSuggestion(String suggestion)
        {
            this.suggestion = suggestion;
        }

        public String getRiskLevel()
        {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel)
        {
            this.riskLevel = riskLevel;
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

        public String getRawContent()
        {
            return rawContent;
        }

        public void setRawContent(String rawContent)
        {
            this.rawContent = rawContent;
        }
    }

    public static class QualityResult
    {
        private boolean success;

        private String qualityGrade;

        private BigDecimal defectRate;

        private String inspectResult;

        private String modelVersion;

        private String rawContent;

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

        public String getRawContent()
        {
            return rawContent;
        }

        public void setRawContent(String rawContent)
        {
            this.rawContent = rawContent;
        }
    }

    public static class GeneralInsightResult
    {
        private boolean success;

        private String insightSummary;

        private String riskLevel;

        private String suggestion;

        private BigDecimal confidenceRate;

        private String modelVersion;

        private String rawContent;

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

        public String getRawContent()
        {
            return rawContent;
        }

        public void setRawContent(String rawContent)
        {
            this.rawContent = rawContent;
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

    public static class CarbonFootprintInsightResult
    {
        private boolean success;

        private String insightSummary;

        private String riskLevel;

        private String suggestion;

        private BigDecimal estimatedEmission;

        private BigDecimal confidenceRate;

        private String modelVersion;

        private String rawContent;

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

        public BigDecimal getEstimatedEmission()
        {
            return estimatedEmission;
        }

        public void setEstimatedEmission(BigDecimal estimatedEmission)
        {
            this.estimatedEmission = estimatedEmission;
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

        public String getRawContent()
        {
            return rawContent;
        }

        public void setRawContent(String rawContent)
        {
            this.rawContent = rawContent;
        }
    }
}
