package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriSmartContractDeploy;
import com.ruoyi.system.service.IAgriSmartContractDeployService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能合约部署Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/smartContract")
public class AgriSmartContractDeployController extends BaseController
{
    @Autowired
    private IAgriSmartContractDeployService agriSmartContractDeployService;

    @PreAuthorize("@ss.hasPermi('agri:smartContract:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriSmartContractDeploy agriSmartContractDeploy)
    {
        startPage();
        List<AgriSmartContractDeploy> list = agriSmartContractDeployService.selectAgriSmartContractDeployList(agriSmartContractDeploy);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:smartContract:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriSmartContractDeploy> list = agriSmartContractDeployService.selectAgriSmartContractDeployList(new AgriSmartContractDeploy());
        int total = list.size();
        int deployed = 0;
        int failed = 0;
        int pending = 0;
        for (AgriSmartContractDeploy item : list)
        {
            if ("1".equals(item.getDeployStatus()))
            {
                deployed++;
            }
            else if ("2".equals(item.getDeployStatus()))
            {
                failed++;
            }
            else
            {
                pending++;
            }
        }

        List<AgriSmartContractDeploy> focus = list.stream()
            .sorted(Comparator.comparing(AgriSmartContractDeploy::getDeployTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(8)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("deployed", deployed);
        kpi.put("failed", failed);
        kpi.put("pending", pending);
        kpi.put("deployRate", total == 0 ? 0 : deployed * 100.0 / total);
        result.put("kpi", kpi);
        result.put("focus", focus);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:smartContract:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriSmartContractDeploy> list = agriSmartContractDeployService.selectAgriSmartContractDeployList(new AgriSmartContractDeploy());
        List<AgriSmartContractDeploy> sortedList = list.stream()
            .sorted(Comparator.comparing(AgriSmartContractDeploy::getDeployTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

        int total = sortedList.size();
        int deployed = 0;
        int failed = 0;
        int pending = 0;
        int missingAbi = 0;
        int missingAddress = 0;

        Map<String, Integer> platformDistribution = new LinkedHashMap<>();
        Map<String, Integer> statusDistribution = new LinkedHashMap<>();

        for (AgriSmartContractDeploy item : sortedList)
        {
            if ("1".equals(item.getDeployStatus()))
            {
                deployed++;
            }
            else if ("2".equals(item.getDeployStatus()))
            {
                failed++;
            }
            else
            {
                pending++;
            }

            if (StringUtils.isEmpty(item.getAbiJson()))
            {
                missingAbi++;
            }
            if (StringUtils.isEmpty(item.getContractAddress()))
            {
                missingAddress++;
            }

            platformDistribution.merge(StringUtils.defaultIfEmpty(item.getChainPlatform(), "UNKNOWN"), 1, Integer::sum);
            statusDistribution.merge(statusLabel(item.getDeployStatus()), 1, Integer::sum);
        }

        List<Map<String, Object>> pressureQueue = sortedList.stream()
            .map(this::toPressureItem)
            .sorted(Comparator.comparingInt((Map<String, Object> o) -> (Integer) o.get("riskScore")).reversed())
            .limit(8)
            .collect(Collectors.toList());

        Map<String, Object> focus = pressureQueue.isEmpty() ? new LinkedHashMap<>() : pressureQueue.get(0);
        List<Map<String, Object>> suggestions = buildOpsSuggestions(pending, failed, missingAbi, missingAddress, total);

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("deployed", deployed);
        kpi.put("failed", failed);
        kpi.put("pending", pending);
        kpi.put("deployRate", total == 0 ? 0 : deployed * 100.0 / total);
        kpi.put("missingAbi", missingAbi);
        kpi.put("missingContractAddress", missingAddress);

        result.put("kpi", kpi);
        result.put("pressureQueue", pressureQueue);
        result.put("focus", focus);
        result.put("suggestions", suggestions);
        result.put("platformDistribution", toDistributionList(platformDistribution));
        result.put("statusDistribution", toDistributionList(statusDistribution));
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:smartContract:edit')")
    @GetMapping("/smart/security/{deployId}")
    public AjaxResult smartSecurity(@PathVariable("deployId") Long deployId)
    {
        AgriSmartContractDeploy deploy = agriSmartContractDeployService.selectAgriSmartContractDeployByDeployId(deployId);
        if (deploy == null)
        {
            return error("合约部署任务不存在");
        }

        int score = 90;
        List<String> findings = new ArrayList<>();
        if (StringUtils.isEmpty(deploy.getSourceHash()) || deploy.getSourceHash().length() < 16)
        {
            score -= 30;
            findings.add("源码哈希异常");
        }
        if (StringUtils.isEmpty(deploy.getAbiJson()))
        {
            score -= 25;
            findings.add("ABI缺失");
        }
        if (!"1".equals(deploy.getDeployStatus()))
        {
            score -= 20;
            findings.add("尚未完成部署");
        }
        if (StringUtils.isEmpty(deploy.getContractAddress()))
        {
            score -= 15;
            findings.add("缺少合约地址");
        }
        score = Math.max(score, 0);
        String level = score < 50 ? "高" : (score < 80 ? "中" : "低");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deployId", deploy.getDeployId());
        result.put("algorithm", "contract-security-lite-v1");
        result.put("securityScore", score);
        result.put("riskLevel", level);
        result.put("findings", findings);
        result.put("summary", "完成合约部署安全体检，风险等级为" + level + "。");
        return success(result);
    }

    private Map<String, Object> toPressureItem(AgriSmartContractDeploy deploy)
    {
        int riskScore = 10;
        List<String> reasons = new ArrayList<>();
        if ("2".equals(deploy.getDeployStatus()))
        {
            riskScore += 45;
            reasons.add("部署失败");
        }
        if (!"1".equals(deploy.getDeployStatus()))
        {
            riskScore += 20;
            reasons.add("尚未部署成功");
        }
        if (StringUtils.isEmpty(deploy.getAbiJson()))
        {
            riskScore += 20;
            reasons.add("ABI缺失");
        }
        if (StringUtils.isEmpty(deploy.getContractAddress()))
        {
            riskScore += 15;
            reasons.add("缺少合约地址");
        }
        if (StringUtils.isEmpty(deploy.getSourceHash()) || deploy.getSourceHash().length() < 16)
        {
            riskScore += 10;
            reasons.add("源码哈希可信度不足");
        }
        riskScore = Math.min(riskScore, 100);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("deployId", deploy.getDeployId());
        item.put("contractName", deploy.getContractName());
        item.put("contractVersion", deploy.getContractVersion());
        item.put("chainPlatform", deploy.getChainPlatform());
        item.put("deployStatus", deploy.getDeployStatus());
        item.put("statusLabel", statusLabel(deploy.getDeployStatus()));
        item.put("contractAddress", deploy.getContractAddress());
        item.put("deployTxHash", deploy.getDeployTxHash());
        item.put("riskScore", riskScore);
        item.put("riskLevel", riskLevel(riskScore));
        item.put("riskReason", reasons.isEmpty() ? "部署状态健康" : String.join("；", reasons));
        item.put("summary", StringUtils.defaultIfEmpty(deploy.getRemark(), "建议持续执行合约版本和链平台一致性巡检。"));
        item.put("deployTime", deploy.getDeployTime());
        return item;
    }

    private List<Map<String, Object>> buildOpsSuggestions(int pending, int failed, int missingAbi, int missingAddress, int total)
    {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        if (failed > 0)
        {
            suggestions.add(suggestion("失败部署复盘", "高", "存在 " + failed + " 条失败记录，建议优先核验链平台连通性与Gas配置。"));
        }
        if (pending > 0)
        {
            suggestions.add(suggestion("发布窗口排程", "高", "当前待部署 " + pending + " 条，建议按业务窗口分批灰度并保留回滚策略。"));
        }
        if (missingAbi > 0 || missingAddress > 0)
        {
            suggestions.add(suggestion("元数据补齐", "中", "缺失ABI " + missingAbi + " 条，缺失合约地址 " + missingAddress + " 条，建议补齐后再进行链上验收。"));
        }
        if (suggestions.isEmpty())
        {
            suggestions.add(suggestion("执行版本巡检", "低", "当前 " + total + " 条部署记录整体稳定，建议持续执行版本一致性与权限最小化审计。"));
        }
        return suggestions;
    }

    private List<Map<String, Object>> toDistributionList(Map<String, Integer> source)
    {
        return source.entrySet().stream().map(entry -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            return item;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> suggestion(String title, String priority, String content)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("priority", priority);
        item.put("content", content);
        return item;
    }

    private String riskLevel(int score)
    {
        if (score >= 80)
        {
            return "高";
        }
        if (score >= 50)
        {
            return "中";
        }
        return "低";
    }

    private String statusLabel(String status)
    {
        if ("1".equals(status))
        {
            return "已部署";
        }
        if ("2".equals(status))
        {
            return "失败";
        }
        return "待部署";
    }

    @PreAuthorize("@ss.hasPermi('agri:smartContract:export')")
    @Log(title = "智能合约部署", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriSmartContractDeploy agriSmartContractDeploy)
    {
        List<AgriSmartContractDeploy> list = agriSmartContractDeployService.selectAgriSmartContractDeployList(agriSmartContractDeploy);
        ExcelUtil<AgriSmartContractDeploy> util = new ExcelUtil<>(AgriSmartContractDeploy.class);
        util.exportExcel(response, list, "智能合约部署数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:smartContract:query')")
    @GetMapping(value = "/{deployId}")
    public AjaxResult getInfo(@PathVariable("deployId") Long deployId)
    {
        return success(agriSmartContractDeployService.selectAgriSmartContractDeployByDeployId(deployId));
    }

    @PreAuthorize("@ss.hasPermi('agri:smartContract:add')")
    @Log(title = "智能合约部署", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriSmartContractDeploy agriSmartContractDeploy)
    {
        agriSmartContractDeploy.setCreateBy(getUsername());
        if (StringUtils.isEmpty(agriSmartContractDeploy.getDeployStatus()))
        {
            agriSmartContractDeploy.setDeployStatus("0");
        }
        return toAjax(agriSmartContractDeployService.insertAgriSmartContractDeploy(agriSmartContractDeploy));
    }

    @PreAuthorize("@ss.hasPermi('agri:smartContract:edit')")
    @Log(title = "智能合约部署", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriSmartContractDeploy agriSmartContractDeploy)
    {
        agriSmartContractDeploy.setUpdateBy(getUsername());
        return toAjax(agriSmartContractDeployService.updateAgriSmartContractDeploy(agriSmartContractDeploy));
    }

    @PreAuthorize("@ss.hasPermi('agri:smartContract:edit')")
    @Log(title = "智能合约执行部署", businessType = BusinessType.UPDATE)
    @PutMapping("/deploy/{deployId}")
    public AjaxResult deploy(@PathVariable("deployId") Long deployId)
    {
        AgriSmartContractDeploy contract = agriSmartContractDeployService.selectAgriSmartContractDeployByDeployId(deployId);
        if (contract == null)
        {
            return error("合约部署任务不存在");
        }
        contract.setDeployStatus("1");
        contract.setDeployTime(DateUtils.getNowDate());
        if (StringUtils.isEmpty(contract.getContractAddress()))
        {
            contract.setContractAddress("0x" + Long.toHexString(System.currentTimeMillis()) + "abcde");
        }
        if (StringUtils.isEmpty(contract.getDeployTxHash()))
        {
            contract.setDeployTxHash("0x" + Long.toHexString(System.nanoTime()) + Long.toHexString(deployId));
        }
        contract.setUpdateBy(getUsername());
        return toAjax(agriSmartContractDeployService.updateAgriSmartContractDeploy(contract));
    }

    @PreAuthorize("@ss.hasPermi('agri:smartContract:remove')")
    @Log(title = "智能合约部署", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deployIds}")
    public AjaxResult remove(@PathVariable Long[] deployIds)
    {
        return toAjax(agriSmartContractDeployService.deleteAgriSmartContractDeployByDeployIds(deployIds));
    }
}
