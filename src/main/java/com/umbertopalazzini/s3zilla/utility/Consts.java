package com.umbertopalazzini.s3zilla.utility;

import java.io.File;

public class Consts {

    public static final String SETTINGS_FILE = System.getProperty("user.home") + File.separator + ".s3fxviewer.properties";

    public static final String DOWNLOAD_PATH = System.getProperty("user.home") + File.separator + "S3Zilla" + File.separator;

    public static final long KB = 1024L;
    public static final long MB = 1024L * 1024L;
    public static final long GB = 1024L * 1024L * 1024L;
    public static final long TB = 1024L * 1024L * 1024L * 1024L;

    // TODO: Replace these three constants with internazionalized strings.
    public static final String CANCEL = "CANCEL";
    public static final String PAUSE = "PAUSE";
    public static final String RESUME = "RESUME";

    public static final String SETTINGS_LANGUAGE = "language";

    public static final String SETTINGS_VIEW_HEIGHT = "view.height";
    public static final String SETTINGS_VIEW_WIDTH = "view.width";
    public static final String SETTINGS_VIEW_POSX = "view.posx";
    public static final String SETTINGS_VIEW_POSY = "view.posy";

    public static final String SETTINGS_S3ACCOUNTCONFIG_PREFIX = "s3accountconfig.";
    public static final String SETTINGS_S3ACCOUNTCONFIG_ACTIVE = SETTINGS_S3ACCOUNTCONFIG_PREFIX + "active";

    public static final String SETTINGS_PROXYCONFIG = "proxyconfig";

    public static final String SETTINGS_SELECTED_BUCKET = "selectedBucket";
}
