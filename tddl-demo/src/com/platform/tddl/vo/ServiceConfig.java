package com.platform.tddl.vo;

/**
 * Created with IntelliJ IDEA.
 * User: tomp
 * Date: 13-5-31
 * Time: 下午3:05
 * To change this template use File | Settings | File Templates.
 */
public class ServiceConfig {

    private Integer id;

    private String name;

    private String protocol;

    private String template;

    private String converter;

    private String url;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getConverter() {
        return converter;
    }

    public void setConverter(String converter) {
        this.converter = converter;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}