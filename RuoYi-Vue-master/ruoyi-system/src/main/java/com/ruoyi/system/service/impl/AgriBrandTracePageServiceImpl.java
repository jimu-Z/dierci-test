package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriBrandTracePage;
import com.ruoyi.system.mapper.AgriBrandTracePageMapper;
import com.ruoyi.system.service.IAgriBrandTracePageService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 品牌溯源页面Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriBrandTracePageServiceImpl implements IAgriBrandTracePageService
{
    @Autowired
    private AgriBrandTracePageMapper agriBrandTracePageMapper;

    @Override
    public AgriBrandTracePage selectAgriBrandTracePageByPageId(Long pageId)
    {
        return agriBrandTracePageMapper.selectAgriBrandTracePageByPageId(pageId);
    }

    @Override
    public List<AgriBrandTracePage> selectAgriBrandTracePageList(AgriBrandTracePage agriBrandTracePage)
    {
        return agriBrandTracePageMapper.selectAgriBrandTracePageList(agriBrandTracePage);
    }

    @Override
    public int insertAgriBrandTracePage(AgriBrandTracePage agriBrandTracePage)
    {
        return agriBrandTracePageMapper.insertAgriBrandTracePage(agriBrandTracePage);
    }

    @Override
    public int updateAgriBrandTracePage(AgriBrandTracePage agriBrandTracePage)
    {
        return agriBrandTracePageMapper.updateAgriBrandTracePage(agriBrandTracePage);
    }

    @Override
    public int deleteAgriBrandTracePageByPageIds(Long[] pageIds)
    {
        return agriBrandTracePageMapper.deleteAgriBrandTracePageByPageIds(pageIds);
    }

    @Override
    public AgriBrandTracePage selectAgriBrandTracePageByTraceCode(String traceCode)
    {
        return agriBrandTracePageMapper.selectAgriBrandTracePageByTraceCode(traceCode);
    }
}
