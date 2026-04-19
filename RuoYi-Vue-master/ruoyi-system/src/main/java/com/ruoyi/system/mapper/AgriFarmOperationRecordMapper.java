package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriFarmOperationRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 农事记录Mapper接口
 *
 * @author ruoyi
 */
public interface AgriFarmOperationRecordMapper
{
    /**
     * 查询农事记录
     *
     * @param operationId 主键
     * @return 农事记录
     */
    public AgriFarmOperationRecord selectAgriFarmOperationRecordByOperationId(Long operationId);

    /**
     * 查询农事记录列表
     *
     * @param agriFarmOperationRecord 农事记录
     * @return 农事记录集合
     */
    public List<AgriFarmOperationRecord> selectAgriFarmOperationRecordList(AgriFarmOperationRecord agriFarmOperationRecord);

    /**
     * 新增农事记录
     *
     * @param agriFarmOperationRecord 农事记录
     * @return 结果
     */
    public int insertAgriFarmOperationRecord(AgriFarmOperationRecord agriFarmOperationRecord);

    /**
     * 修改农事记录
     *
     * @param agriFarmOperationRecord 农事记录
     * @return 结果
     */
    public int updateAgriFarmOperationRecord(AgriFarmOperationRecord agriFarmOperationRecord);

    /**
     * 删除农事记录
     *
     * @param operationId 主键
     * @return 结果
     */
    public int deleteAgriFarmOperationRecordByOperationId(Long operationId);

    /**
     * 批量删除农事记录
     *
     * @param operationIds 需要删除的主键集合
     * @return 结果
     */
    public int deleteAgriFarmOperationRecordByOperationIds(Long[] operationIds);

    /**
     * 查询地块编码选项
     *
     * @param keyword 搜索关键字
     * @return 地块编码列表
     */
    public List<String> selectPlotCodeOptions(@Param("keyword") String keyword);
}
