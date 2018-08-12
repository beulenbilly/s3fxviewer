/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.umbertopalazzini.s3zilla.factories.S3Factory;
import com.umbertopalazzini.s3zilla.model.ProxyConfig;
import com.umbertopalazzini.s3zilla.model.S3AccountConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author sascha
 */
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class.getName());

    private AmazonS3 amazonS3Client;
    private TransferManager transferManager;
    private ProxyConfig proxyConfig;
    private S3AccountConfig s3AccountConfig;

    public void setProxyConfig(ProxyConfig proxyConfig) {
        invalidate();
        this.proxyConfig = proxyConfig;
    }

    public void setS3AccountConfig(S3AccountConfig s3AccountConfig) {
        invalidate();
        this.s3AccountConfig = s3AccountConfig;
    }

    /**
     * Lists the buckets.
     *
     * @return
     */
    public List<Bucket> listBuckets() {
        return getAmazonS3Client().listBuckets();
    }

    /**
     * lists the bucket content.
     *
     * @param bucketName the bucket
     * @param folder the folde aka prefix
     * @return the result from amazon
     */
    public ListObjectsV2Result listObjects(String bucketName, String folder) {
        logger.debug("listFiles: '{}', '{}'", bucketName, folder);
        ListObjectsV2Request listRequest = new ListObjectsV2Request().withBucketName(bucketName);

        if (StringUtils.hasValue(folder)) {
            listRequest = listRequest.withPrefix(folder);
        }
        listRequest = listRequest.withDelimiter("/");

        ListObjectsV2Result listResult = amazonS3Client.listObjectsV2(listRequest);
        logger.debug("listFiles.result.getCommonPrefixes: {}", listResult.getCommonPrefixes());
        logger.debug("listFiles.result.getObjectSummaries: {}", listResult.getObjectSummaries());
        return listResult;
    }

    public void deleteObject(S3ObjectSummary summary) {
        logger.debug("deleting object: '{}'", summary);
        amazonS3Client.deleteObject(summary.getBucketName(), summary.getKey());
    }

    public void deleteObjects(S3ObjectSummary summary) {
        logger.debug("deleting prefix: '{}'", summary);
        ListObjectsV2Request listRequest = new ListObjectsV2Request().withBucketName(summary.getBucketName()).withPrefix(summary.getKey());
        ListObjectsV2Result listResult = amazonS3Client.listObjectsV2(listRequest);
        if (!CollectionUtils.isNullOrEmpty(listResult.getObjectSummaries())) {
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(summary.getBucketName()).withKeys(listResult.getObjectSummaries().stream().map(sum -> new DeleteObjectsRequest.KeyVersion(sum.getKey())).collect(Collectors.toList()));
            amazonS3Client.deleteObjects(deleteRequest);
        }
    }

    public void invalidate() {
        if (null != transferManager) {
            transferManager.shutdownNow(true);
        }
        transferManager = null;
        if (null != amazonS3Client) {
            amazonS3Client.shutdown();
        }
        amazonS3Client = null;
    }

    public AmazonS3 getAmazonS3Client() {
        if (null == amazonS3Client) {
            amazonS3Client = S3Factory.createS3Client(proxyConfig, s3AccountConfig);
        }
        return amazonS3Client;
    }

    public TransferManager getTransferManager() {
        if (null == transferManager) {
            transferManager = S3Factory.transferManager(getAmazonS3Client());
        }
        return transferManager;
    }
}
