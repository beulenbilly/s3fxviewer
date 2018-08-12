/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.model;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 *
 * @author sascha
 */
public class S3ObjectWrapperTreeItem extends TreeItem<S3ObjectWrapper> {

    private final boolean leaf;
    private boolean loaded;

    public S3ObjectWrapperTreeItem(boolean leaf) {
	this(leaf, null);
    }

    public S3ObjectWrapperTreeItem(boolean leaf, S3ObjectWrapper value) {
	this(leaf, false, value);
    }

    public S3ObjectWrapperTreeItem(boolean leaf, boolean loaded, S3ObjectWrapper value) {
	this(leaf, loaded, value, null);
    }

    public S3ObjectWrapperTreeItem(boolean leaf, boolean loaded, S3ObjectWrapper value, Node graphic) {
	super(value, graphic);
	this.leaf = leaf;
	this.loaded = loaded;
    }

    @Override
    public boolean isLeaf() {
	return leaf;
    }

    public boolean isLoaded() {
	return loaded;
    }

    public void setLoaded(boolean loaded) {
	this.loaded = loaded;
    }

}
