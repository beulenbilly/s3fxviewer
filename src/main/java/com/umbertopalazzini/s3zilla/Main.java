package com.umbertopalazzini.s3zilla;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.umbertopalazzini.s3zilla.controller.ContentController;
import com.umbertopalazzini.s3zilla.controller.MainController;
import com.umbertopalazzini.s3zilla.model.ProxyConfig;
import com.umbertopalazzini.s3zilla.model.S3AccountConfig;
import com.umbertopalazzini.s3zilla.service.S3Service;
import com.umbertopalazzini.s3zilla.utility.Consts;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());
    private final static Locale defaultLocale = Locale.ENGLISH;

    private Stage primaryStage;
    private ResourceBundle bundle;
    private Properties settings;
    private MainController mainController;
    private ContentController contentController;
    private ObjectMapper objectMapper;
    private S3Service s3Service;

    public Main() {
	objectMapper = new ObjectMapper();
	objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
	objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void start(Stage primaryStage) {
	// Initializes the stage and show it.
	this.primaryStage = primaryStage;

	settings = loadSettings(Consts.SETTINGS_FILE);
	Locale locale = loadLocale(settings, defaultLocale);

	loadView(locale);

	primaryStage.setTitle("S3FxViewer");
	primaryStage.setMinWidth(800);
	primaryStage.setMinHeight(614);
	restoreViewSettings(primaryStage);
	primaryStage.setResizable(true);

	primaryStage.show();
    }

    public void loadView(Locale locale) {
	try {
	    if (null != mainController) {
		saveSettings();
	    }
	    if (null != s3Service) {
		s3Service.invalidate();
	    }
	    // Retrieves the fxml file containing the layout of the GUI.
	    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout_main.fxml"));
	    bundle = ResourceBundle.getBundle("locales.locale", locale);
	    // Loads the bundle/language.
	    fxmlLoader.setResources(bundle);

	    BorderPane root = fxmlLoader.load();
	    // Loads the fxml.
	    Scene scene = new Scene(root);
	    scene.getStylesheets().add("/custom-styles.css");

	    // Sets the controller Main reference to this class.
	    mainController = fxmlLoader.getController();

	    mainController.setMain(this);
	    s3Service = new S3Service();
	    mainController.setS3Service(s3Service);

	    fxmlLoader = new FXMLLoader(getClass().getResource("/layout_content.fxml"));
	    fxmlLoader.setResources(bundle);
	    SplitPane splitPane = fxmlLoader.load();

	    root.setCenter(splitPane);
	    contentController = fxmlLoader.getController();
	    contentController.setS3Service(s3Service);
	    contentController.setMainController(mainController);
	    mainController.setContentController(contentController);

	    restoreProxySettings();
	    restoreAccountConfigs();
	    restoreSelectBucket();

	    primaryStage.setScene(scene);

	    settings.setProperty(Consts.SETTINGS_LANGUAGE, locale.getLanguage());
	} catch (IOException ex) {
	    logger.error("Could not init app", ex);
	}
    }

    private Properties loadSettings(String path) {
	Properties prop = new Properties();
	try (FileInputStream fis = new FileInputStream(path)) {
	    prop.load(fis);
	} catch (IOException ex) {
	    logger.debug("could not load settings file", ex);
	}
	return prop;
    }

    private Locale loadLocale(Properties settings, Locale defaultLocale) {
	return Locale.forLanguageTag(settings.getProperty(Consts.SETTINGS_LANGUAGE, defaultLocale.getLanguage()));
    }

    /**
     * Returns the stage in order to be accessed from other controller(s) to
     * give their (GUI) components a 'father'.
     *
     * @return
     */
    public Stage getPrimaryStage() {
	return this.primaryStage;
    }

    public static void main(String[] args) {
	launch(args);
    }

    @Override
    public void stop() throws Exception {
	super.stop();
	saveSettings();
	settings.store(new FileOutputStream(new File(Consts.SETTINGS_FILE)), "S3FxViewer settings");
	if (null != s3Service) {
	    s3Service.invalidate();
	}
    }

    private void saveSettings() {
	saveViewSettings();
	saveAccountConfigs();
	saveProxySettings();
	saveSelectBucket();
    }

    private void restoreViewSettings(Stage primaryStage1) {
	double height = getDouble(settings, Consts.SETTINGS_VIEW_HEIGHT);
	if (height > 0) {
	    primaryStage1.setHeight(height);
	}
	double width = getDouble(settings, Consts.SETTINGS_VIEW_WIDTH);
	if (width > 0) {
	    primaryStage1.setWidth(width);
	}
	double posx = getDouble(settings, Consts.SETTINGS_VIEW_POSX);
	if (posx > 0) {
	    primaryStage1.setX(posx);
	}
	double posy = getDouble(settings, Consts.SETTINGS_VIEW_POSY);
	if (posy > 0) {
	    primaryStage1.setY(posy);
	}
    }

    private void saveViewSettings() {
	settings.setProperty(Consts.SETTINGS_VIEW_HEIGHT, String.valueOf(primaryStage.getHeight()));
	settings.setProperty(Consts.SETTINGS_VIEW_WIDTH, String.valueOf(primaryStage.getWidth()));
	settings.setProperty(Consts.SETTINGS_VIEW_POSX, String.valueOf(primaryStage.getX()));
	settings.setProperty(Consts.SETTINGS_VIEW_POSY, String.valueOf(primaryStage.getY()));
    }

    private void saveAccountConfigs() {
	//first delete
	Enumeration<Object> keys = settings.keys();
	while (keys.hasMoreElements()) {
	    String key = (String) keys.nextElement();
	    if (key.startsWith("s3accountconfig.")) {
		settings.remove(key);
	    }
	}
	//adding currentSettings
	List<S3AccountConfig> configuredSettings = mainController.getS3AccountConfigs();
	for (int i = 0; i < configuredSettings.size(); i++) {
	    try {
		settings.setProperty(Consts.SETTINGS_S3ACCOUNTCONFIG_PREFIX + i, objectMapper.writeValueAsString(configuredSettings.get(i)));
		if ((null != mainController.getS3AccountConfig()) && (mainController.getS3AccountConfig().equals(configuredSettings.get(i)))) {
		    settings.setProperty(Consts.SETTINGS_S3ACCOUNTCONFIG_ACTIVE, String.valueOf(i));
		}
	    } catch (JsonProcessingException ex) {
		logger.warn("could not transform account config", ex);
	    }
	}
    }

    private void restoreAccountConfigs() {
	List<S3AccountConfig> configuredSettings = new ArrayList<>();
	Map<Integer, S3AccountConfig> restoredAccounts = new HashMap<>();
	Enumeration<Object> keys = settings.keys();
	int activeAccount = -1;
	while (keys.hasMoreElements()) {
	    String key = (String) keys.nextElement();
	    if (key.startsWith(Consts.SETTINGS_S3ACCOUNTCONFIG_PREFIX)) {
		if (key.equals(Consts.SETTINGS_S3ACCOUNTCONFIG_ACTIVE)) {
		    try {
			activeAccount = Integer.parseInt(settings.getProperty(key, "-1"));
		    } catch (NumberFormatException ex) {
			logger.warn("could not restore active account", ex);
		    }
		} else {
		    try {
			int index = Integer.parseInt(key.split("\\.")[1]);
			S3AccountConfig config = objectMapper.readValue(settings.getProperty(key), S3AccountConfig.class);
			restoredAccounts.put(index, config);
		    } catch (RuntimeException | IOException ex) {
			logger.warn("could not restore s3account setting " + key, ex);
		    }
		}
	    }
	}
	if (!restoredAccounts.isEmpty()) {
	    restoredAccounts.keySet().stream().forEachOrdered(key -> configuredSettings.add(restoredAccounts.get(key)));
	}
	if (!configuredSettings.isEmpty()) {
	    mainController.setS3AccountConfigs(configuredSettings);
	}
	if ((activeAccount >= 0) && (null != configuredSettings.get(activeAccount))) {
	    mainController.setS3AccountConfig(configuredSettings.get(activeAccount));
	}
    }

    private void saveProxySettings() {
	logger.debug("saveProxySettings");
	settings.remove(Consts.SETTINGS_PROXYCONFIG);

	ProxyConfig config = mainController.getProxyConfig();
	if (null != config) {
	    try {
		settings.setProperty(Consts.SETTINGS_PROXYCONFIG, objectMapper.writeValueAsString(config));
	    } catch (JsonProcessingException ex) {
		logger.warn("could not serialize proxy config", ex);
	    }
	}
    }

    private void restoreProxySettings() {
	logger.debug("restoreProxySettings");
	if (StringUtils.hasValue(settings.getProperty(Consts.SETTINGS_PROXYCONFIG))) {
	    try {
		ProxyConfig config = objectMapper.readValue(settings.getProperty(Consts.SETTINGS_PROXYCONFIG), ProxyConfig.class);
		mainController.setProxyConfig(config);
	    } catch (RuntimeException | IOException ex) {
		logger.warn("could not restore proxyconfig", ex);
	    }
	}
    }

    private void saveSelectBucket() {
	try {
	    settings.setProperty(Consts.SETTINGS_SELECTED_BUCKET, objectMapper.writeValueAsString(mainController.getSelectedBucket()));
	} catch (JsonProcessingException ex) {
	    logger.warn("could not save selectedBucket", ex);
	}
    }

    private void restoreSelectBucket() {
        Bucket bucket = null;
	if (null != settings.getProperty(Consts.SETTINGS_SELECTED_BUCKET)) {
	    try {
		bucket = objectMapper.readValue(settings.getProperty(Consts.SETTINGS_SELECTED_BUCKET), Bucket.class);
	    } catch (RuntimeException | IOException ex) {
		logger.warn("could not read selectedBucket", ex);
	    }
	}
        mainController.setSelectedBucket(bucket);
    }

    private double getDouble(Properties settings, String key) {
	try {
	    return Double.parseDouble(settings.getProperty(key, "0.0"));
	} catch (NumberFormatException ex) {
	    logger.warn("could not transform double", ex);
	}
	return 0D;
    }

}
