package com.ruoyi.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.AgriCarbonFootprintModel;
import com.ruoyi.system.domain.AgriFarmOperationRecord;
import com.ruoyi.system.domain.AgriLogisticsTrack;
import com.ruoyi.system.domain.AgriProcessBatchLink;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.integration.AgriHttpIntegrationClient.CarbonFootprintInsightResult;
import com.ruoyi.system.integration.AgriIntegrationProperties;
import com.ruoyi.system.mapper.AgriCarbonFootprintModelMapper;
import com.ruoyi.system.service.IAgriCarbonFootprintModelService;
import com.ruoyi.system.service.IAgriFarmOperationRecordService;
import com.ruoyi.system.service.IAgriLogisticsTrackService;
import com.ruoyi.system.service.IAgriProcessBatchLinkService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 碳足迹核算模型Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriCarbonFootprintModelServiceImpl implements IAgriCarbonFootprintModelService
{
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
    private static final Logger log = LoggerFactory.getLogger(AgriCarbonFootprintModelServiceImpl.class);
    private static final int DEEPSEEK_RETRY_TIMES = 3;
    private static final int BUSINESS_WINDOW_DAYS = 90;

    @Autowired
    private AgriCarbonFootprintModelMapper agriCarbonFootprintModelMapper;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @Autowired
    private AgriIntegrationProperties agriIntegrationProperties;

    @Autowired
    private IAgriFarmOperationRecordService agriFarmOperationRecordService;

    @Autowired
    private IAgriLogisticsTrackService agriLogisticsTrackService;

    @Autowired
    private IAgriProcessBatchLinkService agriProcessBatchLinkService;

    @Override
    public AgriCarbonFootprintModel selectAgriCarbonFootprintModelByModelId(Long modelId)
    {
        return agriCarbonFootprintModelMapper.selectAgriCarbonFootprintModelByModelId(modelId);
    }

    @Override
    public List<AgriCarbonFootprintModel> selectAgriCarbonFootprintModelList(AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        return agriCarbonFootprintModelMapper.selectAgriCarbonFootprintModelList(agriCarbonFootprintModel);
    }

    @Override
    public Map<String, Object> selectAgriCarbonFootprintModelDashboard(AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        List<AgriCarbonFootprintModel> sourceList = agriCarbonFootprintModelMapper.selectAgriCarbonFootprintModelList(
            agriCarbonFootprintModel == null ? new AgriCarbonFootprintModel() : agriCarbonFootprintModel
        );
        List<AgriCarbonFootprintModel> modelList = sourceList.stream().map(this::normalizeModel).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", buildKpi(modelList));
        result.put("statusDistribution", buildStatusDistribution(modelList));
        result.put("productDistribution", buildProductDistribution(modelList));
        result.put("topList", buildTopList(modelList));
        result.put("trend", buildTrend(modelList));
        result.put("insight", buildInsight(modelList));
        result.put("recommendations", buildRecommendations(modelList));
        result.put("total", modelList.size());
        return result;
    }

    @Override
    public Map<String, Object> analyzeAgriCarbonFootprintModel(Long modelId)
    {
        AgriCarbonFootprintModel model = agriCarbonFootprintModelMapper.selectAgriCarbonFootprintModelByModelId(modelId);
        if (model == null)
        {
            return null;
        }
        AgriCarbonFootprintModel normalized = normalizeModel(model);
        BigDecimal estimatedEmission = estimateEmission(normalized);
        int riskScore = buildRiskScore(normalized, estimatedEmission, estimateEmissionByHistoryAge(normalized.getCalcTime()));
        String riskLevel = riskScore >= 60 ? "高" : riskScore >= 30 ? "中" : "低";
        Map<String, Object> businessCorrelation = buildBusinessCorrelation(normalized, estimatedEmission);
        Map<String, Object> boundaryCoverage = buildBoundaryCoverage(normalized, businessCorrelation);
        Map<String, Object> emissionFactorReview = buildEmissionFactorReview(normalized);
        Map<String, Object> rationalityValidation = buildRationalityValidation(estimatedEmission, businessCorrelation);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("model", normalized);
        context.put("estimatedEmission", estimatedEmission);
        context.put("riskScore", riskScore);
        context.put("riskLevel", riskLevel);
        context.put("factorProfile", buildFactorProfile(normalized));
        context.put("boundaryCoverage", boundaryCoverage);
        context.put("emissionFactorReview", emissionFactorReview);
        context.put("businessCorrelation", businessCorrelation);
        context.put("rationalityValidation", rationalityValidation);

        Map<String, Object> insight = new LinkedHashMap<>();
        insight.put("estimatedEmission", estimatedEmission);
        insight.put("riskLevel", riskLevel);
        insight.put("confidenceRate", confidenceByRisk(riskScore));
        insight.put("modelVersion", "rule-v1");
        insight.put("insightSummary", buildLocalInsight(normalized, estimatedEmission, riskLevel, boundaryCoverage, emissionFactorReview, rationalityValidation));
        insight.put("suggestion", buildLocalSuggestion(normalized, estimatedEmission, riskLevel));
        insight.put("analysisEngine", "rule-local");
        insight.put("deepseekStatus", "fallback");
        insight.put("deepseekMessage", "未调用DeepSeek，使用本地规则结果");
        insight.put("boundaryCoverage", boundaryCoverage);
        insight.put("emissionFactorReview", emissionFactorReview);
        insight.put("businessCorrelation", businessCorrelation);
        insight.put("rationalityValidation", rationalityValidation);

        Exception lastDeepSeekException = null;
        for (int i = 0; i < DEEPSEEK_RETRY_TIMES; i++)
        {
            try
            {
                CarbonFootprintInsightResult aiResult = agriHttpIntegrationClient.invokeCarbonFootprintInsight(JSON.toJSONString(context));
                if (aiResult != null)
                {
                    insight.put("insightSummary", StringUtils.defaultIfBlank(aiResult.getInsightSummary(), (String) insight.get("insightSummary")));
                    insight.put("riskLevel", StringUtils.defaultIfBlank(aiResult.getRiskLevel(), riskLevel));
                    insight.put("suggestion", StringUtils.defaultIfBlank(aiResult.getSuggestion(), (String) insight.get("suggestion")));
                    insight.put("confidenceRate", aiResult.getConfidenceRate() == null ? insight.get("confidenceRate") : aiResult.getConfidenceRate());
                    insight.put("modelVersion", StringUtils.defaultIfBlank(aiResult.getModelVersion(), "deepseek-chat"));
                    if (aiResult.getEstimatedEmission() != null && aiResult.getEstimatedEmission().compareTo(BigDecimal.ZERO) > 0)
                    {
                        insight.put("estimatedEmission", aiResult.getEstimatedEmission());
                    }
                    insight.put("analysisEngine", "deepseek");
                    insight.put("deepseekStatus", "success");
                    insight.put("deepseekMessage", "DeepSeek调用成功");
                    lastDeepSeekException = null;
                    break;
                }
            }
            catch (Exception ex)
            {
                lastDeepSeekException = ex;
                log.warn("DeepSeek调用失败，第{}次重试，modelId={}，原因={}", i + 1, modelId, ex.getMessage());
            }
        }

        if (lastDeepSeekException != null)
        {
            String reason = StringUtils.defaultIfBlank(lastDeepSeekException.getMessage(), "调用异常");
            insight.put("deepseekStatus", "failed");
            insight.put("deepseekMessage", "DeepSeek调用失败，已回退本地规则：" + reason);
        }

        BigDecimal finalEmission = safeBigDecimal((BigDecimal) insight.get("estimatedEmission")).setScale(4, RoundingMode.HALF_UP);
        normalized.setCarbonEmission(finalEmission);
        normalized.setCalcStatus("1");
        normalized.setCalcTime(new Date());
        normalized.setVerifier(currentOperator());
        normalized.setUpdateBy(currentOperator());
        agriCarbonFootprintModelMapper.updateAgriCarbonFootprintModel(normalized);

        insight.put("model", normalized);
        insight.put("factorProfile", buildFactorProfile(normalized));
        return insight;
    }

    private Map<String, Object> buildBusinessCorrelation(AgriCarbonFootprintModel model, BigDecimal estimatedEmission)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> keywords = buildModelKeywords(model);
        LocalDate thresholdDate = LocalDate.now().minusDays(BUSINESS_WINDOW_DAYS);

        List<AgriFarmOperationRecord> farmRecords = filterRecentFarmRecords(
            agriFarmOperationRecordService.selectAgriFarmOperationRecordList(new AgriFarmOperationRecord()),
            thresholdDate,
            keywords
        );
        List<AgriLogisticsTrack> logisticsTracks = filterRecentLogisticsTracks(
            agriLogisticsTrackService.selectAgriLogisticsTrackList(new AgriLogisticsTrack()),
            thresholdDate,
            keywords
        );
        List<AgriProcessBatchLink> processLinks = filterRecentProcessLinks(
            agriProcessBatchLinkService.selectAgriProcessBatchLinkList(new AgriProcessBatchLink()),
            thresholdDate,
            keywords
        );

        result.put("production", buildProductionMetrics(farmRecords));
        result.put("transportation", buildTransportationMetrics(logisticsTracks));
        result.put("processing", buildProcessingMetrics(processLinks));
        result.put("keywords", keywords);
        result.put("windowDays", BUSINESS_WINDOW_DAYS);
        result.put("activityEmissionBenchmark", buildActivityEmissionBenchmark(farmRecords, logisticsTracks, processLinks));
        result.put("estimatedEmission", estimatedEmission);
        return result;
    }

    private Map<String, Object> buildBoundaryCoverage(AgriCarbonFootprintModel model, Map<String, Object> businessCorrelation)
    {
        Map<String, Object> coverage = new LinkedHashMap<>();
        String scope = StringUtils.defaultString(model.getBoundaryScope());
        @SuppressWarnings("unchecked")
        Map<String, Object> production = (Map<String, Object>) businessCorrelation.getOrDefault("production", new LinkedHashMap<>());
        @SuppressWarnings("unchecked")
        Map<String, Object> transportation = (Map<String, Object>) businessCorrelation.getOrDefault("transportation", new LinkedHashMap<>());
        @SuppressWarnings("unchecked")
        Map<String, Object> processing = (Map<String, Object>) businessCorrelation.getOrDefault("processing", new LinkedHashMap<>());

        coverage.put("production", buildBoundaryStage("生产", containsAny(scope, "生产", "种植", "农事", "田间"), intValue(production.get("recordCount"))));
        coverage.put("transportation", buildBoundaryStage("运输", containsAny(scope, "运输", "物流", "在途", "冷链"), intValue(transportation.get("recordCount"))));
        coverage.put("processing", buildBoundaryStage("加工", containsAny(scope, "加工", "分拣", "包装", "仓储"), intValue(processing.get("recordCount"))));

        int covered = 0;
        int withData = 0;
        for (Object value : coverage.values())
        {
            @SuppressWarnings("unchecked")
            Map<String, Object> stage = (Map<String, Object>) value;
            if (Boolean.TRUE.equals(stage.get("inBoundary")))
            {
                covered++;
                if (intValue(stage.get("dataCount")) > 0)
                {
                    withData++;
                }
            }
        }
        coverage.put("coveredStageCount", covered);
        coverage.put("coveredWithData", withData);
        coverage.put("completenessRate", covered == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(withData).divide(BigDecimal.valueOf(covered), 2, RoundingMode.HALF_UP));
        coverage.put("coverageLevel", covered == 0 ? "低" : withData == covered ? "高" : "中");
        return coverage;
    }

    private Map<String, Object> buildEmissionFactorReview(AgriCarbonFootprintModel model)
    {
        Map<String, Object> review = new LinkedHashMap<>();
        BigDecimal factor = safeBigDecimal(model.getEmissionFactor());
        BigDecimal lower;
        BigDecimal upper;
        String productType = StringUtils.defaultString(model.getProductType());
        if ("vegetable".equals(productType))
        {
            lower = new BigDecimal("0.30");
            upper = new BigDecimal("2.20");
        }
        else if ("fruit".equals(productType))
        {
            lower = new BigDecimal("0.40");
            upper = new BigDecimal("2.80");
        }
        else if ("grain".equals(productType))
        {
            lower = new BigDecimal("0.55");
            upper = new BigDecimal("3.20");
        }
        else
        {
            lower = new BigDecimal("0.40");
            upper = new BigDecimal("3.00");
        }

        String reviewStatus;
        String suggestion;
        if (factor.compareTo(BigDecimal.ZERO) <= 0)
        {
            reviewStatus = "缺失";
            suggestion = "排放因子为空，建议优先采用行业标准因子并补充实测数据。";
        }
        else if (factor.compareTo(lower) >= 0 && factor.compareTo(upper) <= 0)
        {
            reviewStatus = "合理";
            suggestion = "排放因子处于行业参考区间，可继续用实测值做季度校准。";
        }
        else
        {
            reviewStatus = "偏离";
            suggestion = "排放因子偏离行业参考区间，建议复核来源并结合实测值修正。";
        }

        review.put("currentFactor", factor);
        review.put("benchmarkLower", lower);
        review.put("benchmarkUpper", upper);
        review.put("reviewStatus", reviewStatus);
        review.put("recommendedSource", "行业标准数据库或企业实测值");
        review.put("suggestion", suggestion);
        return review;
    }

    private Map<String, Object> buildRationalityValidation(BigDecimal estimatedEmission, Map<String, Object> businessCorrelation)
    {
        Map<String, Object> validation = new LinkedHashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> benchmark = (Map<String, Object>) businessCorrelation.getOrDefault("activityEmissionBenchmark", new LinkedHashMap<>());
        BigDecimal activityEstimate = safeBigDecimal((BigDecimal) benchmark.get("activityEstimatedEmission"));
        BigDecimal lowerBound = activityEstimate.multiply(new BigDecimal("0.70")).setScale(4, RoundingMode.HALF_UP);
        BigDecimal upperBound = activityEstimate.multiply(new BigDecimal("1.30")).setScale(4, RoundingMode.HALF_UP);

        String status;
        String message;
        if (activityEstimate.compareTo(BigDecimal.ZERO) == 0)
        {
            status = "数据不足";
            message = "业务活动样本不足，暂无法进行强校验，建议补齐农事、物流、加工记录后再复核。";
        }
        else if (estimatedEmission.compareTo(lowerBound) >= 0 && estimatedEmission.compareTo(upperBound) <= 0)
        {
            status = "合理";
            message = "估算值与业务活动强度基本一致。";
        }
        else
        {
            status = "偏差";
            message = "估算值与业务活动强度存在偏差，建议复核核算边界与排放因子。";
        }

        validation.put("estimatedEmission", estimatedEmission);
        validation.put("activityEstimatedEmission", activityEstimate);
        validation.put("lowerBound", lowerBound);
        validation.put("upperBound", upperBound);
        validation.put("status", status);
        validation.put("message", message);
        return validation;
    }

    private List<AgriFarmOperationRecord> filterRecentFarmRecords(List<AgriFarmOperationRecord> source, LocalDate thresholdDate, List<String> keywords)
    {
        List<AgriFarmOperationRecord> result = source.stream()
            .filter(Objects::nonNull)
            .filter(item -> "0".equals(StringUtils.defaultIfBlank(item.getStatus(), "0")))
            .filter(item -> isAfter(item.getOperationTime(), thresholdDate))
            .filter(item -> containsAny(StringUtils.defaultString(item.getOperationContent()) + " " + StringUtils.defaultString(item.getInputName()) + " " + StringUtils.defaultString(item.getPlotCode()), keywords))
            .collect(Collectors.toList());
        if (!result.isEmpty())
        {
            return result;
        }
        return source.stream()
            .filter(Objects::nonNull)
            .filter(item -> isAfter(item.getOperationTime(), thresholdDate))
            .sorted(Comparator.comparing(AgriFarmOperationRecord::getOperationTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .limit(50)
            .collect(Collectors.toList());
    }

    private List<AgriLogisticsTrack> filterRecentLogisticsTracks(List<AgriLogisticsTrack> source, LocalDate thresholdDate, List<String> keywords)
    {
        List<AgriLogisticsTrack> result = source.stream()
            .filter(Objects::nonNull)
            .filter(item -> "0".equals(StringUtils.defaultIfBlank(item.getStatus(), "0")))
            .filter(item -> isAfter(item.getEventTime(), thresholdDate))
            .filter(item -> containsAny(StringUtils.defaultString(item.getTraceCode()) + " " + StringUtils.defaultString(item.getProductBatchNo()) + " " + StringUtils.defaultString(item.getEventDesc()), keywords))
            .collect(Collectors.toList());
        if (!result.isEmpty())
        {
            return result;
        }
        return source.stream()
            .filter(Objects::nonNull)
            .filter(item -> isAfter(item.getEventTime(), thresholdDate))
            .sorted(Comparator.comparing(AgriLogisticsTrack::getEventTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .limit(50)
            .collect(Collectors.toList());
    }

    private List<AgriProcessBatchLink> filterRecentProcessLinks(List<AgriProcessBatchLink> source, LocalDate thresholdDate, List<String> keywords)
    {
        List<AgriProcessBatchLink> result = source.stream()
            .filter(Objects::nonNull)
            .filter(item -> "0".equals(StringUtils.defaultIfBlank(item.getStatus(), "0")))
            .filter(item -> isAfter(item.getProcessTime(), thresholdDate))
            .filter(item -> containsAny(StringUtils.defaultString(item.getProductCode()) + " " + StringUtils.defaultString(item.getProcessBatchNo()) + " " + StringUtils.defaultString(item.getPlantingBatchNo()), keywords))
            .collect(Collectors.toList());
        if (!result.isEmpty())
        {
            return result;
        }
        return source.stream()
            .filter(Objects::nonNull)
            .filter(item -> isAfter(item.getProcessTime(), thresholdDate))
            .sorted(Comparator.comparing(AgriProcessBatchLink::getProcessTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .limit(50)
            .collect(Collectors.toList());
    }

    private Map<String, Object> buildProductionMetrics(List<AgriFarmOperationRecord> records)
    {
        Map<String, Object> metrics = new LinkedHashMap<>();
        BigDecimal totalInputAmount = records.stream()
            .map(AgriFarmOperationRecord::getInputAmount)
            .map(this::safeBigDecimal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_UP);
        long fertilizerLikeCount = records.stream()
            .filter(item -> containsAny(StringUtils.defaultString(item.getInputName()) + " " + StringUtils.defaultString(item.getOperationContent()), "肥", "农药", "药剂", "饲料"))
            .count();
        metrics.put("recordCount", records.size());
        metrics.put("totalInputAmount", totalInputAmount);
        metrics.put("fertilizerLikeCount", fertilizerLikeCount);
        return metrics;
    }

    private Map<String, Object> buildTransportationMetrics(List<AgriLogisticsTrack> tracks)
    {
        Map<String, Object> metrics = new LinkedHashMap<>();
        long abnormalCount = tracks.stream().filter(item -> "3".equals(item.getTrackStatus())).count();
        long inTransitCount = tracks.stream().filter(item -> "1".equals(item.getTrackStatus())).count();
        BigDecimal avgTemp = averageOfBigDecimal(tracks.stream().map(AgriLogisticsTrack::getTemperature).collect(Collectors.toList()));
        BigDecimal avgHumidity = averageOfBigDecimal(tracks.stream().map(AgriLogisticsTrack::getHumidity).collect(Collectors.toList()));
        metrics.put("recordCount", tracks.size());
        metrics.put("inTransitCount", inTransitCount);
        metrics.put("abnormalCount", abnormalCount);
        metrics.put("avgTemp", avgTemp);
        metrics.put("avgHumidity", avgHumidity);
        return metrics;
    }

    private Map<String, Object> buildProcessingMetrics(List<AgriProcessBatchLink> processLinks)
    {
        Map<String, Object> metrics = new LinkedHashMap<>();
        long finishedCount = processLinks.stream().filter(item -> "2".equals(item.getProcessStatus())).count();
        BigDecimal totalWeight = processLinks.stream()
            .map(AgriProcessBatchLink::getProcessWeightKg)
            .map(this::safeBigDecimal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_UP);
        metrics.put("recordCount", processLinks.size());
        metrics.put("finishedCount", finishedCount);
        metrics.put("totalWeightKg", totalWeight);
        return metrics;
    }

    private Map<String, Object> buildActivityEmissionBenchmark(List<AgriFarmOperationRecord> farmRecords,
                                                               List<AgriLogisticsTrack> logisticsTracks,
                                                               List<AgriProcessBatchLink> processLinks)
    {
        BigDecimal productionEmission = farmRecords.stream()
            .map(AgriFarmOperationRecord::getInputAmount)
            .map(this::safeBigDecimal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .multiply(new BigDecimal("0.35"))
            .add(BigDecimal.valueOf(farmRecords.size()).multiply(new BigDecimal("1.80")));

        long abnormalCount = logisticsTracks.stream().filter(item -> "3".equals(item.getTrackStatus())).count();
        BigDecimal transportationEmission = BigDecimal.valueOf(logisticsTracks.size()).multiply(new BigDecimal("15.00"))
            .add(BigDecimal.valueOf(abnormalCount).multiply(new BigDecimal("4.00")));

        BigDecimal processWeight = processLinks.stream()
            .map(AgriProcessBatchLink::getProcessWeightKg)
            .map(this::safeBigDecimal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal processingEmission = processWeight.multiply(new BigDecimal("0.08"))
            .add(BigDecimal.valueOf(processLinks.size()).multiply(new BigDecimal("2.50")));

        BigDecimal total = productionEmission.add(transportationEmission).add(processingEmission).setScale(4, RoundingMode.HALF_UP);

        Map<String, Object> benchmark = new LinkedHashMap<>();
        benchmark.put("productionEstimatedEmission", productionEmission.setScale(4, RoundingMode.HALF_UP));
        benchmark.put("transportationEstimatedEmission", transportationEmission.setScale(4, RoundingMode.HALF_UP));
        benchmark.put("processingEstimatedEmission", processingEmission.setScale(4, RoundingMode.HALF_UP));
        benchmark.put("activityEstimatedEmission", total);
        return benchmark;
    }

    private Map<String, Object> buildBoundaryStage(String name, boolean inBoundary, int dataCount)
    {
        Map<String, Object> stage = new LinkedHashMap<>();
        stage.put("name", name);
        stage.put("inBoundary", inBoundary);
        stage.put("dataCount", dataCount);
        stage.put("coverageStatus", !inBoundary ? "未纳入" : dataCount > 0 ? "已覆盖" : "缺数据");
        return stage;
    }

    private List<String> buildModelKeywords(AgriCarbonFootprintModel model)
    {
        List<String> keywords = new ArrayList<>();
        addKeyword(keywords, model.getModelCode());
        addKeyword(keywords, model.getModelName());
        addKeyword(keywords, model.getBoundaryScope());
        addKeyword(keywords, productTypeLabel(model.getProductType()));
        addKeyword(keywords, model.getProductType());
        return keywords.stream().distinct().collect(Collectors.toList());
    }

    private void addKeyword(List<String> keywords, String value)
    {
        if (StringUtils.isBlank(value))
        {
            return;
        }
        String normalized = value.trim().toLowerCase();
        if (normalized.length() > 1)
        {
            keywords.add(normalized);
        }
    }

    private boolean isAfter(Date date, LocalDate threshold)
    {
        if (date == null)
        {
            return false;
        }
        LocalDate localDate = date.toInstant().atZone(SYSTEM_ZONE).toLocalDate();
        return !localDate.isBefore(threshold);
    }

    private boolean containsAny(String text, List<String> keywords)
    {
        if (StringUtils.isBlank(text) || keywords == null || keywords.isEmpty())
        {
            return false;
        }
        String source = text.toLowerCase();
        return keywords.stream().anyMatch(source::contains);
    }

    private boolean containsAny(String text, String... words)
    {
        if (StringUtils.isBlank(text) || words == null || words.length == 0)
        {
            return false;
        }
        String source = text.toLowerCase();
        for (String word : words)
        {
            if (StringUtils.isNotBlank(word) && source.contains(word.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }

    private int intValue(Object value)
    {
        if (value == null)
        {
            return 0;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.parseInt(String.valueOf(value));
        }
        catch (Exception ex)
        {
            return 0;
        }
    }

    private BigDecimal averageOfBigDecimal(List<BigDecimal> values)
    {
        List<BigDecimal> valid = values.stream().map(this::safeBigDecimal).filter(item -> item.compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
        if (valid.isEmpty())
        {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sum = valid.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(valid.size()), 2, RoundingMode.HALF_UP);
    }

    @Override
    public Map<String, Object> checkCarbonFootprintAiHealth()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        AgriIntegrationProperties.Ai ai = agriIntegrationProperties.getAi();

        result.put("enabled", ai.isEnabled());
        result.put("baseUrl", ai.getBaseUrl());
        result.put("model", ai.getModel());
        result.put("path", ai.getCarbonFootprintPath());
        result.put("apiKeyConfigured", StringUtils.isNotBlank(ai.getApiKey()));

        long start = System.currentTimeMillis();
        try
        {
            agriHttpIntegrationClient.invokeCarbonFootprintInsight("{\"healthCheck\":true}");
            result.put("status", "UP");
            result.put("analysisEngine", "deepseek");
            result.put("message", "DeepSeek调用成功");
        }
        catch (Exception ex)
        {
            result.put("status", "DOWN");
            result.put("analysisEngine", "rule-local");
            result.put("message", StringUtils.defaultIfBlank(ex.getMessage(), "DeepSeek调用失败"));
        }
        result.put("latencyMs", System.currentTimeMillis() - start);
        return result;
    }

    private String currentOperator()
    {
        try
        {
            return StringUtils.defaultIfBlank(SecurityUtils.getUsername(), "system");
        }
        catch (Exception ex)
        {
            return "system";
        }
    }

    @Override
    public int insertAgriCarbonFootprintModel(AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        return agriCarbonFootprintModelMapper.insertAgriCarbonFootprintModel(agriCarbonFootprintModel);
    }

    @Override
    public int updateAgriCarbonFootprintModel(AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        return agriCarbonFootprintModelMapper.updateAgriCarbonFootprintModel(agriCarbonFootprintModel);
    }

    @Override
    public int deleteAgriCarbonFootprintModelByModelIds(Long[] modelIds)
    {
        return agriCarbonFootprintModelMapper.deleteAgriCarbonFootprintModelByModelIds(modelIds);
    }

    private AgriCarbonFootprintModel normalizeModel(AgriCarbonFootprintModel model)
    {
        if (model == null)
        {
            return new AgriCarbonFootprintModel();
        }
        if (model.getEmissionFactor() == null)
        {
            model.setEmissionFactor(BigDecimal.ZERO);
        }
        if (model.getCarbonEmission() == null)
        {
            model.setCarbonEmission(BigDecimal.ZERO);
        }
        if (StringUtils.isBlank(model.getCalcStatus()))
        {
            model.setCalcStatus("0");
        }
        if (StringUtils.isBlank(model.getStatus()))
        {
            model.setStatus("0");
        }
        return model;
    }

    private Map<String, Object> buildKpi(List<AgriCarbonFootprintModel> modelList)
    {
        Map<String, Object> kpi = new LinkedHashMap<>();
        int total = modelList.size();
        long calculated = modelList.stream().filter(item -> "1".equals(item.getCalcStatus()) || "2".equals(item.getCalcStatus())).count();
        long reviewed = modelList.stream().filter(item -> "2".equals(item.getCalcStatus())).count();
        long pending = total - calculated;
        BigDecimal avgFactor = averageOf(modelList, AgriCarbonFootprintModel::getEmissionFactor);
        BigDecimal avgEmission = averageOf(modelList, AgriCarbonFootprintModel::getCarbonEmission);
        BigDecimal totalEmission = modelList.stream().map(item -> safeBigDecimal(item.getCarbonEmission())).reduce(BigDecimal.ZERO, BigDecimal::add);
        long highEmissionCount = modelList.stream().filter(item -> effectiveEmission(item).compareTo(avgEmission.multiply(new BigDecimal("1.2"))) > 0).count();

        kpi.put("totalCount", total);
        kpi.put("calculatedCount", calculated);
        kpi.put("reviewedCount", reviewed);
        kpi.put("pendingCount", pending);
        kpi.put("highEmissionCount", highEmissionCount);
        kpi.put("avgFactor", avgFactor);
        kpi.put("avgEmission", avgEmission);
        kpi.put("totalEmission", totalEmission);
        kpi.put("reviewRate", total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(reviewed).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP));
        return kpi;
    }

    private List<Map<String, Object>> buildStatusDistribution(List<AgriCarbonFootprintModel> modelList)
    {
        int total = Math.max(modelList.size(), 1);
        long pending = modelList.stream().filter(item -> "0".equals(item.getCalcStatus())).count();
        long calculated = modelList.stream().filter(item -> "1".equals(item.getCalcStatus())).count();
        long reviewed = modelList.stream().filter(item -> "2".equals(item.getCalcStatus())).count();
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildDistributionItem("pending", "待计算", pending, total, "info"));
        list.add(buildDistributionItem("calculated", "已计算", calculated, total, "success"));
        list.add(buildDistributionItem("reviewed", "已复核", reviewed, total, "primary"));
        return list;
    }

    private List<Map<String, Object>> buildProductDistribution(List<AgriCarbonFootprintModel> modelList)
    {
        Map<String, Long> counts = modelList.stream().collect(Collectors.groupingBy(item -> StringUtils.defaultIfBlank(item.getProductType(), "unknown"), LinkedHashMap::new, Collectors.counting()));
        int total = Math.max(modelList.size(), 1);
        List<Map<String, Object>> list = new ArrayList<>();
        counts.forEach((key, value) -> list.add(buildDistributionItem(key, productTypeLabel(key), value, total, "warning")));
        return list;
    }

    private Map<String, Object> buildDistributionItem(String key, String label, long count, int total, String type)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("label", label);
        item.put("count", count);
        item.put("rate", total == 0 ? 0 : BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP));
        item.put("type", type);
        return item;
    }

    private List<Map<String, Object>> buildTopList(List<AgriCarbonFootprintModel> modelList)
    {
        return modelList.stream()
            .sorted(Comparator.comparing(this::effectiveEmission).reversed())
            .limit(6)
            .map(this::toModelCard)
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildTrend(List<AgriCarbonFootprintModel> modelList)
    {
        Map<String, BigDecimal> emissionByDay = modelList.stream()
            .filter(item -> item.getCalcTime() != null)
            .collect(Collectors.groupingBy(item -> formatDay(item.getCalcTime()), LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO, this::effectiveEmission, BigDecimal::add)));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--)
        {
            String day = java.time.LocalDate.now().minusDays(offset).toString();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("day", day);
            item.put("emission", emissionByDay.getOrDefault(day, BigDecimal.ZERO));
            trend.add(item);
        }
        return trend;
    }

    private Map<String, Object> buildInsight(List<AgriCarbonFootprintModel> modelList)
    {
        Map<String, Object> insight = new LinkedHashMap<>();
        BigDecimal avgEmission = averageOf(modelList, AgriCarbonFootprintModel::getCarbonEmission);
        BigDecimal avgFactor = averageOf(modelList, AgriCarbonFootprintModel::getEmissionFactor);
        long pendingCount = modelList.stream().filter(item -> "0".equals(item.getCalcStatus())).count();
        long staleCount = modelList.stream().filter(item -> item.getCalcTime() == null || ChronoUnit.DAYS.between(item.getCalcTime().toInstant().atZone(SYSTEM_ZONE).toLocalDate(), java.time.LocalDate.now()) > 30).count();
        insight.put("riskLevel", pendingCount > modelList.size() / 2 ? "高" : staleCount > 0 ? "中" : "低");
        insight.put("confidenceRate", confidenceByCompleteness(modelList));
        insight.put("modelVersion", "rule-v1");
        insight.put("insightSummary", buildLocalInsightSummary(modelList, avgEmission, avgFactor, pendingCount, staleCount));
        insight.put("suggestion", buildLocalSuggestion(modelList, pendingCount, staleCount));
        return insight;
    }

    private List<String> buildRecommendations(List<AgriCarbonFootprintModel> modelList)
    {
        List<String> recommendations = new ArrayList<>();
        long pendingCount = modelList.stream().filter(item -> "0".equals(item.getCalcStatus())).count();
        long highEmissionCount = modelList.stream().filter(item -> effectiveEmission(item).compareTo(averageOf(modelList, AgriCarbonFootprintModel::getCarbonEmission).multiply(new BigDecimal("1.2"))) > 0).count();
        if (pendingCount > 0)
        {
            recommendations.add("优先补齐待计算模型并执行智能估算，减少人工录入遗漏。");
        }
        if (highEmissionCount > 0)
        {
            recommendations.add("对高排放模型进行边界复核和排放因子校正，优先优化高占比产品类型。");
        }
        recommendations.add("按周复核最近30天未更新模型，避免旧核算结果长期沿用。");
        return recommendations;
    }

    private Map<String, Object> toModelCard(AgriCarbonFootprintModel model)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("modelId", model.getModelId());
        item.put("modelCode", model.getModelCode());
        item.put("modelName", model.getModelName());
        item.put("productType", model.getProductType());
        item.put("boundaryScope", model.getBoundaryScope());
        item.put("calcStatus", model.getCalcStatus());
        item.put("emissionFactor", model.getEmissionFactor());
        item.put("carbonEmission", model.getCarbonEmission());
        item.put("effectiveEmission", effectiveEmission(model));
        item.put("riskLevel", riskLevel(model));
        item.put("suggestion", buildLocalSuggestion(model, effectiveEmission(model), riskLevel(model)));
        return item;
    }

    private BigDecimal effectiveEmission(AgriCarbonFootprintModel model)
    {
        BigDecimal emission = safeBigDecimal(model.getCarbonEmission());
        if (emission.compareTo(BigDecimal.ZERO) > 0)
        {
            return emission;
        }
        return estimateEmission(model);
    }

    private BigDecimal estimateEmission(AgriCarbonFootprintModel model)
    {
        BigDecimal factor = safeBigDecimal(model.getEmissionFactor());
        BigDecimal multiplier = productMultiplier(model.getProductType())
            .multiply(boundaryMultiplier(model.getBoundaryScope()))
            .multiply(statusMultiplier(model.getCalcStatus()))
            .multiply(ageMultiplier(model.getCalcTime()));
        return factor.multiply(multiplier).multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
    }

    private int buildRiskScore(AgriCarbonFootprintModel model, BigDecimal estimatedEmission, BigDecimal ageAdjustedEmission)
    {
        int score = 0;
        if ("0".equals(model.getCalcStatus()))
        {
            score += 30;
        }
        if (model.getCarbonEmission() == null || model.getCarbonEmission().compareTo(BigDecimal.ZERO) <= 0)
        {
            score += 20;
        }
        if (safeBigDecimal(model.getEmissionFactor()).compareTo(new BigDecimal("5")) > 0)
        {
            score += 10;
        }
        if (estimatedEmission.compareTo(new BigDecimal("1000")) > 0)
        {
            score += 20;
        }
        if (ageAdjustedEmission.compareTo(new BigDecimal("1000")) > 0)
        {
            score += 10;
        }
        return Math.min(score, 100);
    }

    private String riskLevel(AgriCarbonFootprintModel model)
    {
        int score = buildRiskScore(model, effectiveEmission(model), estimateEmissionByHistoryAge(model.getCalcTime()));
        return score >= 60 ? "高" : score >= 30 ? "中" : "低";
    }

    private BigDecimal estimateEmissionByHistoryAge(Date calcTime)
    {
        if (calcTime == null)
        {
            return BigDecimal.ZERO;
        }
        long days = ChronoUnit.DAYS.between(calcTime.toInstant().atZone(SYSTEM_ZONE).toLocalDate(), java.time.LocalDate.now());
        return BigDecimal.valueOf(Math.max(days, 0));
    }

    private BigDecimal productMultiplier(String productType)
    {
        if ("vegetable".equals(productType))
        {
            return new BigDecimal("0.90");
        }
        if ("fruit".equals(productType))
        {
            return new BigDecimal("1.00");
        }
        if ("grain".equals(productType))
        {
            return new BigDecimal("1.12");
        }
        return new BigDecimal("1.00");
    }

    private BigDecimal boundaryMultiplier(String boundaryScope)
    {
        String value = StringUtils.defaultString(boundaryScope);
        if (value.contains("全链路") || value.contains("全周期"))
        {
            return new BigDecimal("1.25");
        }
        if (value.contains("加工") || value.contains("物流") || value.contains("仓储"))
        {
            return new BigDecimal("1.12");
        }
        if (value.contains("种植") || value.contains("田间"))
        {
            return new BigDecimal("0.96");
        }
        return new BigDecimal("1.00");
    }

    private BigDecimal statusMultiplier(String calcStatus)
    {
        if ("0".equals(calcStatus))
        {
            return new BigDecimal("1.10");
        }
        if ("2".equals(calcStatus))
        {
            return new BigDecimal("0.98");
        }
        return new BigDecimal("1.00");
    }

    private BigDecimal ageMultiplier(Date calcTime)
    {
        if (calcTime == null)
        {
            return new BigDecimal("1.08");
        }
        long days = ChronoUnit.DAYS.between(calcTime.toInstant().atZone(SYSTEM_ZONE).toLocalDate(), java.time.LocalDate.now());
        if (days > 30)
        {
            return new BigDecimal("1.05");
        }
        return BigDecimal.ONE;
    }

    private String buildLocalInsightSummary(List<AgriCarbonFootprintModel> modelList, BigDecimal avgEmission, BigDecimal avgFactor, long pendingCount, long staleCount)
    {
        return "当前共有" + modelList.size() + "个碳足迹模型，平均排放因子约为" + avgFactor + "，平均碳排放约为" + avgEmission + "。"
            + "其中待计算模型" + pendingCount + "个，超过30天未复核模型" + staleCount + "个，建议优先处理高排放与久未更新模型。";
    }

    private String buildLocalSuggestion(List<AgriCarbonFootprintModel> modelList, long pendingCount, long staleCount)
    {
        if (pendingCount > 0)
        {
            return "先补齐待计算模型，再对高排放模型进行边界复核；必要时启用智能分析辅助核算。";
        }
        if (staleCount > 0)
        {
            return "建议更新久未复核模型的核算时间，并重新验证排放因子。";
        }
        return "当前模型整体较稳定，可按月复核并优先关注排放因子异常的模型。";
    }

    private String buildLocalSuggestion(AgriCarbonFootprintModel model, BigDecimal emission, String riskLevel)
    {
        if ("高".equals(riskLevel))
        {
            return "优先复核核算边界与排放因子，建议缩小高排放环节范围并重新核算。";
        }
        if ("中".equals(riskLevel))
        {
            return "建议补齐复核人和核算时间，确认当前模型是否仍适配现有生产场景。";
        }
        return "模型状态稳定，可维持当前参数并按周期复核。";
    }

    private String buildLocalInsight(AgriCarbonFootprintModel model,
                                     BigDecimal emission,
                                     String riskLevel,
                                     Map<String, Object> boundaryCoverage,
                                     Map<String, Object> emissionFactorReview,
                                     Map<String, Object> rationalityValidation)
    {
        String coverageLevel = String.valueOf(boundaryCoverage.getOrDefault("coverageLevel", "中"));
        String factorStatus = String.valueOf(emissionFactorReview.getOrDefault("reviewStatus", "待复核"));
        String rationality = String.valueOf(rationalityValidation.getOrDefault("status", "待验证"));
        return "模型" + model.getModelName() + "的智能估算碳排放约为" + emission + "，当前风险等级为" + riskLevel
            + "。边界覆盖水平" + coverageLevel + "，排放因子复核结果为" + factorStatus + "，业务活动合理性校验为" + rationality + "。";
    }

    private String buildFactorProfile(AgriCarbonFootprintModel model)
    {
        return "product=" + productTypeLabel(model.getProductType()) + ", boundary=" + StringUtils.defaultIfBlank(model.getBoundaryScope(), "未填写")
            + ", status=" + calcStatusLabel(model.getCalcStatus());
    }

    private String productTypeLabel(String productType)
    {
        if ("vegetable".equals(productType)) return "蔬菜";
        if ("fruit".equals(productType)) return "水果";
        if ("grain".equals(productType)) return "粮食";
        return StringUtils.defaultIfBlank(productType, "未知");
    }

    private String calcStatusLabel(String calcStatus)
    {
        if ("0".equals(calcStatus)) return "待计算";
        if ("1".equals(calcStatus)) return "已计算";
        if ("2".equals(calcStatus)) return "已复核";
        return StringUtils.defaultIfBlank(calcStatus, "未知");
    }

    private BigDecimal confidenceByCompleteness(List<AgriCarbonFootprintModel> modelList)
    {
        if (modelList.isEmpty())
        {
            return new BigDecimal("0.50");
        }
        long completed = modelList.stream().filter(item -> "1".equals(item.getCalcStatus()) || "2".equals(item.getCalcStatus())).count();
        return BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(modelList.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal confidenceByRisk(int riskScore)
    {
        if (riskScore >= 60)
        {
            return new BigDecimal("0.82");
        }
        if (riskScore >= 30)
        {
            return new BigDecimal("0.72");
        }
        return new BigDecimal("0.88");
    }

    private BigDecimal averageOf(List<AgriCarbonFootprintModel> modelList, java.util.function.Function<AgriCarbonFootprintModel, BigDecimal> extractor)
    {
        if (modelList.isEmpty())
        {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        BigDecimal sum = modelList.stream()
            .map(extractor)
            .map(this::safeBigDecimal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(modelList.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal safeBigDecimal(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatDay(Date date)
    {
        return date.toInstant().atZone(SYSTEM_ZONE).toLocalDate().toString();
    }
}
