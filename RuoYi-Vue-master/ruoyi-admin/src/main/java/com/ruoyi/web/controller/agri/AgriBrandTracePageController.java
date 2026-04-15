package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriBrandTracePage;
import com.ruoyi.system.service.IAgriBrandTracePageService;
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
 * 品牌溯源页面Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/brandTrace")
public class AgriBrandTracePageController extends BaseController
{
    @Autowired
    private IAgriBrandTracePageService agriBrandTracePageService;

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriBrandTracePage agriBrandTracePage)
    {
        startPage();
        List<AgriBrandTracePage> list = agriBrandTracePageService.selectAgriBrandTracePageList(agriBrandTracePage);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:export')")
    @Log(title = "品牌溯源页面", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriBrandTracePage agriBrandTracePage)
    {
        List<AgriBrandTracePage> list = agriBrandTracePageService.selectAgriBrandTracePageList(agriBrandTracePage);
        ExcelUtil<AgriBrandTracePage> util = new ExcelUtil<>(AgriBrandTracePage.class);
        util.exportExcel(response, list, "品牌溯源页面数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:query')")
    @GetMapping(value = "/{pageId}")
    public AjaxResult getInfo(@PathVariable("pageId") Long pageId)
    {
        return success(agriBrandTracePageService.selectAgriBrandTracePageByPageId(pageId));
    }

    @GetMapping("/preview/{traceCode}")
    public AjaxResult preview(@PathVariable("traceCode") String traceCode)
    {
        AgriBrandTracePage page = agriBrandTracePageService.selectAgriBrandTracePageByTraceCode(traceCode);
        if (page == null)
        {
            return error("溯源码不存在");
        }
        if (!"1".equals(page.getPublishStatus()))
        {
            return error("该溯源码页面未发布");
        }
        return success(page);
    }

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:add')")
    @Log(title = "品牌溯源页面", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriBrandTracePage agriBrandTracePage)
    {
        agriBrandTracePage.setCreateBy(getUsername());
        if (agriBrandTracePage.getPublishStatus() == null || agriBrandTracePage.getPublishStatus().isEmpty())
        {
            agriBrandTracePage.setPublishStatus("0");
        }
        return toAjax(agriBrandTracePageService.insertAgriBrandTracePage(agriBrandTracePage));
    }

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:edit')")
    @Log(title = "品牌溯源页面", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriBrandTracePage agriBrandTracePage)
    {
        agriBrandTracePage.setUpdateBy(getUsername());
        return toAjax(agriBrandTracePageService.updateAgriBrandTracePage(agriBrandTracePage));
    }

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:edit')")
    @Log(title = "品牌溯源页面发布", businessType = BusinessType.UPDATE)
    @PutMapping("/publish/{pageId}")
    public AjaxResult publish(@PathVariable("pageId") Long pageId)
    {
        AgriBrandTracePage page = agriBrandTracePageService.selectAgriBrandTracePageByPageId(pageId);
        if (page == null)
        {
            return error("页面不存在");
        }
        page.setPublishStatus("1");
        page.setUpdateBy(getUsername());
        return toAjax(agriBrandTracePageService.updateAgriBrandTracePage(page));
    }

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:remove')")
    @Log(title = "品牌溯源页面", businessType = BusinessType.DELETE)
    @DeleteMapping("/{pageIds}")
    public AjaxResult remove(@PathVariable Long[] pageIds)
    {
        return toAjax(agriBrandTracePageService.deleteAgriBrandTracePageByPageIds(pageIds));
    }
}
