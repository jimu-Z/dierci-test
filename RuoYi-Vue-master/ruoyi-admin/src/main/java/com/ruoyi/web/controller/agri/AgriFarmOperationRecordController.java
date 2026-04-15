package com.ruoyi.web.controller.agri;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.AgriFarmOperationRecord;
import com.ruoyi.system.service.IAgriFarmOperationRecordService;
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
 * 农事记录Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/farmOp")
public class AgriFarmOperationRecordController extends BaseController
{
    @Autowired
    private IAgriFarmOperationRecordService agriFarmOperationRecordService;

    /**
     * 查询农事记录列表
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgriFarmOperationRecord agriFarmOperationRecord)
    {
        startPage();
        List<AgriFarmOperationRecord> list = agriFarmOperationRecordService.selectAgriFarmOperationRecordList(agriFarmOperationRecord);
        return getDataTable(list);
    }

    /**
     * 导出农事记录列表
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:export')")
    @Log(title = "农事记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AgriFarmOperationRecord agriFarmOperationRecord)
    {
        List<AgriFarmOperationRecord> list = agriFarmOperationRecordService.selectAgriFarmOperationRecordList(agriFarmOperationRecord);
        ExcelUtil<AgriFarmOperationRecord> util = new ExcelUtil<>(AgriFarmOperationRecord.class);
        util.exportExcel(response, list, "农事记录数据");
    }

    /**
     * 获取农事记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:query')")
    @GetMapping(value = "/{operationId}")
    public AjaxResult getInfo(@PathVariable("operationId") Long operationId)
    {
        return success(agriFarmOperationRecordService.selectAgriFarmOperationRecordByOperationId(operationId));
    }

    /**
     * 新增农事记录
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:add')")
    @Log(title = "农事记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody AgriFarmOperationRecord agriFarmOperationRecord)
    {
        agriFarmOperationRecord.setCreateBy(getUsername());
        return toAjax(agriFarmOperationRecordService.insertAgriFarmOperationRecord(agriFarmOperationRecord));
    }

    /**
     * 修改农事记录
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:edit')")
    @Log(title = "农事记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody AgriFarmOperationRecord agriFarmOperationRecord)
    {
        agriFarmOperationRecord.setUpdateBy(getUsername());
        return toAjax(agriFarmOperationRecordService.updateAgriFarmOperationRecord(agriFarmOperationRecord));
    }

    /**
     * 删除农事记录
     */
    @PreAuthorize("@ss.hasPermi('agri:farmOp:remove')")
    @Log(title = "农事记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operationIds}")
    public AjaxResult remove(@PathVariable Long[] operationIds)
    {
        return toAjax(agriFarmOperationRecordService.deleteAgriFarmOperationRecordByOperationIds(operationIds));
    }
}
