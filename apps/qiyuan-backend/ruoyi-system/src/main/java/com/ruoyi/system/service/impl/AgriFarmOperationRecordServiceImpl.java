package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriFarmOperationRecord;
import com.ruoyi.system.mapper.AgriFarmOperationRecordMapper;
import com.ruoyi.system.service.IAgriFarmOperationRecordService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 农事记录Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriFarmOperationRecordServiceImpl implements IAgriFarmOperationRecordService
{
    @Autowired
    private AgriFarmOperationRecordMapper agriFarmOperationRecordMapper;

    /**
     * 查询农事记录
     *
     * @param operationId 主键
     * @return 农事记录
     */
    @Override
    public AgriFarmOperationRecord selectAgriFarmOperationRecordByOperationId(Long operationId)
    {
        return agriFarmOperationRecordMapper.selectAgriFarmOperationRecordByOperationId(operationId);
    }

    /**
     * 查询农事记录列表
     *
     * @param agriFarmOperationRecord 农事记录
     * @return 农事记录
     */
    @Override
    public List<AgriFarmOperationRecord> selectAgriFarmOperationRecordList(AgriFarmOperationRecord agriFarmOperationRecord)
    {
        return agriFarmOperationRecordMapper.selectAgriFarmOperationRecordList(agriFarmOperationRecord);
    }

    /**
     * 新增农事记录
     *
     * @param agriFarmOperationRecord 农事记录
     * @return 结果
     */
    @Override
    public int insertAgriFarmOperationRecord(AgriFarmOperationRecord agriFarmOperationRecord)
    {
        return agriFarmOperationRecordMapper.insertAgriFarmOperationRecord(agriFarmOperationRecord);
    }

    /**
     * 修改农事记录
     *
     * @param agriFarmOperationRecord 农事记录
     * @return 结果
     */
    @Override
    public int updateAgriFarmOperationRecord(AgriFarmOperationRecord agriFarmOperationRecord)
    {
        return agriFarmOperationRecordMapper.updateAgriFarmOperationRecord(agriFarmOperationRecord);
    }

    /**
     * 批量删除农事记录
     *
     * @param operationIds 需要删除的主键
     * @return 结果
     */
    @Override
    public int deleteAgriFarmOperationRecordByOperationIds(Long[] operationIds)
    {
        return agriFarmOperationRecordMapper.deleteAgriFarmOperationRecordByOperationIds(operationIds);
    }

    @Override
    public List<String> selectPlotCodeOptions(String keyword)
    {
        return agriFarmOperationRecordMapper.selectPlotCodeOptions(keyword);
    }
}
