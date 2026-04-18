package com.ruoyi.system.service;

import com.ruoyi.system.domain.AgriCarbonFootprintModel;
import java.util.List;
import java.util.Map;

/**
 * 碳足迹核算模型Service接口
 *
 * @author ruoyi
 */
public interface IAgriCarbonFootprintModelService
{
    public AgriCarbonFootprintModel selectAgriCarbonFootprintModelByModelId(Long modelId);

    public List<AgriCarbonFootprintModel> selectAgriCarbonFootprintModelList(AgriCarbonFootprintModel agriCarbonFootprintModel);

    public Map<String, Object> selectAgriCarbonFootprintModelDashboard(AgriCarbonFootprintModel agriCarbonFootprintModel);

    public Map<String, Object> analyzeAgriCarbonFootprintModel(Long modelId);

    public Map<String, Object> checkCarbonFootprintAiHealth();

    public int insertAgriCarbonFootprintModel(AgriCarbonFootprintModel agriCarbonFootprintModel);

    public int updateAgriCarbonFootprintModel(AgriCarbonFootprintModel agriCarbonFootprintModel);

    public int deleteAgriCarbonFootprintModelByModelIds(Long[] modelIds);
}
