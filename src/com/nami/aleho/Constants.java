package com.nami.aleho;

import java.io.File;
import android.os.Environment;

public class Constants {
	public static final double VERSION = 2.5;
	public static final String VERSION_URL = "https://github.com/ShahNami/ALeho/tags";
	public static final String GITHUB_URL = "https://github.com/ShahNami/ALeho/releases/download/";
	public static final String SAVE_AS = "ALeho";
	public static final String DOWNLOAD_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "Download" + File.separator;// "/sdcard/Download/";
	public static final boolean DEBUG = false;
	private Constants() {

	}
}