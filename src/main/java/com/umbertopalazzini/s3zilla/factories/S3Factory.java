/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.factories;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.util.StringUtils;
import com.umbertopalazzini.s3zilla.model.ProxyConfig;
import com.umbertopalazzini.s3zilla.model.S3AccountConfig;
import com.umbertopalazzini.s3zilla.utility.Consts;

/**
 *
 * @author sascha
 */
public class S3Factory {

    public static AmazonS3 createS3Client(ProxyConfig proxyConfig, S3AccountConfig s3AccountConfig) {
	ClientConfiguration clientConfiguration = new ClientConfiguration();

	clientConfiguration.setMaxErrorRetry(10);
	clientConfiguration.setRetryPolicy(PredefinedRetryPolicies.getDefaultRetryPolicyWithCustomMaxRetries(10));
        clientConfiguration.setConnectionTimeout(2500);
        clientConfiguration.setRequestTimeout(10000);

	if ((null != proxyConfig) && (StringUtils.hasValue(proxyConfig.getProxyHost()))) {
	    clientConfiguration.setProxyHost(proxyConfig.getProxyHost());
	    if (proxyConfig.getProxyPort() > 0) {
		clientConfiguration.setProxyPort(proxyConfig.getProxyPort());
	    }
	    if (StringUtils.hasValue(proxyConfig.getProxyUsername())) {
		clientConfiguration.setProxyUsername(proxyConfig.getProxyUsername());
		if (StringUtils.hasValue(proxyConfig.getProxyPassword())) {
		    clientConfiguration.setProxyPassword(proxyConfig.getProxyPassword());
		}
	    }
	}

	AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withClientConfiguration(clientConfiguration);
	if (null != s3AccountConfig) {
	    if (null != s3AccountConfig.getRegion()) {
		builder.withRegion(s3AccountConfig.getRegion());
	    }
	    if (StringUtils.hasValue(s3AccountConfig.getAccessKey()) && StringUtils.hasValue(s3AccountConfig.getSecretKey())) {
		builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(s3AccountConfig.getAccessKey(), s3AccountConfig.getSecretKey())));
	    }
	}
	return builder.build();
    }

    public static TransferManager transferManager(AmazonS3 amazonS3) {
	return TransferManagerBuilder
		.standard()
		.withMultipartUploadThreshold(5 * Consts.MB)
		.withMultipartCopyThreshold(25 * Consts.MB)
		.withDisableParallelDownloads(false)
		.withS3Client(amazonS3)
		.build();
    }

}
