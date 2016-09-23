package com.minecraftnews.theadri1900;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManadger {

	// Cette classe permet de construire la configuration fournit au plugin.
	// Elle est prise sur le HelpManadger qui est sensiblement pareil.

	private FileConfiguration config;
	private Path configFile;
	private KonsoleServer plugin;


	ConfigManadger(KonsoleServer plugin, Path file){
		this.configFile = file;
		this.plugin = plugin;
		load();
	}

	private void load(){
		config = null;
		
		// si le fichier existe pas, on le crée à partir de nos ressources.
		if(!Files.exists(configFile)){ 
			//on essaie de créer le dossier du plugin au cas où
			try { 
				Files.createDirectory(configFile.getParent());
			}
			catch(IOException e1){	/* Si dossier déjà présent ... rien faire de spécial*/ }
			
			// copie du fichier
			try {				
				Files.copy(getClass().getResourceAsStream("/config.yml"), configFile, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException e) {
				// si erreur on la log.
				plugin.log.error(e.toString()); e.printStackTrace();
			}
		
			// on charge le fichier de configuration + génération de passwords par défaut. On sauv qu'après avoir généré tout les mdp.
			config = YamlConfiguration.loadConfiguration(configFile.toFile());
			setConfig("users.admin.password", Utils.generatePassword(12,true,true), false); // on sauv pas on doit rajouter qqch juste après.
			setConfig("users.operator.password", Utils.generatePassword(12,true,true));
		}
		// si fichier existe, il suffit alors de le charger.
		else{
			config = YamlConfiguration.loadConfiguration(configFile.toFile()); // le .toFile pour être compatible avec le bukkit qui est encore sous Java 6
		}

	}

	/** Fonction de sauvegarde de la configuration dynamique */
	public void save(){
		try {
			config.save(configFile.toFile()); // on traduit le save de l'object en un save du fichier de config.
		} catch (IOException e) {plugin.log.error(e.toString()); e.printStackTrace();}
	}

	/** Récupération d'une valeur de configuration 
	 * @param key
	 * 			La clé pour récupérer la valeur associé
	 * */
	public String getConfig(String key){
		return this.config.getString(key);  //pareil, traduction simple car objet en private (getter and setter)

	}

	/**
	 *  Modification ou ajout d'une valeur sur une clé avec sauvegarde automatique sur le disque dur
	 * @param key
	 * 			La clé a configurer
	 * @param config
	 * 			La valeur
	 **/
	public void setConfig(String key, Object config){
		this.config.set(key, config);
		save();
	}
	/**
	 *  Modification ou ajout d'une valeur sur une clé mais surgargé pour prendre la main sur la sauv ou pas
	 * @param key
	 * 			La clé a configurer
	 * @param config
	 * 			La valeur
	 * @param save
	 * 			Si on doit sauvegarder dans un fichier à la fin ou pas
	 */
	public void setConfig(String key, Object config, boolean save){
		this.config.set(key,config);
		if(save)
			save();
	}

}
