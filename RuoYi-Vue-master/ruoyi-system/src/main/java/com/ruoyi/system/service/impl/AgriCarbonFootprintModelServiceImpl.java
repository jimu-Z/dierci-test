package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.AgriCarbonFootprintModel;
import com.ruoyi.system.mapper.AgriCarbonFootprintModelMapper;
import com.ruoyi.system.service.IAgriCarbonFootprintModelService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 碳足迹核算模型Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class AgriCarbonFootprintModelServiceImpl implements IAgriCarbonFootprintModelService
{
    @Autowired
    private AgriCarbonFootprintModelMapper agriCarbonFootprintModelMapper;

    @Override
    public AgriCarbonFootprintModel selectAgriCarbonFootprintModelByModelId(Long modelId)
    {
        return agriCarbonFootprintModelMapper.selectAgriCarbonFootprintModelByModelId(modelId);
    }

    @Override
    public List<AgriCarbonFootprintModel> selectAgriCarbonFootprintModelList(AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        return agriCarbonFootprintModelMapper.selectAgriCarbonFootprintModelList(agriCarbonFootprintModel);
    }

    @Override
    public int insertAgriCarbonFootprintModel(AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        return agriCarbonFootprintModelMapper.insertAgriCarbonFootprintModel(agriCarbonFootprintModel);
    }

    @Override
    public int updateAgriCarbonFootprintModel(AgriCarbonFootprintModel agriCarbonFootprintModel)
    {
        return agriCarbonFootprintModelMapper.updateAgriCarbonFootprintModel(agriCarbonFootprintModel);
    }

    @Override
    public int deleteAgriCarbonFootprintModelByModelIds(Long[] modelIds)
    {
        return agriCarbonFootprintModelMapper.deleteAgriCarbonFootprintModelByModelIds(modelIds);
    }
}
