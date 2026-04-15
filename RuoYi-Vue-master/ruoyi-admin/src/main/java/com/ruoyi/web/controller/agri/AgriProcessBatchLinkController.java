package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriProcessBatchLink;
import com.ruoyi.system.service.IAgriProcessBatchLinkService;
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
 * 加工批次关联Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/processBatch")
public class AgriProcessBatchLinkController extends BaseController
{
    @Autowired
    private IAgriProcessBatchLinkService agriProcessBatchLinkService;

    @PreAuthorize("@ss.hasPermi('agri:processBatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriProcessBatchLink agriProcessBatchLink)
    {
        startPage();
        List<AgriProcessBatchLink> list = agriProcessBatchLinkService.selectAgriProcessBatchLinkList(agriProcessBatchLink);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:export')")
    @Log(title = "加工批次关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriProcessBatchLink agriProcessBatchLink)
    {
        List<AgriProcessBatchLink> list = agriProcessBatchLinkService.selectAgriProcessBatchLinkList(agriProcessBatchLink);
        ExcelUtil<AgriProcessBatchLink> util = new ExcelUtil<>(AgriProcessBatchLink.class);
        util.exportExcel(response, list, "加工批次关联数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:query')")
    @GetMapping(value = "/{linkId}")
    public AjaxResult getInfo(@PathVariable("linkId") Long linkId)
    {
        return success(agriProcessBatchLinkService.selectAgriProcessBatchLinkByLinkId(linkId));
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:add')")
    @Log(title = "加工批次关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriProcessBatchLink agriProcessBatchLink)
    {
        agriProcessBatchLink.setCreateBy(getUsername());
        return toAjax(agriProcessBatchLinkService.insertAgriProcessBatchLink(agriProcessBatchLink));
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:edit')")
    @Log(title = "加工批次关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriProcessBatchLink agriProcessBatchLink)
    {
        agriProcessBatchLink.setUpdateBy(getUsername());
        return toAjax(agriProcessBatchLinkService.updateAgriProcessBatchLink(agriProcessBatchLink));
    }

    @PreAuthorize("@ss.hasPermi('agri:processBatch:remove')")
    @Log(title = "加工批次关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{linkIds}")
    public AjaxResult remove(@PathVariable Long[] linkIds)
    {
        return toAjax(agriProcessBatchLinkService.deleteAgriProcessBatchLinkByLinkIds(linkIds));
    }
}
