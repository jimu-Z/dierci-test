package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriYieldForecastTask;
import com.ruoyi.system.integration.AgriHttpIntegrationClient;
import com.ruoyi.system.service.IAgriYieldForecastTaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * 产量预测任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/yieldForecast")
public class AgriYieldForecastTaskController extends BaseController
{
    @Autowired
    private IAgriYieldForecastTaskService agriYieldForecastTaskService;

    @Autowired
    private AgriHttpIntegrationClient agriHttpIntegrationClient;

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriYieldForecastTask agriYieldForecastTask)
    {
        startPage();
        List<AgriYieldForecastTask> list = agriYieldForecastTaskService.selectAgriYieldForecastTaskList(agriYieldForecastTask);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:export')")
    @Log(title = "产量预测任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriYieldForecastTask agriYieldForecastTask)
    {
        List<AgriYieldForecastTask> list = agriYieldForecastTaskService.selectAgriYieldForecastTaskList(agriYieldForecastTask);
        ExcelUtil<AgriYieldForecastTask> util = new ExcelUtil<>(AgriYieldForecastTask.class);
        util.exportExcel(response, list, "产量预测任务数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:query')")
    @GetMapping(value = "/{forecastId}")
    public AjaxResult getInfo(@PathVariable("forecastId") Long forecastId)
    {
        return success(agriYieldForecastTaskService.selectAgriYieldForecastTaskByForecastId(forecastId));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:add')")
    @Log(title = "产量预测任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriYieldForecastTask agriYieldForecastTask)
    {
        agriYieldForecastTask.setCreateBy(getUsername());
        if (agriYieldForecastTask.getForecastStatus() == null || agriYieldForecastTask.getForecastStatus().isEmpty())
        {
            agriYieldForecastTask.setForecastStatus("0");
        }
        return toAjax(agriYieldForecastTaskService.insertAgriYieldForecastTask(agriYieldForecastTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:edit')")
    @Log(title = "产量预测任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriYieldForecastTask agriYieldForecastTask)
    {
        agriYieldForecastTask.setUpdateBy(getUsername());
        return toAjax(agriYieldForecastTaskService.updateAgriYieldForecastTask(agriYieldForecastTask));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:remove')")
    @Log(title = "产量预测任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{forecastIds}")
    public AjaxResult remove(@PathVariable Long[] forecastIds)
    {
        return toAjax(agriYieldForecastTaskService.deleteAgriYieldForecastTaskByForecastIds(forecastIds));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:edit')")
    @Log(title = "产量预测回写", businessType = BusinessType.UPDATE)
    @PutMapping("/predict")
    public AjaxResult predict(@RequestBody AgriYieldForecastTask agriYieldForecastTask)
    {
        AgriYieldForecastTask db = agriYieldForecastTaskService.selectAgriYieldForecastTaskByForecastId(agriYieldForecastTask.getForecastId());
        if (db == null)
        {
            return error("任务不存在");
        }

        BigDecimal predicted = agriYieldForecastTask.getForecastYieldKg();
        if (predicted == null)
        {
            BigDecimal area = db.getAreaMu() == null ? BigDecimal.ONE : db.getAreaMu();
            predicted = area.multiply(new BigDecimal("450")).setScale(2, RoundingMode.HALF_UP);
        }

        db.setForecastYieldKg(predicted);
        db.setForecastStatus("1");
        db.setForecastTime(DateUtils.getNowDate());
        db.setModelVersion(agriYieldForecastTask.getModelVersion());
        db.setUpdateBy(getUsername());
        return toAjax(agriYieldForecastTaskService.updateAgriYieldForecastTask(db));
    }

    @PreAuthorize("@ss.hasPermi('agri:yieldForecast:edit')")
    @Log(title = "产量预测调用", businessType = BusinessType.UPDATE)
    @PostMapping("/invoke/{forecastId}")
    public AjaxResult invoke(@PathVariable("forecastId") Long forecastId)
    {
        AgriYieldForecastTask db = agriYieldForecastTaskService.selectAgriYieldForecastTaskByForecastId(forecastId);
        if (db == null)
        {
            return error("任务不存在");
        }

        try
        {
            AgriHttpIntegrationClient.YieldResult result = agriHttpIntegrationClient.invokeYield(db);
            db.setForecastYieldKg(result.getForecastYieldKg());
            db.setModelVersion(result.getModelVersion());
            db.setForecastStatus("1");
            db.setForecastTime(DateUtils.getNowDate());
            db.setUpdateBy(getUsername());
            agriYieldForecastTaskService.updateAgriYieldForecastTask(db);
            return success("预测调用成功");
        }
        catch (Exception ex)
        {
            db.setForecastStatus("2");
            db.setRemark("调用失败: " + ex.getMessage());
            db.setForecastTime(DateUtils.getNowDate());
            db.setUpdateBy(getUsername());
            agriYieldForecastTaskService.updateAgriYieldForecastTask(db);
            return error("预测调用失败: " + ex.getMessage());
        }
    }
}
