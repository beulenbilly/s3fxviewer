/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.model;

/**
 *
 * @author sascha
 */
public class ProxyConfig {

    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    public ProxyConfig() {
    }

    public ProxyConfig(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
	this.proxyHost = proxyHost;
	this.proxyPort = proxyPort;
	this.proxyUsername = proxyUsername;
	this.proxyPassword = proxyPassword;
    }

    public String getProxyHost() {
	return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
	this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
	return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
	this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
	return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
	this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
	return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
	this.proxyPassword = proxyPassword;
    }

    @Override
    public String toString() {
	return "ProxyConfig{" + "proxyHost=" + proxyHost + ", proxyPort=" + proxyPort + ", proxyUsername=" + proxyUsername + '}';
    }

}
