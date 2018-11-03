/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umbertopalazzini.s3zilla.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.umbertopalazzini.s3zilla.Main;
import com.umbertopalazzini.s3zilla.model.ProxyConfig;
import com.umbertopalazzini.s3zilla.model.S3AccountConfig;
import com.umbertopalazzini.s3zilla.service.S3Service;
import com.umbertopalazzini.s3zilla.view.AccountDialog;
import com.umbertopalazzini.s3zilla.view.ProxyDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 *
 * @author sascha
 */
public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class.getName());

    @FXML
    private ToggleGroup menubarlanguage;
    @FXML
    private RadioMenuItem menubarLanguageDe;
    @FXML
    private RadioMenuItem menubarLanguageEn;
    @FXML
    private Menu accountMenu;
    @FXML
    private Menu bucketMenu;
    private final Image checkedImage = new Image(MainController.class.getClassLoader().getResourceAsStream("images/checkmark.png"));
    private final List<Menu> accountMenuEntries = new ArrayList<>();
    private final List<Menu> bucketMenuEntries = new ArrayList<>();

    private SimpleObjectProperty<Bucket> selectedBucketProperty;
    private SimpleObjectProperty<ProxyConfig> proxyConfigProperty;
    private SimpleObjectProperty<S3AccountConfig> s3AccountConfigProperty;

    private Main main;
    private ResourceBundle resourceBundle;
    private S3Service s3Service;
    private ContentController contentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
	logger.debug("initialize");
	this.resourceBundle = resources;
	proxyConfigProperty = new SimpleObjectProperty<>();
	proxyConfigProperty.addListener(proxyChangeListener());
	s3AccountConfigProperty = new SimpleObjectProperty<>();
	s3AccountConfigProperty.addListener(s3AccountConfigChangeListener());

	selectedBucketProperty = new SimpleObjectProperty<>();
	selectedBucketProperty.addListener(s3BucketChangeListener());

	menubarLanguageDe.setUserData(Locale.GERMAN);
	menubarLanguageEn.setUserData(Locale.ENGLISH);

	menubarlanguage.getToggles().filtered(toggle -> resourceBundle.getLocale().equals(toggle.getUserData())).forEach(toggle -> toggle.setSelected(true));

	Platform.runLater(() -> {
	    logger.debug("Platform.runLater");
	    menubarlanguage.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
		    logger.debug("new language toggel val: '{}'", newValue);
		    if (newValue != null) {
			main.loadView((Locale) newValue.getUserData());
		    }
		}
	    });
	});
    }

    public void setMain(Main main) {
	this.main = main;
    }

    public void addS3Config() {
	Optional<S3AccountConfig> result = new AccountDialog(resourceBundle, null).showAndWait();

	result.ifPresent(config -> {
	    logger.debug("new config: '{}'", config);
	    Menu menu = createAccountMenuEntry(config, resourceBundle);
	    addS3AccountMenu(menu);
	}
	);
    }

    public void editProxySettings() {
	logger.debug("editProxySettings");
	Optional<ProxyConfig> result = new ProxyDialog(resourceBundle, proxyConfigProperty.getValue()).showAndWait();

	result.ifPresent(config -> {
	    logger.debug("new proxy config: '{}'", config);
	    proxyConfigProperty.setValue(config);
	}
	);
    }

    private void addS3AccountMenu(Menu menu) {
	accountMenu.getItems().add(menu);
	accountMenuEntries.add(menu);
    }

    private void updateBucketList() {
	bucketMenu.getItems().removeAll(bucketMenuEntries);
	bucketMenuEntries.clear();
	if (null != s3AccountConfigProperty.getValue()) {
	    bucketMenuEntries.addAll(genereateBucketMenuItems(getBucketList(), resourceBundle));
	    bucketMenu.getItems().addAll(bucketMenuEntries);
//	    bucketMenu.setDisable(false);
	} else {
//	    bucketMenu.setDisable(true);
	}
    }

    private List<Bucket> getBucketList() {
	try {
	    return s3Service.listBuckets();
	} catch (RuntimeException ex) {
	    logger.error("Could not genrate bucketlist", ex);
	    showAndWaitAlert(AlertType.ERROR, resourceBundle.getString("s3service.getBucketList.title"), resourceBundle.getString("s3service.getBucketList.content"), ex);
	}
	return new ArrayList<>();
    }

    private List<Menu> genereateBucketMenuItems(List<Bucket> buckets, ResourceBundle bundle) {
	if (null != buckets) {
	    return buckets.stream().map(bucket -> {
		MenuItem edit = new MenuItem(bundle.getString("menu.bucket.edit.label"));
		//edit.setOnAction(this::editAccountItemEvent);
		edit.setDisable(true);
		MenuItem delete = new MenuItem(bundle.getString("menu.bucket.delete.label"));
		//elete.setOnAction(this::deleteAccountItemEvent);
		delete.setDisable(true);
		Menu result = new Menu(bucket.getName(), null, edit, delete);
		result.setUserData(bucket);
		result.setOnAction(this::activateBucketItemEvent);
		return result;
	    }).collect(Collectors.toList());
	}
	return new ArrayList<>();
    }

    private ChangeListener<ProxyConfig> proxyChangeListener() {
	return new ChangeListener<ProxyConfig>() {
	    @Override
	    public void changed(ObservableValue<? extends ProxyConfig> observable, ProxyConfig oldValue, ProxyConfig newValue) {
		logger.debug("proxy config changed: '{}'", newValue);
		s3Service.setProxyConfig(newValue);
		selectedBucketProperty.setValue(null);
		updateBucketList();
	    }
	};
    }

    private ChangeListener<S3AccountConfig> s3AccountConfigChangeListener() {
	return new ChangeListener<S3AccountConfig>() {
	    @Override
	    public void changed(ObservableValue<? extends S3AccountConfig> observable, S3AccountConfig oldValue, S3AccountConfig newValue) {
		logger.debug("s3 account changed: '{}'", newValue);
		s3Service.setS3AccountConfig(newValue);
		selectedBucketProperty.setValue(null);
		updateBucketList();
	    }
	};
    }

    private ChangeListener<Bucket> s3BucketChangeListener() {
	return new ChangeListener<Bucket>() {
	    @Override
	    public void changed(ObservableValue<? extends Bucket> observable, Bucket oldValue, Bucket newValue) {
		logger.debug("bucket selection changed: '{}'", newValue);
		contentController.setSelecetedBucket(newValue);
	    }
	};
    }

    public Optional<ButtonType> showAndWaitAlert(Alert.AlertType type, String title, String content, Throwable ex) {
	Alert alert = new Alert(type);
	alert.setTitle(title);
	alert.setContentText(content);
        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

	if (null != ex) {
	    // Create expandable Exception.
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    ex.printStackTrace(pw);
	    String exceptionText = sw.toString();

	    Label label = new Label("The exception stacktrace was:");

	    TextArea textArea = new TextArea(exceptionText);
	    textArea.setEditable(false);
	    textArea.setWrapText(true);

	    textArea.setMaxWidth(Double.MAX_VALUE);
	    textArea.setMaxHeight(Double.MAX_VALUE);
	    GridPane.setVgrow(textArea, Priority.ALWAYS);
	    GridPane.setHgrow(textArea, Priority.ALWAYS);

	    GridPane expContent = new GridPane();
	    expContent.setMaxWidth(Double.MAX_VALUE);
	    expContent.add(label, 0, 0);
	    expContent.add(textArea, 0, 1);

	    // Set expandable Exception into the dialog pane.
	    alert.getDialogPane().setExpandableContent(expContent);
	}

	return alert.showAndWait();
    }

    private void activateBucketItemEvent(ActionEvent event) {
	logger.debug("activateBucketItemEvent: '{}'", event);
	Menu activateMenuEntry = (Menu) event.getSource();
	bucketMenuEntries.forEach(menuItem -> {
	    Menu entry = (Menu) menuItem;
	    if (entry.equals(activateMenuEntry)) {
		changeStateOfBucketMenu(entry, true);
		selectedBucketProperty.setValue((Bucket) entry.getUserData());
	    } else {
		changeStateOfBucketMenu(entry, false);
	    }
	});
	activateMenuEntry.getParentMenu().hide();
    }

    private void activateAccountItemEvent(ActionEvent event) {
	logger.debug("activateAccountItemEvent: '{}'", event);
	Menu activateMenuEntry = (Menu) ((MenuItem) event.getSource());
	accountMenuEntries.forEach(menuItem -> {
	    Menu entry = (Menu) menuItem;
	    if (entry.equals(activateMenuEntry)) {
		changeStateOfAccountMenu(entry, true);
		s3AccountConfigProperty.setValue((S3AccountConfig) entry.getUserData());
	    } else {
		changeStateOfAccountMenu(entry, false);
	    }
	});
	activateMenuEntry.getParentMenu().hide();
    }

    private void changeStateOfAccountMenu(Menu accountMenu, boolean active) {
	if (active) {
	    accountMenu.getItems().get(1).setDisable(true);
	    accountMenu.setStyle("-fx-font-weight: bold;");
	    accountMenu.setGraphic(new ImageView(checkedImage));
	} else {
	    accountMenu.getItems().get(1).setDisable(false);
	    accountMenu.setStyle("");
	    accountMenu.setGraphic(null);
	}
    }

    private void changeStateOfBucketMenu(Menu accountMenu, boolean active) {
	if (active) {
//	    accountMenu.getItems().get(1).setDisable(true);
	    accountMenu.setStyle("-fx-font-weight: bold;");
	    accountMenu.setGraphic(new ImageView(checkedImage));
	} else {
//	    accountMenu.getItems().get(1).setDisable(false);
	    accountMenu.setStyle("");
	    accountMenu.setGraphic(null);
	}
    }

    private void editAccountItemEvent(ActionEvent event) {
	logger.debug("editMenuEvent: '{}'", event);
	Menu menuItem = (Menu) ((MenuItem) event.getSource()).getParentMenu();
	Optional<S3AccountConfig> result = new AccountDialog(resourceBundle, (S3AccountConfig) menuItem.getUserData()).showAndWait();

	result.ifPresent(config -> {
	    logger.debug("new config: '{}'", config);
	    menuItem.setUserData(config);
	    if (null != menuItem.getGraphic()) {
		logger.debug("entry is active. have to update the s3 client");
		s3AccountConfigProperty.setValue(config);
	    }
	}
	);
    }

    private void deleteAccountItemEvent(ActionEvent event) {
	logger.debug("deleteMenuEvent: '{}'", event);
	accountMenu.getItems().remove(((MenuItem) event.getSource()).getParentMenu());
	accountMenuEntries.remove((Menu) ((MenuItem) event.getSource()).getParentMenu());
    }

    private Menu createAccountMenuEntry(S3AccountConfig userdata, ResourceBundle bundle) {
	MenuItem edit = new MenuItem(bundle.getString("menu.account.edit.label"));
	edit.setOnAction(this::editAccountItemEvent);
	MenuItem delete = new MenuItem(bundle.getString("menu.account.delete.label"));
	delete.setOnAction(this::deleteAccountItemEvent);
	Menu result = new Menu(userdata.getDisplayName(), null, edit, delete);
	result.setUserData(userdata);
	result.setOnAction(this::activateAccountItemEvent);
	return result;
    }

    public List<S3AccountConfig> getS3AccountConfigs() {
	return accountMenuEntries.stream().map(entry -> ((S3AccountConfig) entry.getUserData())).collect(Collectors.toList());
    }

    public S3AccountConfig getS3AccountConfig() {
	return s3AccountConfigProperty.getValue();
    }

    public void setS3AccountConfig(S3AccountConfig config) {
	logger.debug("setS3AccountConfig");
	s3AccountConfigProperty.setValue(config);
	if (config != null) {
	    accountMenuEntries.forEach(menuItem -> {
		Menu entry = (Menu) menuItem;
		if (config.equals(entry.getUserData())) {
		    changeStateOfAccountMenu(entry, true);
		}
	    });
	}
    }

    public void setS3AccountConfigs(List<S3AccountConfig> configs) {
	logger.debug("setS3AccountConfigs");
	configs.stream().filter(entry -> entry != null).map(entry -> createAccountMenuEntry(entry, resourceBundle)).forEach(this::addS3AccountMenu);
    }

    public ProxyConfig getProxyConfig() {
	return proxyConfigProperty.getValue();
    }

    public void setProxyConfig(ProxyConfig proxyConfig) {
	logger.debug("setProxyConfig");
	proxyConfigProperty.setValue(proxyConfig);
    }

    public void setS3Service(S3Service s3Service) {
	logger.debug("setS3Service");
	this.s3Service = s3Service;
    }

    public void setContentController(ContentController contentController) {
	this.contentController = contentController;
    }

    public Stage getPrimaryStage() {
	return main.getPrimaryStage();
    }

    public Bucket getSelectedBucket() {
	return selectedBucketProperty.getValue();
    }

    public void setSelectedBucket(Bucket selectedBucket) {
	if (null != selectedBucket) {
	    bucketMenuEntries.forEach(menuItem -> {
		if (((Bucket) menuItem.getUserData()).getName().equals(selectedBucket.getName())) {
		    changeStateOfBucketMenu(menuItem, true);
		}
	    });
	}
        this.selectedBucketProperty.setValue(selectedBucket);
    }

}
