/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.view;

import com.amazonaws.util.StringUtils;
import com.umbertopalazzini.s3zilla.model.ProxyConfig;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 *
 * @author sascha
 */
public class ProxyDialog extends AbstractDialog<ProxyConfig> {

    private TextField proxyHost;
    private TextField proxyPort;
    private TextField proxyUser;
    private PasswordField proxyPassword;

    public ProxyDialog(ResourceBundle resourceBundle, ProxyConfig proxyConfig) {
	super();
	init(resourceBundle, proxyConfig);
    }

    private void init(ResourceBundle resourceBundle, ProxyConfig proxyConfig) {
	setTitle(resourceBundle.getString("dialog.proxy.title"));
	setHeaderText(resourceBundle.getString("dialog.proxy.content"));
	// Set the button types.
	final ButtonType applyButtonType = ButtonType.APPLY; //new ButtonType("Login", ButtonData.OK_DONE);
	getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

	GridPane grid = new GridPane();
	grid.setHgap(10);
	grid.setVgap(10);
	grid.setPadding(new Insets(20, 10, 10, 10));

	proxyHost = new TextField();
	proxyHost.setPromptText(resourceBundle.getString("dialog.proxy.proxyHost.promttext"));
	configure(proxyHost);

	proxyPort = new TextField();
	configure(proxyPort);
	proxyPort.setPromptText(resourceBundle.getString("dialog.proxy.proxyPort.promttext"));
	proxyPort.textProperty().addListener((observable, oldValue, newValue) -> {
	    if (!newValue.matches("\\d*")) {
		proxyPort.setText(oldValue);

	    }
	});

	proxyUser = new PasswordField();
	configure(proxyUser);
	proxyUser.setPromptText(resourceBundle.getString("dialog.proxy.proxyUser.promttext"));

	proxyPassword = new PasswordField();
	configure(proxyPassword);
	proxyPassword.setPromptText(resourceBundle.getString("dialog.proxy.proxyPassword.promttext"));

	if (null != proxyConfig) {
	    proxyHost.setText(proxyConfig.getProxyHost());
	    proxyPort.setText(String.valueOf(proxyConfig.getProxyPort()));
	    proxyUser.setText(proxyConfig.getProxyUsername());
	    proxyPassword.setText(proxyConfig.getProxyPassword());
	}

	grid.add(new Label(resourceBundle.getString("dialog.proxy.proxyHost.label")), 0, 0);
	grid.add(proxyHost, 1, 0);
	grid.add(new Label(resourceBundle.getString("dialog.proxy.proxyPort.label")), 0, 1);
	grid.add(proxyPort, 1, 1);
	grid.add(new Label(resourceBundle.getString("dialog.proxy.proxyUser.label")), 0, 2);
	grid.add(proxyUser, 1, 2);
	grid.add(new Label(resourceBundle.getString("dialog.proxy.proxyPassword.label")), 0, 3);
	grid.add(proxyPassword, 1, 3);

	getDialogPane().setContent(grid);

	// Convert the result to a username-password-pair when the login button is clicked.
	setResultConverter(dialogButton -> {
	    if (dialogButton == applyButtonType) {
		return new ProxyConfig(proxyHost.getText(), StringUtils.hasValue(proxyPort.getText()) ? Integer.parseInt(proxyPort.getText()) : -1, proxyUser.getText(), proxyPassword.getText());
	    }
	    return null;
	}
	);
    }
}
