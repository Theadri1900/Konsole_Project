package com.minecraftnews.theadri1900;

public class ErrorReadingFileException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ErrorReadingFileException(){
		System.out.println("[Konsole] Error : Impossible to read the specified file ! Error while parsing or reading ...");
		printStackTrace();
	}
}
