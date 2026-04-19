package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriDataAttestationVerify;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriDataAttestationVerifyService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据存证与校验Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/attestationVerify")
public class AgriDataAttestationVerifyController extends BaseController
{
    @Autowired
    private IAgriDataAttestationVerifyService agriDataAttestationVerifyService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriDataAttestationVerify agriDataAttestationVerify)
    {
        startPage();
        return getDataTable(agriDataAttestationVerifyService.selectAgriDataAttestationVerifyList(agriDataAttestationVerify));
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriDataAttestationVerify> list = agriDataAttestationVerifyService.selectAgriDataAttestationVerifyList(new AgriDataAttestationVerify());
        int total = list.size();
        int matched = 0;
        int mismatched = 0;
        int pending = 0;
        for (AgriDataAttestationVerify item : list)
        {
            if ("1".equals(item.getVerifyStatus()))
            {
                matched++;
            }
            else if ("2".equals(item.getVerifyStatus()))
            {
                mismatched++;
            }
            else
            {
                pending++;
            }
        }

        List<AgriDataAttestationVerify> recent = list.stream()
            .sorted(Comparator.comparing(AgriDataAttestationVerify::getVerifyTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(8)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("matched", matched);
        kpi.put("mismatched", mismatched);
        kpi.put("pending", pending);
        kpi.put("matchRate", total == 0 ? 0 : (matched * 100.0 / total));
        result.put("kpi", kpi);
        result.put("recent", recent);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:list')")
    @GetMapping("/ops/overview")
    public AjaxResult opsOverview()
    {
        List<AgriDataAttestationVerify> list = agriDataAttestationVerifyService.selectAgriDataAttestationVerifyList(new AgriDataAttestationVerify());
        int total = list.size();
        int matched = 0;
        int mismatched = 0;
        int pending = 0;
        int highRisk = 0;

        Map<String, Integer> typeCounter = new LinkedHashMap<>();
        Map<String, Integer> statusCounter = new LinkedHashMap<>();
        Map<String, Integer> dayCounter = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--)
        {
            dayCounter.put(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, DateUtils.addDays(DateUtils.getNowDate(), -i)), 0);
        }

        for (AgriDataAttestationVerify item : list)
        {
            if ("1".equals(item.getVerifyStatus()))
            {
                matched++;
            }
            else if ("2".equals(item.getVerifyStatus()))
            {
                mismatched++;
                highRisk++;
            }
            else
            {
                pending++;
            }
            typeCounter.merge(defaultValue(item.getDataType(), "未知"), 1, Integer::sum);
            statusCounter.merge(statusLabel(item.getVerifyStatus()), 1, Integer::sum);
            if (item.getVerifyTime() != null)
            {
                String dayKey = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, item.getVerifyTime());
                if (dayCounter.containsKey(dayKey))
                {
                    dayCounter.put(dayKey, dayCounter.get(dayKey) + 1);
                }
            }
        }

        List<AgriDataAttestationVerify> riskQueue = list.stream()
            .sorted((a, b) -> Integer.compare(scoreRisk(b), scoreRisk(a)))
            .limit(8)
            .toList();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("matched", matched);
        kpi.put("mismatched", mismatched);
        kpi.put("pending", pending);
        kpi.put("highRisk", highRisk);
        kpi.put("matchRate", total == 0 ? 0 : Math.round((matched * 10000.0 / total)) / 100.0);

        List<Map<String, Object>> typeStats = toCounterList(typeCounter);
        List<Map<String, Object>> statusStats = toCounterList(statusCounter);
        List<Map<String, Object>> timeline = dayCounter.entrySet().stream().map(entry -> {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("day", entry.getKey());
            node.put("count", entry.getValue());
            return node;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", kpi);
        result.put("typeStats", typeStats);
        result.put("statusStats", statusStats);
        result.put("timeline", timeline);
        result.put("riskQueue", riskQueue);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:edit')")
    @GetMapping("/smart/verify/{verifyId}")
    public AjaxResult smartVerify(@PathVariable("verifyId") Long verifyId)
    {
        AgriDataAttestationVerify record = agriDataAttestationVerifyService.selectAgriDataAttestationVerifyByVerifyId(verifyId);
        if (record == null)
        {
            return error("校验记录不存在");
        }

        String origin = normalizeHash(record.getOriginHash());
        String chain = normalizeHash(record.getChainHash());

        boolean exactMatch = origin.equals(chain) && !origin.isEmpty();
        double similarity = hashSimilarity(origin, chain);
        String inferredStatus = exactMatch ? "1" : (similarity >= 0.85 ? "0" : "2");
        String riskLevel = exactMatch ? "低" : ("0".equals(inferredStatus) ? "中" : "高");

        List<String> hints = new ArrayList<>();
        if (exactMatch)
        {
            hints.add("原始哈希与链上哈希完全一致，可判定通过");
        }
        else if ("0".equals(inferredStatus))
        {
            hints.add("哈希相似度较高但未完全一致，建议重新触发上链校验任务");
        }
        else
        {
            hints.add("哈希差异明显，建议立即核查数据源与链上交易记录");
        }

        String summary = "完成链上链下哈希一致性检测，建议状态为" + inferredStatus + "。";
        String aiOriginalExcerpt = null;
        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "数据存证一致性智能校验");
            context.put("verifyId", record.getVerifyId());
            context.put("attestationNo", record.getAttestationNo());
            context.put("batchNo", record.getBatchNo());
            context.put("dataType", record.getDataType());
            context.put("verifyStatus", record.getVerifyStatus());
            context.put("originHash", origin);
            context.put("chainHash", chain);
            context.put("exactMatch", exactMatch);
            context.put("similarity", similarity);
            context.put("ruleInferredStatus", inferredStatus);
            context.put("ruleRiskLevel", riskLevel);
            context.put("ruleHints", hints);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("数据存证一致性智能校验", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (aiResult.getInsightSummary() != null && !aiResult.getInsightSummary().isEmpty())
            {
                summary = aiResult.getInsightSummary();
            }
            if (aiResult.getRiskLevel() != null && !aiResult.getRiskLevel().isEmpty())
            {
                riskLevel = aiResult.getRiskLevel();
            }
            if (aiResult.getSuggestion() != null && !aiResult.getSuggestion().isEmpty())
            {
                hints.add(0, "AI建议：" + aiResult.getSuggestion());
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("verifyId", record.getVerifyId());
        result.put("attestationNo", record.getAttestationNo());
        result.put("algorithm", "hash-integrity-v1");
        result.put("exactMatch", exactMatch);
        result.put("similarity", similarity);
        result.put("inferredStatus", inferredStatus);
        result.put("riskLevel", riskLevel);
        result.put("hints", hints);
        result.put("summary", summary);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:edit')")
    @Log(title = "数据存证与校验", businessType = BusinessType.UPDATE)
    @PostMapping("/smart/resolve/{verifyId}")
    public AjaxResult smartResolve(@PathVariable("verifyId") Long verifyId, @RequestBody(required = false) Map<String, Object> payload)
    {
        AgriDataAttestationVerify record = agriDataAttestationVerifyService.selectAgriDataAttestationVerifyByVerifyId(verifyId);
        if (record == null)
        {
            return error("校验记录不存在");
        }

        String action = payload == null ? "pass" : defaultValue((String) payload.get("action"), "pass");
        String note = payload == null ? "" : defaultValue((String) payload.get("note"), "");
        Date now = DateUtils.getNowDate();
        record.setVerifyTime(now);
        record.setVerifyByUser(getUsername());
        record.setUpdateBy(getUsername());

        if ("manualReview".equalsIgnoreCase(action))
        {
            record.setVerifyStatus("0");
            record.setRemark(mergeRemark(record.getRemark(), "转人工复核", note));
        }
        else
        {
            record.setVerifyStatus("1");
            if (record.getChainHash() == null || record.getChainHash().trim().isEmpty())
            {
                record.setChainHash(record.getOriginHash());
            }
            record.setRemark(mergeRemark(record.getRemark(), "闭环通过", note));
        }

        agriDataAttestationVerifyService.updateAgriDataAttestationVerify(record);
        Map<String, Object> result = new HashMap<>();
        result.put("verifyId", record.getVerifyId());
        result.put("verifyStatus", record.getVerifyStatus());
        result.put("verifyByUser", record.getVerifyByUser());
        result.put("verifyTime", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, now));
        result.put("message", "处置完成");
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:export')")
    @Log(title = "数据存证与校验", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriDataAttestationVerify agriDataAttestationVerify)
    {
        ExcelUtil<AgriDataAttestationVerify> util = new ExcelUtil<>(AgriDataAttestationVerify.class);
        util.exportExcel(response, agriDataAttestationVerifyService.selectAgriDataAttestationVerifyList(agriDataAttestationVerify), "数据存证与校验数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:query')")
    @GetMapping(value = "/{verifyId}")
    public AjaxResult getInfo(@PathVariable("verifyId") Long verifyId)
    {
        return success(agriDataAttestationVerifyService.selectAgriDataAttestationVerifyByVerifyId(verifyId));
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:add')")
    @Log(title = "数据存证与校验", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriDataAttestationVerify agriDataAttestationVerify)
    {
        return toAjax(agriDataAttestationVerifyService.insertAgriDataAttestationVerify(agriDataAttestationVerify));
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:edit')")
    @Log(title = "数据存证与校验", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriDataAttestationVerify agriDataAttestationVerify)
    {
        return toAjax(agriDataAttestationVerifyService.updateAgriDataAttestationVerify(agriDataAttestationVerify));
    }

    @PreAuthorize("@ss.hasPermi('agri:attestationVerify:remove')")
    @Log(title = "数据存证与校验", businessType = BusinessType.DELETE)
    @DeleteMapping("/{verifyIds}")
    public AjaxResult remove(@PathVariable Long[] verifyIds)
    {
        return toAjax(agriDataAttestationVerifyService.deleteAgriDataAttestationVerifyByVerifyIds(verifyIds));
    }

    private String normalizeHash(String value)
    {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private double hashSimilarity(String a, String b)
    {
        if (a.isEmpty() || b.isEmpty())
        {
            return 0;
        }
        int len = Math.max(a.length(), b.length());
        int same = 0;
        for (int i = 0; i < Math.min(a.length(), b.length()); i++)
        {
            if (a.charAt(i) == b.charAt(i))
            {
                same++;
            }
        }
        return Math.round((same * 1.0 / len) * 10000d) / 10000d;
    }

    private int scoreRisk(AgriDataAttestationVerify item)
    {
        String origin = normalizeHash(item.getOriginHash());
        String chain = normalizeHash(item.getChainHash());
        if ("2".equals(item.getVerifyStatus()))
        {
            return 95;
        }
        if (origin.isEmpty() || chain.isEmpty())
        {
            return 85;
        }
        if ("0".equals(item.getVerifyStatus()))
        {
            return 65;
        }
        return 20;
    }

    private List<Map<String, Object>> toCounterList(Map<String, Integer> counter)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counter.entrySet())
        {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("name", entry.getKey());
            node.put("value", entry.getValue());
            list.add(node);
        }
        return list;
    }

    private String statusLabel(String status)
    {
        if ("1".equals(status))
        {
            return "一致";
        }
        if ("2".equals(status))
        {
            return "不一致";
        }
        return "待校验";
    }

    private String defaultValue(String value, String fallback)
    {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String mergeRemark(String oldRemark, String stage, String note)
    {
        String prefix = oldRemark == null || oldRemark.trim().isEmpty() ? "" : oldRemark.trim() + "；";
        String extra = note == null || note.trim().isEmpty() ? "" : "（" + note.trim() + "）";
        return prefix + stage + extra;
    }
}
