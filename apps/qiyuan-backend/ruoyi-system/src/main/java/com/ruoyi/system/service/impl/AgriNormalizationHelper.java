package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;

/**
 * Agri 服务层归一化助手。
 */
public final class AgriNormalizationHelper
{
    private AgriNormalizationHelper()
    {
    }

    public static <T> T ensureNotNull(T value, Supplier<T> supplier)
    {
        return value == null ? supplier.get() : value;
    }

    public static void defaultIfBlank(Supplier<String> getter, Consumer<String> setter, String defaultValue)
    {
        if (StringUtils.isBlank(getter.get()))
        {
            setter.accept(defaultValue);
        }
    }

    public static void defaultIfNull(Supplier<BigDecimal> getter, Consumer<BigDecimal> setter, BigDecimal defaultValue)
    {
        if (getter.get() == null)
        {
            setter.accept(defaultValue);
        }
    }
}