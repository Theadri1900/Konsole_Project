package com.minecraftnews.theadri1900;

public class FileLangNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileLangNotFoundException(){
		System.out.println("[Konsole] Error : the language file specified on the config file cannot be found or the default language file doesn't exist !");
		printStackTrace();
	}
}
