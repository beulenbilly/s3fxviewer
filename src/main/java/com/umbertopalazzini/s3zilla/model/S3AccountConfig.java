/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.model;

import com.amazonaws.regions.Regions;
import java.io.Serializable;

/**
 *
 * @author sascha
 */
public class S3AccountConfig implements Serializable {

    private String displayName;
    private String accessKey;
    private String secretKey;
    private Regions region;

    public S3AccountConfig() {
    }

    public S3AccountConfig(String displayName, String accessKey, String secretKey, Regions region) {
	this.displayName = displayName;
	this.accessKey = accessKey;
	this.secretKey = secretKey;
	this.region = region;
    }

    public String getDisplayName() {
	return displayName;
    }

    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }

    public String getAccessKey() {
	return accessKey;
    }

    public void setAccessKey(String accessKey) {
	this.accessKey = accessKey;
    }

    public String getSecretKey() {
	return secretKey;
    }

    public void setSecretKey(String secretKey) {
	this.secretKey = secretKey;
    }

    public Regions getRegion() {
	return region;
    }

    public void setRegion(Regions region) {
	this.region = region;
    }

    @Override
    public String toString() {
	return "S3BucketConfig{" + "displayName=" + displayName + ", accessKey=" + accessKey + ", secretKey=" + secretKey + ", region=" + region + '}';
    }
}
