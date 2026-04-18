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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        List<AgriBrandTracePage> list = agriBrandTracePageService.selectAgriBrandTracePageList(new AgriBrandTracePage());
        int total = list.size();
        int published = 0;
        int hasCover = 0;
        int hasQr = 0;
        for (AgriBrandTracePage item : list)
        {
            if ("1".equals(item.getPublishStatus()))
            {
                published++;
            }
            if (notBlank(item.getCoverImageUrl()))
            {
                hasCover++;
            }
            if (notBlank(item.getQrCodeUrl()))
            {
                hasQr++;
            }
        }

        List<AgriBrandTracePage> draftTop = list.stream()
            .filter(x -> !"1".equals(x.getPublishStatus()))
            .sorted(Comparator.comparing(AgriBrandTracePage::getUpdateTime,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(6)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("published", published);
        kpi.put("publishRate", total == 0 ? 0 : (published * 100.0 / total));
        kpi.put("coverReady", hasCover);
        kpi.put("qrReady", hasQr);
        result.put("kpi", kpi);
        result.put("draftTop", draftTop);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:list')")
    @GetMapping("/dashboard/ops")
    public AjaxResult dashboardOps()
    {
        List<AgriBrandTracePage> list = agriBrandTracePageService.selectAgriBrandTracePageList(new AgriBrandTracePage());
        int total = list.size();
        int published = 0;
        int draft = 0;
        int withCover = 0;
        int withQr = 0;
        int excellent = 0;
        int review = 0;
        int poor = 0;

        for (AgriBrandTracePage item : list)
        {
            if ("1".equals(item.getPublishStatus()))
            {
                published++;
            }
            else
            {
                draft++;
            }
            if (notBlank(item.getCoverImageUrl()))
            {
                withCover++;
            }
            if (notBlank(item.getQrCodeUrl()))
            {
                withQr++;
            }

            int score = calcQualityScore(item);
            if (score >= 85)
            {
                excellent++;
            }
            else if (score >= 65)
            {
                review++;
            }
            else
            {
                poor++;
            }
        }

        List<AgriBrandTracePage> pendingPages = list.stream()
            .filter(x -> !"1".equals(x.getPublishStatus()))
            .sorted(Comparator.comparing(this::scoreForSort).reversed()
                .thenComparing(AgriBrandTracePage::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(8)
            .collect(Collectors.toList());

        List<AgriBrandTracePage> latestPublished = list.stream()
            .filter(x -> "1".equals(x.getPublishStatus()))
            .sorted(Comparator.comparing(AgriBrandTracePage::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(6)
            .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("total", total);
        kpi.put("published", published);
        kpi.put("draft", draft);
        kpi.put("publishRate", total == 0 ? 0 : (published * 100.0 / total));
        kpi.put("coverRate", total == 0 ? 0 : (withCover * 100.0 / total));
        kpi.put("qrRate", total == 0 ? 0 : (withQr * 100.0 / total));
        result.put("kpi", kpi);

        Map<String, Object> publishFunnel = new LinkedHashMap<>();
        publishFunnel.put("created", total);
        publishFunnel.put("contentReady", Math.min(withCover, withQr));
        publishFunnel.put("published", published);
        result.put("publishFunnel", publishFunnel);

        Map<String, Object> qualityBucket = new LinkedHashMap<>();
        qualityBucket.put("excellent", excellent);
        qualityBucket.put("review", review);
        qualityBucket.put("poor", poor);
        result.put("qualityBucket", qualityBucket);

        result.put("pendingPages", pendingPages);
        result.put("latestPublished", latestPublished);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('agri:brandTrace:edit')")
    @GetMapping("/smart/inspect/{pageId}")
    public AjaxResult smartInspect(@PathVariable("pageId") Long pageId)
    {
        AgriBrandTracePage page = agriBrandTracePageService.selectAgriBrandTracePageByPageId(pageId);
        if (page == null)
        {
            return error("页面不存在");
        }

        int score = 40;
        List<String> missing = new ArrayList<>();
        score += scorePart(page.getBrandName(), 10, missing, "品牌名称");
        score += scorePart(page.getProductName(), 10, missing, "产品名称");
        score += scorePart(page.getOriginPlace(), 10, missing, "产地");
        score += scorePart(page.getCoverImageUrl(), 8, missing, "封面图");
        score += scorePart(page.getQrCodeUrl(), 8, missing, "二维码地址");
        score += scorePart(page.getBrandStory(), 8, missing, "品牌故事");
        score += "1".equals(page.getPublishStatus()) ? 6 : 0;

        String readiness = score >= 85 ? "ready" : (score >= 65 ? "review" : "draft");
        List<String> suggestions = new ArrayList<>();
        if (!missing.isEmpty())
        {
            suggestions.add("请优先补齐字段: " + String.join("、", missing));
        }
        if (!"1".equals(page.getPublishStatus()))
        {
            suggestions.add("页面尚未发布，建议完成审核后执行发布操作");
        }
        if (suggestions.isEmpty())
        {
            suggestions.add("溯源信息完整，可进入渠道投放与扫码活动阶段");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageId", page.getPageId());
        result.put("traceCode", page.getTraceCode());
        result.put("algorithm", "trace-page-quality-v1");
        result.put("contentScore", score);
        result.put("readiness", readiness);
        result.put("missing", missing);
        result.put("suggestions", suggestions);
        result.put("summary", "完成页面内容完整性与可发布性检测，当前状态为" + readiness + "。");
        return success(result);
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

    private boolean notBlank(String value)
    {
        return value != null && !value.trim().isEmpty();
    }

    private int scorePart(String value, int points, List<String> missing, String fieldName)
    {
        if (notBlank(value))
        {
            return points;
        }
        missing.add(fieldName);
        return 0;
    }

    private int calcQualityScore(AgriBrandTracePage page)
    {
        int score = 40;
        score += notBlank(page.getBrandName()) ? 10 : 0;
        score += notBlank(page.getProductName()) ? 10 : 0;
        score += notBlank(page.getOriginPlace()) ? 10 : 0;
        score += notBlank(page.getCoverImageUrl()) ? 8 : 0;
        score += notBlank(page.getQrCodeUrl()) ? 8 : 0;
        score += notBlank(page.getBrandStory()) ? 8 : 0;
        score += "1".equals(page.getPublishStatus()) ? 6 : 0;
        return score;
    }

    private Integer scoreForSort(AgriBrandTracePage page)
    {
        return calcQualityScore(page);
    }
}
