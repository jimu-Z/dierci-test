package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriDeviceAccessNode;
import com.ruoyi.system.service.IAgriDeviceAccessNodeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * 设备接入管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/deviceAccess")
public class AgriDeviceAccessNodeController extends BaseController
{
    @Autowired
    private IAgriDeviceAccessNodeService agriDeviceAccessNodeService;

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriDeviceAccessNode agriDeviceAccessNode)
    {
        startPage();
        List<AgriDeviceAccessNode> list = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeList(agriDeviceAccessNode);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriDeviceAccessNode> list = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeList(new AgriDeviceAccessNode());
        int total = list.size();
        int online = 0;
        int offline = 0;
        int abnormal = 0;
        int pending = 0;
        Map<String, Integer> protocolCount = new LinkedHashMap<>();
        Map<String, Integer> deviceTypeCount = new LinkedHashMap<>();
        List<Map<String, Object>> queue = new ArrayList<>();

        for (AgriDeviceAccessNode item : list)
        {
            if (item == null)
            {
                continue;
            }
            if ("1".equals(item.getAccessStatus()))
            {
                online++;
            }
            else if ("3".equals(item.getAccessStatus()))
            {
                abnormal++;
            }
            else if ("0".equals(item.getAccessStatus()))
            {
                pending++;
            }
            else
            {
                offline++;
            }

            protocolCount.put(resolveProtocolLabel(item.getProtocolType()), protocolCount.getOrDefault(resolveProtocolLabel(item.getProtocolType()), 0) + 1);
            deviceTypeCount.put(resolveDeviceTypeLabel(item.getDeviceType()), deviceTypeCount.getOrDefault(resolveDeviceTypeLabel(item.getDeviceType()), 0) + 1);
            queue.add(buildPressureNodeView(item));
        }

        queue.sort(Comparator.comparingInt((Map<String, Object> item) -> ((Number) item.get("riskScore")).intValue()).reversed()
            .thenComparing(item -> item.get("lastOnlineTime") == null ? "" : item.get("lastOnlineTime").toString(), Comparator.reverseOrder()));

        List<Map<String, Object>> pressureQueue = queue.stream().limit(8).toList();
        List<Map<String, Object>> warningList = queue.stream()
            .filter(item -> ((Number) item.get("riskScore")).intValue() >= 70)
            .limit(6)
            .toList();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("online", online);
        kpi.put("offline", offline);
        kpi.put("abnormal", abnormal);
        kpi.put("pending", pending);
        kpi.put("onlineRate", total == 0 ? 0 : roundPercent(online * 100.0 / total));
        kpi.put("activationRate", total == 0 ? 0 : roundPercent((total - pending) * 100.0 / total));
        kpi.put("highRiskCount", warningList.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpi", kpi);
        result.put("statusDistribution", buildStatusDistribution(total, online, offline, abnormal, pending));
        result.put("protocolDistribution", buildCountDistribution(protocolCount, total));
        result.put("deviceTypeDistribution", buildCountDistribution(deviceTypeCount, total));
        result.put("pressureQueue", pressureQueue);
        result.put("warningList", warningList);
        result.put("suggestions", buildOpsSuggestions(total, online, offline, abnormal, pending, warningList.size()));
        result.put("focusList", pressureQueue);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriDeviceAccessNode> list = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeList(new AgriDeviceAccessNode());
        int total = list.size();
        int online = 0;
        int offline = 0;
        int abnormal = 0;
        for (AgriDeviceAccessNode item : list)
        {
            if ("1".equals(item.getAccessStatus()))
            {
                online++;
            }
            else if ("3".equals(item.getAccessStatus()))
            {
                abnormal++;
            }
            else
            {
                offline++;
            }
        }

        List<AgriDeviceAccessNode> focusList = list.stream()
            .filter(x -> "2".equals(x.getAccessStatus()) || "3".equals(x.getAccessStatus()))
            .sorted(Comparator.comparing(AgriDeviceAccessNode::getUpdateTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(8)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("online", online);
        kpi.put("offline", offline);
        kpi.put("abnormal", abnormal);
        kpi.put("onlineRate", total == 0 ? 0 : online * 100.0 / total);
        result.put("kpi", kpi);
        result.put("focusList", focusList);
        return success(result);
    }

    private List<Map<String, Object>> buildStatusDistribution(int total, int online, int offline, int abnormal, int pending)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(buildDistributionItem("在线", online, total));
        result.add(buildDistributionItem("离线", offline, total));
        result.add(buildDistributionItem("异常", abnormal, total));
        result.add(buildDistributionItem("待接入", pending, total));
        return result;
    }

    private List<Map<String, Object>> buildCountDistribution(Map<String, Integer> countMap, int total)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet())
        {
            result.add(buildDistributionItem(entry.getKey(), entry.getValue(), total));
        }
        return result;
    }

    private Map<String, Object> buildDistributionItem(String label, int count, int total)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("count", count);
        item.put("rate", total == 0 ? 0 : roundPercent(count * 100.0 / total));
        return item;
    }

    private Map<String, Object> buildPressureNodeView(AgriDeviceAccessNode item)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        int riskScore = calculateRiskScore(item);
        view.put("nodeId", item.getNodeId());
        view.put("deviceCode", item.getDeviceCode());
        view.put("deviceName", item.getDeviceName());
        view.put("deviceType", item.getDeviceType());
        view.put("deviceTypeLabel", resolveDeviceTypeLabel(item.getDeviceType()));
        view.put("protocolType", item.getProtocolType());
        view.put("protocolLabel", resolveProtocolLabel(item.getProtocolType()));
        view.put("firmwareVersion", item.getFirmwareVersion());
        view.put("bindArea", item.getBindArea());
        view.put("accessStatus", item.getAccessStatus());
        view.put("statusLabel", resolveAccessStatusLabel(item.getAccessStatus()));
        view.put("lastOnlineTime", item.getLastOnlineTime());
        view.put("riskScore", riskScore);
        view.put("riskLevel", resolveRiskLevel(riskScore));
        view.put("riskReason", buildRiskReason(item));
        return view;
    }

    private int calculateRiskScore(AgriDeviceAccessNode item)
    {
        int score = 16;
        if (item == null)
        {
            return score;
        }
        if ("3".equals(item.getAccessStatus()))
        {
            score += 45;
        }
        else if ("2".equals(item.getAccessStatus()))
        {
            score += 28;
        }
        else if ("0".equals(item.getAccessStatus()))
        {
            score += 18;
        }

        if (item.getLastOnlineTime() == null)
        {
            score += 12;
        }
        if (item.getFirmwareVersion() == null || item.getFirmwareVersion().isBlank())
        {
            score += 10;
        }
        if (item.getBindArea() == null || item.getBindArea().isBlank())
        {
            score += 8;
        }
        if (item.getDeviceType() == null || item.getDeviceType().isBlank())
        {
            score += 8;
        }
        return Math.min(score, 100);
    }

    private List<String> buildRiskFactors(AgriDeviceAccessNode item)
    {
        List<String> factors = new ArrayList<>();
        if (item == null)
        {
            return factors;
        }
        if ("3".equals(item.getAccessStatus()))
        {
            factors.add("设备状态异常");
        }
        if ("2".equals(item.getAccessStatus()))
        {
            factors.add("设备当前离线");
        }
        if (item.getLastOnlineTime() == null)
        {
            factors.add("缺少最近在线时间");
        }
        if (item.getFirmwareVersion() == null || item.getFirmwareVersion().isBlank())
        {
            factors.add("固件版本未填写");
        }
        if (item.getBindArea() == null || item.getBindArea().isBlank())
        {
            factors.add("绑定区域未明确");
        }
        return factors;
    }

    private List<Map<String, Object>> buildOpsSuggestions(int total, int online, int offline, int abnormal, int pending, int highRiskCount)
    {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        suggestions.add(buildSuggestion("优先处置离线与异常节点", "当前离线" + offline + "台、异常" + abnormal + "台，建议优先补链路和排查接入异常。", "P0"));
        suggestions.add(buildSuggestion("补齐待接入清单", "待接入节点有" + pending + "台，可结合区域批量激活。", "P1"));
        suggestions.add(buildSuggestion("核对固件与区域", "建议检查固件版本与绑定区域是否完整，避免设备上线后缺少基础归档信息。", "P1"));
        if (highRiskCount > 0)
        {
            suggestions.add(buildSuggestion("锁定高压设备", "当前识别到" + highRiskCount + "台高压节点，请在运营台优先分配诊断责任人。", "P0"));
        }
        if (total == 0)
        {
            suggestions.add(buildSuggestion("先补设备台账", "当前暂无设备接入数据，请先导入基础台账或执行初始化。", "P1"));
        }
        if (online > 0)
        {
            suggestions.add(buildSuggestion("保持在线巡检", "在线设备有" + online + "台，建议保持最近在线时间滚动更新。", "P2"));
        }
        return suggestions;
    }

    private Map<String, Object> buildSuggestion(String title, String content, String priority)
    {
        Map<String, Object> suggestion = new LinkedHashMap<>();
        suggestion.put("title", title);
        suggestion.put("content", content);
        suggestion.put("priority", priority);
        return suggestion;
    }

    private String buildRiskReason(AgriDeviceAccessNode item)
    {
        List<String> reasons = buildRiskFactors(item);
        if (reasons.isEmpty())
        {
            reasons.add("设备接入状态正常，但需持续巡检");
        }
        return String.join("、", reasons);
    }

    private String resolveAccessStatusLabel(String value)
    {
        if ("0".equals(value))
        {
            return "待接入";
        }
        if ("1".equals(value))
        {
            return "在线";
        }
        if ("2".equals(value))
        {
            return "离线";
        }
        if ("3".equals(value))
        {
            return "异常";
        }
        return value;
    }

    private String resolveDeviceTypeLabel(String value)
    {
        if ("TEMP_HUMIDITY".equals(value))
        {
            return "温湿度传感器";
        }
        if ("CAMERA".equals(value))
        {
            return "摄像头";
        }
        if ("GATEWAY".equals(value))
        {
            return "网关";
        }
        if ("RFID".equals(value))
        {
            return "RFID终端";
        }
        return value;
    }

    private String resolveProtocolLabel(String value)
    {
        if ("MQTT".equals(value))
        {
            return "MQTT";
        }
        if ("HTTP".equals(value))
        {
            return "HTTP";
        }
        if ("COAP".equals(value))
        {
            return "CoAP";
        }
        if ("MODBUS".equals(value))
        {
            return "Modbus";
        }
        return value;
    }

    private String resolveRiskLevel(int riskScore)
    {
        if (riskScore >= 75)
        {
            return "高";
        }
        if (riskScore >= 45)
        {
            return "中";
        }
        return "低";
    }

    private double roundPercent(double value)
    {
        return Math.round(value * 10.0) / 10.0;
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:edit')")
    @GetMapping("/smart/diagnose/{nodeId}")
    public AjaxResult smartDiagnose(@PathVariable("nodeId") Long nodeId)
    {
        AgriDeviceAccessNode node = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeByNodeId(nodeId);
        if (node == null)
        {
            return error("设备节点不存在");
        }

        int healthScore = 85;
        List<String> factors = new ArrayList<>();
        if ("2".equals(node.getAccessStatus()))
        {
            healthScore -= 35;
            factors.add("设备离线");
        }
        if ("3".equals(node.getAccessStatus()))
        {
            healthScore -= 45;
            factors.add("设备异常");
        }
        if (node.getFirmwareVersion() == null || node.getFirmwareVersion().trim().isEmpty())
        {
            healthScore -= 10;
            factors.add("缺少固件版本");
        }

        healthScore = Math.max(healthScore, 5);
        String risk = healthScore < 40 ? "高" : (healthScore < 70 ? "中" : "低");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodeId", node.getNodeId());
        result.put("algorithm", "device-health-v1");
        result.put("healthScore", healthScore);
        result.put("risk", risk);
        result.put("factors", factors);
        result.put("summary", "完成设备接入健康度诊断，当前风险等级为" + risk + "。");
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:export')")
    @Log(title = "设备接入管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriDeviceAccessNode agriDeviceAccessNode)
    {
        List<AgriDeviceAccessNode> list = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeList(agriDeviceAccessNode);
        ExcelUtil<AgriDeviceAccessNode> util = new ExcelUtil<>(AgriDeviceAccessNode.class);
        util.exportExcel(response, list, "设备接入管理数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:query')")
    @GetMapping(value = "/{nodeId}")
    public AjaxResult getInfo(@PathVariable("nodeId") Long nodeId)
    {
        return success(agriDeviceAccessNodeService.selectAgriDeviceAccessNodeByNodeId(nodeId));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:add')")
    @Log(title = "设备接入管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriDeviceAccessNode agriDeviceAccessNode)
    {
        agriDeviceAccessNode.setCreateBy(getUsername());
        if (agriDeviceAccessNode.getAccessStatus() == null || agriDeviceAccessNode.getAccessStatus().isEmpty())
        {
            agriDeviceAccessNode.setAccessStatus("0");
        }
        return toAjax(agriDeviceAccessNodeService.insertAgriDeviceAccessNode(agriDeviceAccessNode));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:edit')")
    @Log(title = "设备接入管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriDeviceAccessNode agriDeviceAccessNode)
    {
        agriDeviceAccessNode.setUpdateBy(getUsername());
        return toAjax(agriDeviceAccessNodeService.updateAgriDeviceAccessNode(agriDeviceAccessNode));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:edit')")
    @Log(title = "设备激活", businessType = BusinessType.UPDATE)
    @PutMapping("/activate/{nodeId}")
    public AjaxResult activate(@PathVariable("nodeId") Long nodeId)
    {
        AgriDeviceAccessNode node = agriDeviceAccessNodeService.selectAgriDeviceAccessNodeByNodeId(nodeId);
        if (node == null)
        {
            return error("设备节点不存在");
        }
        node.setAccessStatus("1");
        node.setLastOnlineTime(DateUtils.getNowDate());
        node.setUpdateBy(getUsername());
        return toAjax(agriDeviceAccessNodeService.updateAgriDeviceAccessNode(node));
    }

    @PreAuthorize("@ss.hasPermi('agri:deviceAccess:remove')")
    @Log(title = "设备接入管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{nodeIds}")
    public AjaxResult remove(@PathVariable Long[] nodeIds)
    {
        return toAjax(agriDeviceAccessNodeService.deleteAgriDeviceAccessNodeByNodeIds(nodeIds));
    }
}
