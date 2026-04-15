package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriBrandTracePage;
import com.ruoyi.system.domain.AgriConsumerScanQuery;
import com.ruoyi.system.service.IAgriBrandTracePageService;
import com.ruoyi.system.service.IAgriConsumerScanQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
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
 * 消费者扫码查询Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/consumerScan")
public class AgriConsumerScanQueryController extends BaseController
{
    @Autowired
    private IAgriConsumerScanQueryService agriConsumerScanQueryService;

    @Autowired
    private IAgriBrandTracePageService agriBrandTracePageService;

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriConsumerScanQuery agriConsumerScanQuery)
    {
        startPage();
        List<AgriConsumerScanQuery> list = agriConsumerScanQueryService.selectAgriConsumerScanQueryList(agriConsumerScanQuery);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:export')")
    @Log(title = "消费者扫码查询", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriConsumerScanQuery agriConsumerScanQuery)
    {
        List<AgriConsumerScanQuery> list = agriConsumerScanQueryService.selectAgriConsumerScanQueryList(agriConsumerScanQuery);
        ExcelUtil<AgriConsumerScanQuery> util = new ExcelUtil<>(AgriConsumerScanQuery.class);
        util.exportExcel(response, list, "消费者扫码查询数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:query')")
    @GetMapping(value = "/{queryId}")
    public AjaxResult getInfo(@PathVariable("queryId") Long queryId)
    {
        return success(agriConsumerScanQueryService.selectAgriConsumerScanQueryByQueryId(queryId));
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:add')")
    @Log(title = "消费者扫码查询", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriConsumerScanQuery agriConsumerScanQuery)
    {
        agriConsumerScanQuery.setCreateBy(getUsername());
        if (StringUtils.isEmpty(agriConsumerScanQuery.getScanResult()))
        {
            agriConsumerScanQuery.setScanResult("0");
        }
        if (agriConsumerScanQuery.getQueryTime() == null)
        {
            agriConsumerScanQuery.setQueryTime(DateUtils.getNowDate());
        }
        return toAjax(agriConsumerScanQueryService.insertAgriConsumerScanQuery(agriConsumerScanQuery));
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:edit')")
    @Log(title = "消费者扫码查询", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriConsumerScanQuery agriConsumerScanQuery)
    {
        agriConsumerScanQuery.setUpdateBy(getUsername());
        return toAjax(agriConsumerScanQueryService.updateAgriConsumerScanQuery(agriConsumerScanQuery));
    }

    @Log(title = "消费者扫码模拟查询", businessType = BusinessType.OTHER)
    @PostMapping("/scan/{traceCode}")
    public AjaxResult scan(@PathVariable("traceCode") String traceCode,
                           @RequestBody(required = false) AgriConsumerScanQuery payload,
                           HttpServletRequest request)
    {
        AgriConsumerScanQuery scanQuery = new AgriConsumerScanQuery();
        scanQuery.setTraceCode(traceCode);
        scanQuery.setQueryTime(DateUtils.getNowDate());
        scanQuery.setScanChannel(payload == null || StringUtils.isEmpty(payload.getScanChannel()) ? "WECHAT" : payload.getScanChannel());
        if (payload != null)
        {
            scanQuery.setConsumerName(payload.getConsumerName());
            scanQuery.setConsumerPhone(payload.getConsumerPhone());
            scanQuery.setScanAddress(payload.getScanAddress());
            scanQuery.setRemark(payload.getRemark());
        }
        scanQuery.setScanIp(getRequestIp(request));
        scanQuery.setStatus("0");

        AgriBrandTracePage page = agriBrandTracePageService.selectAgriBrandTracePageByTraceCode(traceCode);
        if (page == null)
        {
            scanQuery.setScanResult("0");
            agriConsumerScanQueryService.insertAgriConsumerScanQuery(scanQuery);
            return error("未查询到对应溯源码信息");
        }

        if (!"1".equals(page.getPublishStatus()))
        {
            scanQuery.setScanResult("2");
            agriConsumerScanQueryService.insertAgriConsumerScanQuery(scanQuery);
            return error("该溯源码页面未发布");
        }

        scanQuery.setScanResult("1");
        agriConsumerScanQueryService.insertAgriConsumerScanQuery(scanQuery);
        return success(page);
    }

    @PreAuthorize("@ss.hasPermi('agri:consumerScan:remove')")
    @Log(title = "消费者扫码查询", businessType = BusinessType.DELETE)
    @DeleteMapping("/{queryIds}")
    public AjaxResult remove(@PathVariable Long[] queryIds)
    {
        return toAjax(agriConsumerScanQueryService.deleteAgriConsumerScanQueryByQueryIds(queryIds));
    }

    private String getRequestIp(HttpServletRequest request)
    {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && ip.contains(","))
        {
            ip = ip.split(",")[0].trim();
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
