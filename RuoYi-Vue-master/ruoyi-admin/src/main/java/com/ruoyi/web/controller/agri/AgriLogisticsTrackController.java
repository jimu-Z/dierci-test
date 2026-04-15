package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriLogisticsTrack;
import com.ruoyi.system.service.IAgriLogisticsTrackService;
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
 * 物流路径追踪Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/logisticsTrack")
public class AgriLogisticsTrackController extends BaseController
{
    @Autowired
    private IAgriLogisticsTrackService agriLogisticsTrackService;

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriLogisticsTrack agriLogisticsTrack)
    {
        startPage();
        List<AgriLogisticsTrack> list = agriLogisticsTrackService.selectAgriLogisticsTrackList(agriLogisticsTrack);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:export')")
    @Log(title = "物流路径追踪", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriLogisticsTrack agriLogisticsTrack)
    {
        List<AgriLogisticsTrack> list = agriLogisticsTrackService.selectAgriLogisticsTrackList(agriLogisticsTrack);
        ExcelUtil<AgriLogisticsTrack> util = new ExcelUtil<>(AgriLogisticsTrack.class);
        util.exportExcel(response, list, "物流路径追踪数据");
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:query')")
    @GetMapping(value = "/{trackId}")
    public AjaxResult getInfo(@PathVariable("trackId") Long trackId)
    {
        return success(agriLogisticsTrackService.selectAgriLogisticsTrackByTrackId(trackId));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:query')")
    @GetMapping("/timeline/{traceCode}")
    public AjaxResult timeline(@PathVariable("traceCode") String traceCode)
    {
        return success(agriLogisticsTrackService.selectAgriLogisticsTrackTimeline(traceCode));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:add')")
    @Log(title = "物流路径追踪", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriLogisticsTrack agriLogisticsTrack)
    {
        agriLogisticsTrack.setCreateBy(getUsername());
        return toAjax(agriLogisticsTrackService.insertAgriLogisticsTrack(agriLogisticsTrack));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:edit')")
    @Log(title = "物流路径追踪", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriLogisticsTrack agriLogisticsTrack)
    {
        agriLogisticsTrack.setUpdateBy(getUsername());
        return toAjax(agriLogisticsTrackService.updateAgriLogisticsTrack(agriLogisticsTrack));
    }

    @PreAuthorize("@ss.hasPermi('agri:logisticsTrack:remove')")
    @Log(title = "物流路径追踪", businessType = BusinessType.DELETE)
    @DeleteMapping("/{trackIds}")
    public AjaxResult remove(@PathVariable Long[] trackIds)
    {
        return toAjax(agriLogisticsTrackService.deleteAgriLogisticsTrackByTrackIds(trackIds));
    }
}
