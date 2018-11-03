/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.model;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.Date;

/**
 *
 * @author sascha
 */
public class S3ObjectWrapper {

    private final S3ObjectSummary s3ObjectSummary;

    public S3ObjectWrapper(S3ObjectSummary s3ObjectSummary) {
	this.s3ObjectSummary = s3ObjectSummary;
    }

    public S3ObjectSummary getS3ObjectSummary() {
	return s3ObjectSummary;
    }

    public String getName() {
	String result = s3ObjectSummary.getKey();
	if (result.endsWith("/")) {
	    result = result.substring(0, (result.length() - 1));
	}
	String[] splits = result.split("/");
	return splits[splits.length - 1];
    }

    public String getKey() {
	return null != s3ObjectSummary ? s3ObjectSummary.getKey() : "object.null";
    }

    public long getSize() {
	return null != s3ObjectSummary ? s3ObjectSummary.getSize() : 0L;
    }

    public Date getLastModified() {
	return null != s3ObjectSummary ? s3ObjectSummary.getLastModified() : new Date();
    }

    public String getBucketName() {
	return s3ObjectSummary.getBucketName();
    }

    @Override
    public String toString() {
	return "S3Object{" + ", s3ObjectSummary=" + s3ObjectSummary + '}';
    }

}
