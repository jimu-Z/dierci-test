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

    private final Weather weather = new Weather();

    private final Map map = new Map();

    public Ai getAi()
    {
        return ai;
    }

    public Sensor getSensor()
    {
        return sensor;
    }

    public Weather getWeather()
    {
        return weather;
    }

    public Map getMap()
    {
        return map;
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

        private final Emqx emqx = new Emqx();

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

        public Emqx getEmqx()
        {
            return emqx;
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

        public static class Emqx
        {
            private String dataSource = "emqx-webhook";

            public String getDataSource()
            {
                return dataSource;
            }

            public void setDataSource(String dataSource)
            {
                this.dataSource = dataSource;
            }
        }
    }

    public static class Weather
    {
        private boolean enabled = false;

        private String provider = "qweather";

        private String baseUrl;

        private String apiKey;

        private String location = "118.7853,31.9998";

        private String language = "zh-Hans";

        private String unit = "m";

        private int timeoutSec = 8;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        public String getProvider()
        {
            return provider;
        }

        public void setProvider(String provider)
        {
            this.provider = provider;
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

        public String getLocation()
        {
            return location;
        }

        public void setLocation(String location)
        {
            this.location = location;
        }

        public String getLanguage()
        {
            return language;
        }

        public void setLanguage(String language)
        {
            this.language = language;
        }

        public String getUnit()
        {
            return unit;
        }

        public void setUnit(String unit)
        {
            this.unit = unit;
        }

        public int getTimeoutSec()
        {
            return timeoutSec;
        }

        public void setTimeoutSec(int timeoutSec)
        {
            this.timeoutSec = timeoutSec;
        }
    }

    public static class Map
    {
        private boolean enabled = false;

        private String provider = "amap";

        private String baseUrl = "https://restapi.amap.com";

        private String apiKey;

        private String location = "118.7853,31.9998";

        private String securityJsCode;

        private String geocodePath = "/v3/geocode/geo";

        private String drivingPath = "/v3/direction/driving";

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        public String getProvider()
        {
            return provider;
        }

        public void setProvider(String provider)
        {
            this.provider = provider;
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

        public String getLocation()
        {
            return location;
        }

        public void setLocation(String location)
        {
            this.location = location;
        }

        public String getSecurityJsCode()
        {
            return securityJsCode;
        }

        public void setSecurityJsCode(String securityJsCode)
        {
            this.securityJsCode = securityJsCode;
        }

        public String getGeocodePath()
        {
            return geocodePath;
        }

        public void setGeocodePath(String geocodePath)
        {
            this.geocodePath = geocodePath;
        }

        public String getDrivingPath()
        {
            return drivingPath;
        }

        public void setDrivingPath(String drivingPath)
        {
            this.drivingPath = drivingPath;
        }
    }
}
