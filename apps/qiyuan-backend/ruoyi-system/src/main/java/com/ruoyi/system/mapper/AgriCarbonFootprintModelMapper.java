package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AgriCarbonFootprintModel;
import java.util.List;

/**
 * 碳足迹核算模型Mapper接口
 *
 * @author ruoyi
 */
public interface AgriCarbonFootprintModelMapper
{
    public AgriCarbonFootprintModel selectAgriCarbonFootprintModelByModelId(Long modelId);

    public List<AgriCarbonFootprintModel> selectAgriCarbonFootprintModelList(AgriCarbonFootprintModel agriCarbonFootprintModel);

    public int insertAgriCarbonFootprintModel(AgriCarbonFootprintModel agriCarbonFootprintModel);

    public int updateAgriCarbonFootprintModel(AgriCarbonFootprintModel agriCarbonFootprintModel);

    public int deleteAgriCarbonFootprintModelByModelId(Long modelId);

    public int deleteAgriCarbonFootprintModelByModelIds(Long[] modelIds);
}
