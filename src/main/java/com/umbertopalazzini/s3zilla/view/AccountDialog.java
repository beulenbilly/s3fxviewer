/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.view;

import com.amazonaws.regions.Regions;
import com.umbertopalazzini.s3zilla.model.S3AccountConfig;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ResourceBundle;

/**
 *
 * @author sascha
 */
public class AccountDialog extends AbstractDialog<S3AccountConfig> {

    private TextField displayName;
    private TextField accessKey;
    private PasswordField secretKey;
    private ComboBox<Regions> regionsComboBox;

    public AccountDialog(ResourceBundle resourceBundle, S3AccountConfig config) {
	super();
	init(resourceBundle, config);
    }

    private void init(ResourceBundle resourceBundle, S3AccountConfig config) {
	setTitle(resourceBundle.getString("dialog.bucket.title"));
	setHeaderText(resourceBundle.getString("dialog.bucket.content"));
	// Set the button types.
	final ButtonType applyButtonType = ButtonType.APPLY; //new ButtonType("Login", ButtonData.OK_DONE);
	getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

	GridPane grid = new GridPane();
	grid.setHgap(10);
	grid.setVgap(10);
	grid.setPadding(new Insets(20, 10, 10, 10));

	displayName = new TextField();
	displayName.setPromptText(resourceBundle.getString("dialog.bucket.displayName.prompttext"));
	configure(displayName);

	accessKey = new TextField();
	accessKey.setPromptText(resourceBundle.getString("dialog.bucket.accessKey.prompttext"));
	configure(accessKey);

	secretKey = new PasswordField();
	secretKey.setPromptText(resourceBundle.getString("dialog.bucket.secretKey.prompttext"));
	configure(secretKey);

	regionsComboBox = new ComboBox<>(FXCollections.observableArrayList(Regions.values()));
	regionsComboBox.setPromptText(resourceBundle.getString("dialog.bucket.regionsComboBox.prompttext"));
	configure(regionsComboBox);
	regionsComboBox.setCellFactory(cell -> new ListCell<Regions>() {
	    @Override
	    public void updateItem(Regions region, boolean empty) {
		if (!empty) {
		    setText(String.format("%s [%s]", region.getDescription(), region.getName()));
		}
		super.updateItem(region, empty);
	    }

	});

	grid.add(new Label(resourceBundle.getString("dialog.bucket.displayName.label")), 0, 0);
	grid.add(displayName, 1, 0);
	grid.add(new Label(resourceBundle.getString("dialog.bucket.accessKey.label")), 0, 1);
	grid.add(accessKey, 1, 1);
	grid.add(new Label(resourceBundle.getString("dialog.bucket.secretKey.label")), 0, 2);
	grid.add(secretKey, 1, 2);
	grid.add(new Label(resourceBundle.getString("dialog.bucket.regionsComboBox.label")), 0, 3);
	grid.add(regionsComboBox, 1, 3);

	// Enable/Disable login button depending on whether a username was entered.
	Node applyButton = getDialogPane().lookupButton(applyButtonType);
	applyButton.setDisable(true);

	// Do some validation (using the Java 8 lambda syntax).
	displayName.textProperty().addListener((observable, oldValue, newValue) -> {
	    applyButton.setDisable(newValue.trim().isEmpty());
	}
	);

	getDialogPane().setContent(grid);

	// Convert the result to a username-password-pair when the login button is clicked.
	setResultConverter(dialogButton -> {
	    if (dialogButton == applyButtonType) {
		return new S3AccountConfig(displayName.getText(), accessKey.getText(), secretKey.getText(), regionsComboBox.getValue());
	    }
	    return null;
	}
	);

	if (null != config) {
	    displayName.setText(config.getDisplayName());
	    accessKey.setText(config.getAccessKey());
	    secretKey.setText(config.getSecretKey());
	    regionsComboBox.setValue(config.getRegion());
	}
    }
}
