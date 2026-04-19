package com.ruoyi.web.controller.agri;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriSupplyChainContract;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriSupplyChainContractService;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
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
 * 供应链金融合约管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/supplyContract")
public class AgriSupplyChainContractController extends BaseController
{
    @Autowired
    private IAgriSupplyChainContractService agriSupplyChainContractService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriSupplyChainContract agriSupplyChainContract)
    {
        startPage();
        return getDataTable(agriSupplyChainContractService.selectAgriSupplyChainContractList(agriSupplyChainContract));
    }

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriSupplyChainContract> list = agriSupplyChainContractService.selectAgriSupplyChainContractList(new AgriSupplyChainContract());
        int total = list.size();
        int activeCount = 0;
        int highRiskCount = 0;
        int expiringSoonCount = 0;
        BigDecimal amountSum = BigDecimal.ZERO;
        LocalDate now = LocalDate.now();

        for (AgriSupplyChainContract item : list)
        {
            amountSum = amountSum.add(defaultZero(item.getFinanceAmount()));
            if ("1".equals(item.getContractStatus()))
            {
                activeCount++;
            }
            if ("H".equalsIgnoreCase(item.getRiskLevel()) || "C".equalsIgnoreCase(item.getRiskLevel()))
            {
                highRiskCount++;
            }
            if (item.getEndDate() != null)
            {
                long days = ChronoUnit.DAYS.between(now, item.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                if (days >= 0 && days <= 30)
                {
                    expiringSoonCount++;
                }
            }
        }

        List<AgriSupplyChainContract> topList = list.stream()
            .sorted(Comparator.comparing((AgriSupplyChainContract c) -> defaultZero(c.getFinanceAmount())).reversed())
            .limit(6)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("activeCount", activeCount);
        kpi.put("highRiskCount", highRiskCount);
        kpi.put("expiringSoonCount", expiringSoonCount);
        kpi.put("amountSum", amountSum.setScale(2, RoundingMode.HALF_UP));
        result.put("kpi", kpi);
        result.put("topList", topList);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriSupplyChainContract> list = agriSupplyChainContractService.selectAgriSupplyChainContractList(new AgriSupplyChainContract());
        int total = list.size();
        int activeCount = 0;
        int signingCount = 0;
        int maturityCount = 0;
        int highRiskCount = 0;
        BigDecimal amountSum = BigDecimal.ZERO;
        Map<String, Integer> statusBucket = new LinkedHashMap<>();
        statusBucket.put("0", 0);
        statusBucket.put("1", 0);
        statusBucket.put("2", 0);
        statusBucket.put("3", 0);

        LocalDate now = LocalDate.now();
        for (AgriSupplyChainContract item : list)
        {
            amountSum = amountSum.add(defaultZero(item.getFinanceAmount()));
            String status = item.getContractStatus() == null ? "0" : item.getContractStatus();
            statusBucket.put(status, statusBucket.getOrDefault(status, 0) + 1);
            if ("1".equals(status))
            {
                activeCount++;
            }
            if ("0".equals(status))
            {
                signingCount++;
            }
            if ("2".equals(status) || "3".equals(status))
            {
                maturityCount++;
            }
            if ("H".equalsIgnoreCase(item.getRiskLevel()) || "C".equalsIgnoreCase(item.getRiskLevel()))
            {
                highRiskCount++;
            }
        }

        List<AgriSupplyChainContract> pressureQueue = list.stream()
            .sorted(Comparator.comparing((AgriSupplyChainContract c) -> defaultZero(c.getFinanceAmount())).reversed())
            .limit(8)
            .toList();

        List<Map<String, Object>> alerts = list.stream()
            .sorted((left, right) -> compareUrgency(left, right, now))
            .limit(10)
            .map(item -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("contractId", item.getContractId());
                row.put("contractNo", item.getContractNo());
                row.put("contractName", item.getContractName());
                row.put("financeSubject", item.getFinanceSubject());
                row.put("financeAmount", defaultZero(item.getFinanceAmount()));
                row.put("interestRate", defaultZero(item.getInterestRate()));
                row.put("contractStatus", item.getContractStatus());
                row.put("riskLevel", item.getRiskLevel());
                row.put("endDate", item.getEndDate());
                row.put("remainDays", remainDays(item, now));
                return row;
            })
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("activeCount", activeCount);
        kpi.put("signingCount", signingCount);
        kpi.put("maturityCount", maturityCount);
        kpi.put("highRiskCount", highRiskCount);
        kpi.put("amountSum", amountSum.setScale(2, RoundingMode.HALF_UP));
        result.put("kpi", kpi);
        result.put("statusBucket", statusBucket);
        result.put("pressureQueue", pressureQueue);
        result.put("alerts", alerts);
        result.put("suggestions", buildContractSuggestions(activeCount, maturityCount, highRiskCount, amountSum));
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:edit')")
    @GetMapping("/smart/assess/{contractId}")
    public AjaxResult smartAssess(@PathVariable("contractId") Long contractId)
    {
        AgriSupplyChainContract contract = agriSupplyChainContractService.selectAgriSupplyChainContractByContractId(contractId);
        if (contract == null)
        {
            return error("合约不存在");
        }

        BigDecimal amount = defaultZero(contract.getFinanceAmount());
        BigDecimal rate = defaultZero(contract.getInterestRate());
        BigDecimal annualInterest = amount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        long remainDays = -1;
        if (contract.getEndDate() != null)
        {
            remainDays = ChronoUnit.DAYS.between(LocalDate.now(), contract.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        BigDecimal pressureIndex = rate.multiply(new BigDecimal("5"));
        if (remainDays >= 0 && remainDays <= 30)
        {
            pressureIndex = pressureIndex.add(new BigDecimal("25"));
        }
        if ("H".equalsIgnoreCase(contract.getRiskLevel()) || "C".equalsIgnoreCase(contract.getRiskLevel()))
        {
            pressureIndex = pressureIndex.add(new BigDecimal("20"));
        }
        pressureIndex = pressureIndex.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

        String riskBand = pressureIndex.compareTo(new BigDecimal("80")) >= 0 ? "C" :
            (pressureIndex.compareTo(new BigDecimal("60")) >= 0 ? "H" :
                (pressureIndex.compareTo(new BigDecimal("35")) >= 0 ? "M" : "L"));

        List<String> actions = new ArrayList<>();
        if (remainDays >= 0 && remainDays <= 30)
        {
            actions.add("合约即将到期，建议提前完成续约或清偿安排");
        }
        if (rate.compareTo(new BigDecimal("12")) > 0)
        {
            actions.add("利率偏高，建议评估再融资与担保优化方案");
        }
        if (actions.isEmpty())
        {
            actions.add("当前现金流压力可控，建议继续按月跟踪履约进度");
        }

        String summary = "根据融资规模、利率与到期窗口完成现金流压力评估，综合风险等级为" + riskBand + "。";
        String aiOriginalExcerpt = null;
        try
        {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("scene", "供应链合约智能评估");
            context.put("contractId", contract.getContractId());
            context.put("contractNo", contract.getContractNo());
            context.put("contractName", contract.getContractName());
            context.put("financeSubject", contract.getFinanceSubject());
            context.put("financeAmount", amount);
            context.put("interestRate", rate);
            context.put("contractStatus", contract.getContractStatus());
            context.put("riskLevel", contract.getRiskLevel());
            context.put("startDate", contract.getStartDate());
            context.put("endDate", contract.getEndDate());
            context.put("remainDays", remainDays);
            context.put("annualInterest", annualInterest);
            context.put("rulePressureIndex", pressureIndex);
            context.put("ruleRiskBand", riskBand);
            context.put("ruleActions", actions);
            AgriHttpIntegrationClient.GeneralInsightResult aiResult = agriHttpIntegrationClient.invokeGeneralInsight("供应链合约智能评估", JSON.toJSONString(context));
            aiOriginalExcerpt = aiResult.getRawContent();
            if (aiResult.getInsightSummary() != null && !aiResult.getInsightSummary().isEmpty())
            {
                summary = aiResult.getInsightSummary();
            }
            if (aiResult.getRiskLevel() != null && !aiResult.getRiskLevel().isEmpty())
            {
                riskBand = aiResult.getRiskLevel();
            }
            if (aiResult.getSuggestion() != null && !aiResult.getSuggestion().isEmpty())
            {
                actions.add(0, "AI建议：" + aiResult.getSuggestion());
            }
            if (aiOriginalExcerpt != null && !aiOriginalExcerpt.isEmpty())
            {
                actions.add("AI原文摘录：" + aiOriginalExcerpt);
            }
        }
        catch (Exception ignore)
        {
            // keep rule-based fallback when AI is unavailable
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contractId", contract.getContractId());
        result.put("contractNo", contract.getContractNo());
        result.put("algorithm", "cashflow-pressure-v1");
        result.put("annualInterest", annualInterest);
        result.put("remainDays", remainDays);
        result.put("pressureIndex", pressureIndex);
        result.put("riskBand", riskBand);
        result.put("actions", actions);
        result.put("summary", summary);
        result.put("aiOriginalExcerpt", aiOriginalExcerpt);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:export')")
    @Log(title = "供应链金融合约管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriSupplyChainContract agriSupplyChainContract)
    {
        ExcelUtil<AgriSupplyChainContract> util = new ExcelUtil<>(AgriSupplyChainContract.class);
        util.exportExcel(response, agriSupplyChainContractService.selectAgriSupplyChainContractList(agriSupplyChainContract), "供应链金融合约数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:query')")
    @GetMapping(value = "/{contractId}")
    public AjaxResult getInfo(@PathVariable("contractId") Long contractId)
    {
        return success(agriSupplyChainContractService.selectAgriSupplyChainContractByContractId(contractId));
    }

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:add')")
    @Log(title = "供应链金融合约管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriSupplyChainContract agriSupplyChainContract)
    {
        return toAjax(agriSupplyChainContractService.insertAgriSupplyChainContract(agriSupplyChainContract));
    }

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:edit')")
    @Log(title = "供应链金融合约管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriSupplyChainContract agriSupplyChainContract)
    {
        return toAjax(agriSupplyChainContractService.updateAgriSupplyChainContract(agriSupplyChainContract));
    }

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:remove')")
    @Log(title = "供应链金融合约管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{contractIds}")
    public AjaxResult remove(@PathVariable Long[] contractIds)
    {
        return toAjax(agriSupplyChainContractService.deleteAgriSupplyChainContractByContractIds(contractIds));
    }

    private BigDecimal defaultZero(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long remainDays(AgriSupplyChainContract contract, LocalDate now)
    {
        if (contract.getEndDate() == null)
        {
            return Long.MIN_VALUE;
        }
        return ChronoUnit.DAYS.between(now, contract.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private int compareUrgency(AgriSupplyChainContract left, AgriSupplyChainContract right, LocalDate now)
    {
        long leftDays = remainDays(left, now);
        long rightDays = remainDays(right, now);
        boolean leftHighRisk = "H".equalsIgnoreCase(left.getRiskLevel()) || "C".equalsIgnoreCase(left.getRiskLevel());
        boolean rightHighRisk = "H".equalsIgnoreCase(right.getRiskLevel()) || "C".equalsIgnoreCase(right.getRiskLevel());
        if (leftHighRisk != rightHighRisk)
        {
            return leftHighRisk ? -1 : 1;
        }
        if (leftDays != rightDays)
        {
            return Long.compare(leftDays, rightDays);
        }
        return defaultZero(right.getFinanceAmount()).compareTo(defaultZero(left.getFinanceAmount()));
    }

    private List<String> buildContractSuggestions(int activeCount, int maturityCount, int highRiskCount, BigDecimal amountSum)
    {
        List<String> suggestions = new ArrayList<>();
        if (highRiskCount > 0)
        {
            suggestions.add("存在高风险合约，建议优先复核担保、回款与风控等级");
        }
        if (maturityCount > 0)
        {
            suggestions.add("存在临近到期合约，建议同步续约、展期或清偿方案");
        }
        if (amountSum.compareTo(new BigDecimal("1000000")) >= 0)
        {
            suggestions.add("当前融资规模较高，建议按主体维度拆分查看暴露敞口");
        }
        if (activeCount == 0)
        {
            suggestions.add("暂无生效中的合约，建议尽快推进签约后续执行链路");
        }
        if (suggestions.isEmpty())
        {
            suggestions.add("当前合约结构稳定，建议维持月度复盘和到期提醒机制");
        }
        return suggestions;
    }
}
