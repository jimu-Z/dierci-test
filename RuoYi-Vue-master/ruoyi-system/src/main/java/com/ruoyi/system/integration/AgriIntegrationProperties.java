package com.ruoyi.system.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 农业外部接入配置。
 */
@Component
@ConfigurationProperties(prefix = "agri.integration")
public class AgriIntegrationProperties
{
    private final Ai ai = new Ai();

    private final Sensor sensor = new Sensor();

    public Ai getAi()
    {
        return ai;
    }

    public Sensor getSensor()
    {
        return sensor;
    }

    public static class Ai
    {
        private boolean enabled = false;

        private String baseUrl;

        private String apiKey;

        private String model = "deepseek-chat";

        private int connectTimeoutMs = 3000;

        private int readTimeoutMs = 8000;

        private String pestPath = "/pest-identify";

        private String yieldPath = "/yield-forecast";

        private String qualityPath = "/quality-inspect";

        private String marketPath = "/chat/completions";

        private String outputSalesPath = "/chat/completions";

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        public String getBaseUrl()
        {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl)
        {
            this.baseUrl = baseUrl;
        }

        public String getApiKey()
        {
            return apiKey;
        }

        public void setApiKey(String apiKey)
        {
            this.apiKey = apiKey;
        }

        public String getModel()
        {
            return model;
        }

        public void setModel(String model)
        {
            this.model = model;
        }

        public int getConnectTimeoutMs()
        {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs)
        {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs()
        {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs)
        {
            this.readTimeoutMs = readTimeoutMs;
        }

        public String getPestPath()
        {
            return pestPath;
        }

        public void setPestPath(String pestPath)
        {
            this.pestPath = pestPath;
        }

        public String getYieldPath()
        {
            return yieldPath;
        }

        public void setYieldPath(String yieldPath)
        {
            this.yieldPath = yieldPath;
        }

        public String getQualityPath()
        {
            return qualityPath;
        }

        public void setQualityPath(String qualityPath)
        {
            this.qualityPath = qualityPath;
        }

        public String getMarketPath()
        {
            return marketPath;
        }

        public void setMarketPath(String marketPath)
        {
            this.marketPath = marketPath;
        }

        public String getOutputSalesPath()
        {
            return outputSalesPath;
        }

        public void setOutputSalesPath(String outputSalesPath)
        {
            this.outputSalesPath = outputSalesPath;
        }
    }

    public static class Sensor
    {
        private String ingestToken;

        private String authHeaderName = "X-Agri-Token";

        private String dataSource = "gateway-http";

        private double tempAlertHigh = 35D;

        private double humidityAlertHigh = 85D;

        public String getIngestToken()
        {
            return ingestToken;
        }

        public void setIngestToken(String ingestToken)
        {
            this.ingestToken = ingestToken;
        }

        public String getAuthHeaderName()
        {
            return authHeaderName;
        }

        public void setAuthHeaderName(String authHeaderName)
        {
            this.authHeaderName = authHeaderName;
        }

        public String getDataSource()
        {
            return dataSource;
        }

        public void setDataSource(String dataSource)
        {
            this.dataSource = dataSource;
        }

        public double getTempAlertHigh()
        {
            return tempAlertHigh;
        }

        public void setTempAlertHigh(double tempAlertHigh)
        {
            this.tempAlertHigh = tempAlertHigh;
        }

        public double getHumidityAlertHigh()
        {
            return humidityAlertHigh;
        }

        public void setHumidityAlertHigh(double humidityAlertHigh)
        {
            this.humidityAlertHigh = humidityAlertHigh;
        }
    }
}
