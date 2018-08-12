package com.umbertopalazzini.s3zilla.utility;

public class SizeConverter {

    private static final String[] units = {"B", "kB", "MB", "GB", "TB"};

    /**
     * Converts a given size (integer number) to a formatted string with the
     * size suffix.
     *
     * @param size
     * @return
     */
    public static String format(long size) {
	int digits = (int) (Math.log10(size) / Math.log10(1024));

	if (size < 0) {
	    return "NAN";
	}
	if (size == 0) {
	    return "0 B";
	}
	return String.format("%.2f %s", (size / Math.pow(1024, digits)), units[digits]);
    }
}
