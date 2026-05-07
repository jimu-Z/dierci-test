package com.ruoyi.web.controller.agri;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import java.util.List;

/**
 * Agri AI 结果归一化助手。
 */
public final class AgriAiResultHelper
{
    private AgriAiResultHelper()
    {
    }

    public static MergeResult mergeGeneralInsightResult(String summary, String riskLevel, List<String> targetList,
        AgriHttpIntegrationClient.GeneralInsightResult aiResult)
    {
        if (aiResult == null)
        {
            return new MergeResult(summary, riskLevel, null);
        }

        String aiOriginalExcerpt = aiResult.getRawContent();
        if (StringUtils.isNotBlank(aiResult.getInsightSummary()))
        {
            summary = aiResult.getInsightSummary();
        }
        if (StringUtils.isNotBlank(aiResult.getRiskLevel()))
        {
            riskLevel = aiResult.getRiskLevel();
        }
        if (StringUtils.isNotBlank(aiResult.getSuggestion()))
        {
            targetList.add(0, "AI建议：" + aiResult.getSuggestion());
        }
        if (StringUtils.isNotBlank(aiOriginalExcerpt))
        {
            targetList.add("AI原文摘录：" + aiOriginalExcerpt);
        }
        return new MergeResult(summary, riskLevel, aiOriginalExcerpt);
    }

    public static final class MergeResult
    {
        private final String summary;
        private final String riskLevel;
        private final String aiOriginalExcerpt;

        public MergeResult(String summary, String riskLevel, String aiOriginalExcerpt)
        {
            this.summary = summary;
            this.riskLevel = riskLevel;
            this.aiOriginalExcerpt = aiOriginalExcerpt;
        }

        public String getSummary()
        {
            return summary;
        }

        public String getRiskLevel()
        {
            return riskLevel;
        }

        public String getAiOriginalExcerpt()
        {
            return aiOriginalExcerpt;
        }
    }
}