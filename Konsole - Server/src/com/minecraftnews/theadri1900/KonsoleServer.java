package com.minecraftnews.theadri1900;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class KonsoleServer extends JavaPlugin {

	public static final String PROTOCOL_VERSION = "0.0.2";
	public final  Logger log = (Logger) LogManager.getRootLogger();
	public final  String prefix = "[Konsole] ";
	public final  String pluginDirectory = getDataFolder().getPath() + "/";
	public Language lang;
	public ConfigManadger configManadger;
	public KonsoleNetworkWaiter listen;
	public KonsoleGrabber grabber;
	public KonsoleCommand consoleCommandGrabber;
	private UpdateChecker updateChecker;
	private KonsolePersonalLogger personalLogger;

	public void onEnable(){
		this.configManadger = new ConfigManadger(this, Paths.get((pluginDirectory + "config.yml")));
		// initialization des langues (mit dans des méthodes pour être plus propre)
		loadLanguage();
		// ici on lance ou pas le logger
		if(Boolean.parseBoolean(configManadger.getConfig("save_log_user"))){
			personalLogger = new KonsolePersonalLogger(this, Paths.get(pluginDirectory + "logs/"));
		}
		// on lance le listener
		loadListener();
		grabber = new KonsoleGrabber(this);
		Metrics metrics;
		try {
			metrics = new Metrics(this);
			metrics.start();
			if(metrics.isOptOut()){
				log.info(lang.getMessage(KonsoleMessage.INFO_STAT_OFF));
			}
		} catch (IOException e) {
			log.warn(lang.getMessage(KonsoleMessage.ERROR_CONTACTING_STATSERVER).replace("[CODE]", e.toString()));
		}
		if(Boolean.parseBoolean(configManadger.getConfig("notify_update_avaiable"))){
			updateChecker = new UpdateChecker(this, "http://dev.bukkit.org/bukkit-plugins/konsole/files.rss");
			if(updateChecker.isUpdateAvaiable()){
				log.info(lang.getMessage(KonsoleMessage.UPDATE_AVAIABLE).replace("[VERSION]", updateChecker.getVersion()).replace("[URLUPDATE]", updateChecker.getLink()));
				log.info(lang.getMessage(KonsoleMessage.UPDATE_INFO));
			}
		}
		else {
			log.warn(lang.getMessage(KonsoleMessage.UPDATE_WARNING));
		}
		
		consoleCommandGrabber = new KonsoleCommand(this);
		getServer().getPluginManager().registerEvents(consoleCommandGrabber, this);
		log.info(lang.getMessage(KonsoleMessage.SUCCES_ENABLED));
	}


	public void onDisable(){
		this.configManadger.save();
		this.listen.stopServer(false);
		if(personalLogger != null) personalLogger.closeLogger();
	}


	private void loadLanguage(){
		if(configManadger.getConfig("lang") != null){
			try {
				lang = new Language(pluginDirectory + "lang/", configManadger.getConfig("lang"), this.prefix);
			} catch (FileLangNotFoundException | ErrorReadingFileException | IOException e) {
				// on réessaye. Si ça ne marche toujours pas avec la langue par défault, alors on l'indique ...
				try {
					lang = new Language(pluginDirectory + "lang/", Language.DEFAULT_LANG, this.prefix);
					log.warn(prefix + lang.getMessage(KonsoleMessage.FAILED_LOAD_LANG));
				} catch (IOException | FileLangNotFoundException | ErrorReadingFileException e1) {
					e1.printStackTrace();
				}
			}
		}
		else {
			try {
				lang = new Language(pluginDirectory + "lang/", Language.DEFAULT_LANG, this.prefix);
				log.warn(prefix + lang.getMessage(KonsoleMessage.FAILED_LOAD_LANG));
			} catch (FileLangNotFoundException | ErrorReadingFileException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadListener() {
		int port = 0;
		try{
			port = Integer.parseInt(configManadger.getConfig("port"));
			if(port < 1){
				throw new NumberFormatException();
			}
		}catch(NumberFormatException e){
			log.error(lang.getMessage(KonsoleMessage.ERROR_PARSE_LISTENPORT));
			port = 2199;
		}
		listen = new KonsoleNetworkWaiter(this, port);
		listen.runTaskAsynchronously(this);
	}
	
	public void internLog(String message){
		if(personalLogger != null){
			personalLogger.localLog(message);
		}
	}

}
