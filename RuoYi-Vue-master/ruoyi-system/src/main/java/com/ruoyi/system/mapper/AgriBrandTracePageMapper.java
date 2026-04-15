package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriBrandTracePage;
import java.util.List;

/**
 * 品牌溯源页面Mapper接口
 *
 * @author ruoyi
 */
public interface AgriBrandTracePageMapper
{
    public AgriBrandTracePage selectAgriBrandTracePageByPageId(Long pageId);

    public List<AgriBrandTracePage> selectAgriBrandTracePageList(AgriBrandTracePage agriBrandTracePage);

    public int insertAgriBrandTracePage(AgriBrandTracePage agriBrandTracePage);

    public int updateAgriBrandTracePage(AgriBrandTracePage agriBrandTracePage);

    public int deleteAgriBrandTracePageByPageId(Long pageId);

    public int deleteAgriBrandTracePageByPageIds(Long[] pageIds);

    public AgriBrandTracePage selectAgriBrandTracePageByTraceCode(String traceCode);
}
