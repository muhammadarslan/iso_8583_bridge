package com.avcora.iso8583.bridge.listener;

/**
 * @author: daniel
 */
public class ClientConfiguration {

    private Integer port;
    private String description;

    public ClientConfiguration(Integer port, String description) {
        this.port = port;
        this.description = description;
    }

    public ClientConfiguration() {
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
