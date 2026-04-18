package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriCarbonFootprintModel;
import com.ruoyi.system.service.IAgriCarbonFootprintModelService;
import jakarta.servlet.http.HttpServletResponse;
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
 * 碳足迹核算模型Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/carbonFootprint")
public class AgriCarbonFootprintModelController extends BaseController
{
    @Autowired
    private IAgriCarbonFootprintModelService agriCarbonFootprintModelService;

    @PreAuthorize("@ss.hasPermi('agri:carbonFootprint:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        startPage();
        return getDataTable(agriCarbonFootprintModelService.selectAgriCarbonFootprintModelList(agriCarbonFootprintModel));
    }

    @PreAuthorize("@ss.hasPermi('agri:carbonFootprint:query')")
    @GetMapping("/dashboard/overview")
    public AjaxResult dashboardOverview(AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        return success(agriCarbonFootprintModelService.selectAgriCarbonFootprintModelDashboard(agriCarbonFootprintModel));
    }

    @PreAuthorize("@ss.hasPermi('agri:carbonFootprint:query')")
    @GetMapping("/smart/analyze/{modelId}")
    public AjaxResult smartAnalyze(@PathVariable("modelId") Long modelId)
    {
        return success(agriCarbonFootprintModelService.analyzeAgriCarbonFootprintModel(modelId));
    }

    @PreAuthorize("@ss.hasPermi('agri:carbonFootprint:query')")
    @GetMapping("/ai/health")
    public AjaxResult aiHealth()
    {
        return success(agriCarbonFootprintModelService.checkCarbonFootprintAiHealth());
    }

    @PreAuthorize("@ss.hasPermi('agri:carbonFootprint:export')")
    @Log(title = "碳足迹核算模型", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        ExcelUtil<AgriCarbonFootprintModel> util = new ExcelUtil<>(AgriCarbonFootprintModel.class);
        util.exportExcel(response, agriCarbonFootprintModelService.selectAgriCarbonFootprintModelList(agriCarbonFootprintModel), "碳足迹核算模型数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:carbonFootprint:query')")
    @GetMapping(value = "/{modelId}")
    public AjaxResult getInfo(@PathVariable("modelId") Long modelId)
    {
        return success(agriCarbonFootprintModelService.selectAgriCarbonFootprintModelByModelId(modelId));
    }

    @PreAuthorize("@ss.hasPermi('agri:carbonFootprint:add')")
    @Log(title = "碳足迹核算模型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        return toAjax(agriCarbonFootprintModelService.insertAgriCarbonFootprintModel(agriCarbonFootprintModel));
    }

    @PreAuthorize("@ss.hasPermi('agri:carbonFootprint:edit')")
    @Log(title = "碳足迹核算模型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        return toAjax(agriCarbonFootprintModelService.updateAgriCarbonFootprintModel(agriCarbonFootprintModel));
    }

    @PreAuthorize("@ss.hasPermi('agri:carbonFootprint:remove')")
    @Log(title = "碳足迹核算模型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{modelIds}")
    public AjaxResult remove(@PathVariable Long[] modelIds)
    {
        return toAjax(agriCarbonFootprintModelService.deleteAgriCarbonFootprintModelByModelIds(modelIds));
    }
}
