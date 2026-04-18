package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriThirdPartyApiAccess;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriThirdPartyApiAccessService;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
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
 * 第三方API接入Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/thirdApi")
public class AgriThirdPartyApiAccessController extends BaseController
{
    @Autowired
    private IAgriThirdPartyApiAccessService agriThirdPartyApiAccessService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        startPage();
        return getDataTable(agriThirdPartyApiAccessService.selectAgriThirdPartyApiAccessList(agriThirdPartyApiAccess));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard(AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        return success(buildDashboard(agriThirdPartyApiAccess));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:query')")
    @GetMapping("/smart/advice/{accessId}")
    public AjaxResult advice(@PathVariable("accessId") Long accessId)
    {
        AgriThirdPartyApiAccess access = agriThirdPartyApiAccessService.selectAgriThirdPartyApiAccessByAccessId(accessId);
        if (access == null)
        {
            return error("接入配置不存在");
        }
        return success(buildAdvice(access));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:export')")
    @Log(title = "第三方API接入", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        ExcelUtil<AgriThirdPartyApiAccess> util = new ExcelUtil<>(AgriThirdPartyApiAccess.class);
        util.exportExcel(response, agriThirdPartyApiAccessService.selectAgriThirdPartyApiAccessList(agriThirdPartyApiAccess), "第三方API接入数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:query')")
    @GetMapping(value = "/{accessId}")
    public AjaxResult getInfo(@PathVariable("accessId") Long accessId)
    {
        return success(agriThirdPartyApiAccessService.selectAgriThirdPartyApiAccessByAccessId(accessId));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:add')")
    @Log(title = "第三方API接入", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        return toAjax(agriThirdPartyApiAccessService.insertAgriThirdPartyApiAccess(agriThirdPartyApiAccess));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:edit')")
    @Log(title = "第三方API接入", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriThirdPartyApiAccess agriThirdPartyApiAccess)
    {
        return toAjax(agriThirdPartyApiAccessService.updateAgriThirdPartyApiAccess(agriThirdPartyApiAccess));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:remove')")
    @Log(title = "第三方API接入", businessType = BusinessType.DELETE)
    @DeleteMapping("/{accessIds}")
    public AjaxResult remove(@PathVariable Long[] accessIds)
    {
        return toAjax(agriThirdPartyApiAccessService.deleteAgriThirdPartyApiAccessByAccessIds(accessIds));
    }

    @PreAuthorize("@ss.hasPermi('agri:thirdApi:edit')")
    @Log(title = "第三方API探活", businessType = BusinessType.UPDATE)
    @PostMapping("/probe/{accessId}")
    public AjaxResult probe(@PathVariable("accessId") Long accessId)
    {
        AgriThirdPartyApiAccess access = agriThirdPartyApiAccessService.selectAgriThirdPartyApiAccessByAccessId(accessId);
        if (access == null)
        {
            return error("接入配置不存在");
        }

        try
        {
            String apiType = access.getApiType();
            if ("weather".equalsIgnoreCase(apiType))
            {
                return probeWeather(access);
            }
            if ("map".equalsIgnoreCase(apiType) || "amap".equalsIgnoreCase(apiType))
            {
                return probeMap(access);
            }

            AgriHttpIntegrationClient.ThirdApiResult result =
                agriHttpIntegrationClient.probe(access.getEndpointUrl(), access.getTimeoutSec());
            access.setCallStatus(result.isSuccess() ? "1" : "2");
            access.setSuccessRate(result.isSuccess() ? BigDecimal.valueOf(100) : BigDecimal.ZERO);
            access.setLastCallTime(DateUtils.getNowDate());
            access.setRemark("HTTP " + result.getHttpStatus() + " | " + result.getResponseSnippet());
            agriThirdPartyApiAccessService.updateAgriThirdPartyApiAccess(access);
            return result.isSuccess() ? success("探活成功") : error("探活失败，HTTP状态: " + result.getHttpStatus());
        }
        catch (Exception ex)
        {
            access.setCallStatus("2");
            access.setLastCallTime(DateUtils.getNowDate());
            access.setSuccessRate(BigDecimal.ZERO);
            access.setRemark("探活异常: " + ex.getMessage());
            agriThirdPartyApiAccessService.updateAgriThirdPartyApiAccess(access);
            return error("探活异常: " + ex.getMessage());
        }
    }

    private AjaxResult probeWeather(AgriThirdPartyApiAccess access)
    {
        try
        {
            AgriHttpIntegrationClient.WeatherProbeResult result =
                agriHttpIntegrationClient.probeWeather(access.getEndpointUrl(), access.getTimeoutSec());
            access.setCallStatus(result.isSuccess() ? "1" : "2");
            access.setSuccessRate(result.isSuccess() ? BigDecimal.valueOf(100) : BigDecimal.ZERO);
            access.setLastCallTime(DateUtils.getNowDate());
            String remark = "HTTP " + result.getHttpStatus();
            if (result.getWeatherSummary() != null)
            {
                remark = remark + " | " + result.getWeatherSummary();
            }
            if (result.getResponseSnippet() != null)
            {
                remark = remark + " | " + result.getResponseSnippet();
            }
            access.setRemark(remark);
            agriThirdPartyApiAccessService.updateAgriThirdPartyApiAccess(access);
            return result.isSuccess() ? success("天气探活成功") : error("天气探活失败，HTTP状态: " + result.getHttpStatus());
        }
        catch (Exception ex)
        {
            access.setCallStatus("2");
            access.setLastCallTime(DateUtils.getNowDate());
            access.setSuccessRate(BigDecimal.ZERO);
            access.setRemark("天气探活异常: " + ex.getMessage());
            agriThirdPartyApiAccessService.updateAgriThirdPartyApiAccess(access);
            return error("天气探活异常: " + ex.getMessage());
        }
    }

    private AjaxResult probeMap(AgriThirdPartyApiAccess access)
    {
        try
        {
            AgriHttpIntegrationClient.MapProbeResult result =
                agriHttpIntegrationClient.probeMap(access.getEndpointUrl(), access.getTimeoutSec());
            access.setCallStatus(result.isSuccess() ? "1" : "2");
            access.setSuccessRate(result.isSuccess() ? BigDecimal.valueOf(100) : BigDecimal.ZERO);
            access.setLastCallTime(DateUtils.getNowDate());
            String remark = "HTTP " + result.getHttpStatus();
            if (result.getMapSummary() != null)
            {
                remark = remark + " | " + result.getMapSummary();
            }
            if (result.getResponseSnippet() != null)
            {
                remark = remark + " | " + result.getResponseSnippet();
            }
            access.setRemark(remark);
            agriThirdPartyApiAccessService.updateAgriThirdPartyApiAccess(access);
            return result.isSuccess() ? success("地图探活成功") : error("地图探活失败，HTTP状态: " + result.getHttpStatus());
        }
        catch (Exception ex)
        {
            access.setCallStatus("2");
            access.setLastCallTime(DateUtils.getNowDate());
            access.setSuccessRate(BigDecimal.ZERO);
            access.setRemark("地图探活异常: " + ex.getMessage());
            agriThirdPartyApiAccessService.updateAgriThirdPartyApiAccess(access);
            return error("地图探活异常: " + ex.getMessage());
        }
    }

    private Map<String, Object> buildDashboard(AgriThirdPartyApiAccess query)
    {
        List<AgriThirdPartyApiAccess> accesses = agriThirdPartyApiAccessService.selectAgriThirdPartyApiAccessList(query);
        int successCount = 0;
        int failedCount = 0;
        int activeCount = 0;
        List<AgriThirdPartyApiAccess> sorted = new ArrayList<>(accesses);
        sorted.sort(Comparator.comparing(AgriThirdPartyApiAccess::getLastCallTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Map<String, Object>> recentRows = new ArrayList<>();
        for (AgriThirdPartyApiAccess access : accesses)
        {
            if (access == null)
            {
                continue;
            }
            if ("1".equals(access.getCallStatus()))
            {
                successCount++;
            }
            else if ("2".equals(access.getCallStatus()))
            {
                failedCount++;
            }
            else
            {
                activeCount++;
            }
        }

        for (AgriThirdPartyApiAccess access : sorted)
        {
            if (recentRows.size() >= 6)
            {
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("accessId", access.getAccessId());
            row.put("accessCode", access.getAccessCode());
            row.put("accessName", access.getAccessName());
            row.put("apiType", access.getApiType());
            row.put("provider", access.getProvider());
            row.put("successRate", access.getSuccessRate());
            row.put("callStatus", access.getCallStatus());
            row.put("lastCallTime", access.getLastCallTime());
            recentRows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", accesses.size());
        summary.put("successCount", successCount);
        summary.put("failedCount", failedCount);
        summary.put("idleCount", activeCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("recentRows", recentRows);
        return result;
    }

    private Map<String, Object> buildAdvice(AgriThirdPartyApiAccess access)
    {
        int score = 100;
        List<String> suggestions = new ArrayList<>();
        if (access.getTimeoutSec() != null && access.getTimeoutSec() > 60)
        {
            suggestions.add("超时时间偏长，建议按接口类型压缩到 30 秒以内");
            score -= 10;
        }
        if (access.getSuccessRate() != null && access.getSuccessRate().compareTo(BigDecimal.valueOf(80)) < 0)
        {
            suggestions.add("成功率偏低，建议先执行探活并检查鉴权、URL 和超时参数");
            score -= 25;
        }
        if ("weather".equalsIgnoreCase(access.getApiType()))
        {
            suggestions.add("天气接口建议配置真实 Key 后再进入实网调用，当前可先用探活校验返回体");
        }
        else if ("map".equalsIgnoreCase(access.getApiType()) || "amap".equalsIgnoreCase(access.getApiType()))
        {
            suggestions.add("地图接口建议检查签名参数、location 与出发到达点位是否完整");
        }
        else
        {
            suggestions.add("通用接口建议补充标准请求示例和健康检查路径");
        }
        if (access.getEndpointUrl() == null || access.getEndpointUrl().trim().isEmpty())
        {
            suggestions.add("请求地址缺失，建议先补齐目标 URL 再保存");
            score -= 20;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accessId", access.getAccessId());
        result.put("riskScore", Math.max(0, score));
        result.put("riskLevel", score >= 85 ? "低" : score >= 70 ? "中" : "高");
        result.put("suggestions", suggestions);
        result.put("access", access);
        return result;
    }
}
