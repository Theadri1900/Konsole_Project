package com.minecraftnews.theadri1900;

import java.security.SecureRandom;

public class Utils {
	
	/* Fonction de génération d'un mot de passe.  */
	/** 
	 * 
	 * @param numberChar
	 * 			The number of character in your password
	 * @param numbers
	 * 			If you want numbers in your password
	 * @param specialChar
	 * 			If you want special character like *#@?!
	 * @return a password securely genereted with your configuration
	 */
	public static String generatePassword(int numberChar, boolean numbers, boolean specialChar){
		String alphabet = "azertyuiopqsdfghjklmwxcvbnAZERTYUIOPQSDFGHJKLMWXCVBN"; // initialisation de l'alphabet
		if(numbers) alphabet = alphabet.concat("123456789"); // ajout ou non des nombres
		if(specialChar) alphabet = alphabet.concat("*#@?!"); // ajout ou non des charactère spéciaux
		int lenght = alphabet.length(); //on récupère la longeur totale
		SecureRandom random = new SecureRandom(); // initialisation du secureRandom
		StringBuilder build = new StringBuilder(numberChar); // initialisation du stringbuilder avec la taille déjà défini
		for(int i = 0 ; i < numberChar ; i++){ // tant que la longeur finale n'est pas atteinte
			build.append(alphabet.charAt(random.nextInt(lenght))); //on ajoute au mot de passe final un charactère au pif
		}
		return build.toString(); //on retourne le build en string
	}
}
