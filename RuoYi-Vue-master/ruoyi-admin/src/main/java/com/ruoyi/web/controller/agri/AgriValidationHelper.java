package com.ruoyi.web.controller.agri;

import com.ruoyi.common.utils.StringUtils;

/**
 * Agri 控制器参数校验助手。
 */
public final class AgriValidationHelper
{
    private AgriValidationHelper()
    {
    }

    public static boolean isBlank(String value)
    {
        return StringUtils.isBlank(value);
    }

    public static boolean hasAnyBlank(String... values)
    {
        if (values == null)
        {
            return true;
        }
        for (String value : values)
        {
            if (StringUtils.isBlank(value))
            {
                return true;
            }
        }
        return false;
    }
}