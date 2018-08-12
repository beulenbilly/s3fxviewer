/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.view;

import javafx.scene.control.Control;
import javafx.scene.control.Dialog;

/**
 *
 * @author sascha
 */
public class AbstractDialog<T> extends Dialog<T> {

    private final double preferredWidth = 350D;

    public AbstractDialog() {
	super();
    }

    protected void configure(Control control) {
	control.setPrefWidth(preferredWidth);
    }

}
