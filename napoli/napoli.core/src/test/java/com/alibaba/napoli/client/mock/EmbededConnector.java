package com.alibaba.napoli.client.mock;


import com.alibaba.napoli.client.connector.NapoliConnector;
import org.springframework.beans.factory.BeanFactory;

public class EmbededConnector extends NapoliConnector {
    private String config;
    private EmbededConfigService configService;


    public void initConfigService() {
        if (config == null) {
            throw new RuntimeException("config con not be null.");
        }
        configService = new EmbededConfigService(config);
    }

    public EmbededConnector(String config) {
        this.config = config;
    }

    public EmbededConnector() {
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }



    public BeanFactory getBeanFactory() {
        return ((EmbededConfigService) configService).getBeanFactory();
    }
}
