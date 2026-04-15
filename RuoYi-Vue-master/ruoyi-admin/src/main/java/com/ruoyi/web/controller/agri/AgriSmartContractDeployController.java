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
