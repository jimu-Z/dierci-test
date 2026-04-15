package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriSupplyChainContract;
import com.ruoyi.system.service.IAgriSupplyChainContractService;
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

    @PreAuthorize("@ss.hasPermi('agri:supplyContract:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriSupplyChainContract agriSupplyChainContract)
    {
        startPage();
        return getDataTable(agriSupplyChainContractService.selectAgriSupplyChainContractList(agriSupplyChainContract));
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
}
