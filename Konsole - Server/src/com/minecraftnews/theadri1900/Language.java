package com.minecraftnews.theadri1900;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Hashtable;

public class Language {

	/*
	 *  Objet pour gérer les langues, en JAVA 1.7 :)
	 * 
	 */


	private Hashtable<Integer,String> dictionnaire;
	public final static String DEFAULT_LANG = "english";
	public final static String ALTERNATIVE_LANG = "french";
	public final static String MODIFIABLE_LANG = "own";
	private String prefix = "";

	public Language(String path, String lang) throws IOException, FileLangNotFoundException, ErrorReadingFileException {
		initialize(path, lang);
	}
	
	public Language(String path, String lang, String prefix) throws IOException, FileLangNotFoundException, ErrorReadingFileException {
		this.prefix = prefix;
		initialize(path, lang);
	}

	
	private void initialize(String path, String lang) throws IOException, FileLangNotFoundException, ErrorReadingFileException{
		// problème de syntaxe automatiquement réglé:
		if(lang.contains(".yml")){
			lang.replace(".yml", "");
		}
		try{
		Files.createDirectory(Paths.get(path));
		}catch(FileAlreadyExistsException e){
			
		}
		
		if(!Files.isDirectory(Paths.get(path))){
			throw new IOException("Path must be a directory");
		}


		try {
			// si les fichiers de langues basiques n'existent pas, alors on les créé ...
			if(!Files.exists(Paths.get(path + DEFAULT_LANG + ".yml"))){
				Files.copy(getClass().getResourceAsStream("/" + DEFAULT_LANG +".yml"), Paths.get(path + DEFAULT_LANG + ".yml"), StandardCopyOption.REPLACE_EXISTING);
			}

			if(ALTERNATIVE_LANG != null && !ALTERNATIVE_LANG.equalsIgnoreCase("")){
				// si il y a une langue alternative ...
				if(!Files.exists(Paths.get(path + ALTERNATIVE_LANG + ".yml"))){
					Files.copy(getClass().getResourceAsStream("/" + ALTERNATIVE_LANG +".yml"), Paths.get(path + ALTERNATIVE_LANG + ".yml"), StandardCopyOption.REPLACE_EXISTING);
				}
			}

			if(MODIFIABLE_LANG != null && !MODIFIABLE_LANG.equalsIgnoreCase("")){
				// si il y a une langue modifiable ...
				if(!Files.exists(Paths.get(path + MODIFIABLE_LANG + ".yml"))){
					Files.copy(getClass().getResourceAsStream("/" + MODIFIABLE_LANG +".yml"), Paths.get(path + MODIFIABLE_LANG +".yml"), StandardCopyOption.REPLACE_EXISTING);
				}
			}
			
			// maintenant que les fichiers de base sont normalement copiés, on a plus qu'à vérifier que le fichier 
			// demandé existe (il peut avoir demandé une langue que l'on a pas)
			
			if(!Files.exists(Paths.get(path + lang + ".yml"))){
				throw new FileLangNotFoundException();
			}
						
			load(path + lang + ".yml");
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public boolean load(String filePath) throws ErrorReadingFileException{
		return load(Paths.get(filePath));
	}

	public boolean load(Path filePath) throws ErrorReadingFileException{
		// chargeur de phrases pour les langues. On initie la connexion au fichier
		try(BufferedReader br = Files.newBufferedReader(filePath);){

			dictionnaire = new Hashtable<Integer, String>();
			String line;
			// on lit tout le fichier ligne par ligne
			while(true){
				line = br.readLine();
				// si il reste une ligne :
				if(line != null){
					// on ignore les "lignes-commentaires"
					if(!line.startsWith("#") && !line.startsWith(" ")){
						// on split entre le numéro de phrase (ex : 331) et la phrase concernée  ex : (331: Plugin activé)
						String[] arguments = line.split(":");

						if(arguments.length > 2){
							// si ya des ":" à l'intérieur de la phrase concerné, alors on concate
							for(int i=2 ; i < arguments.length ; i++){
								arguments[1] = arguments[1].concat(":").concat(arguments[i]);
							}
							arguments = new String[] {arguments[0], arguments[1]};
						}

						// ici, on a forcément un tableau avec {numéro, phrase}, on add dans notre dico

						dictionnaire.put(Integer.parseInt(arguments[0]), arguments[1].trim());
					}
				}
				else {
					break;
				}
			}

		}catch(Exception e){
			throw new ErrorReadingFileException();
		}
		return true;
		

	}

	public String getMessage(int number){
		if(dictionnaire.containsKey(number))
			return this.prefix.concat(dictionnaire.get(number));
		else
			return this.prefix + "Message not set for message_number : " + number + "\n Please delete your folder Konsole/lang to re-extract langage pack !";
	}


}

