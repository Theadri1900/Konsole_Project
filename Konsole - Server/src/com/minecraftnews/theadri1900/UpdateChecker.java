package com.minecraftnews.theadri1900;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateChecker {

	private KonsoleServer plugin;
	private URL urlRss;
	private String version, link;

	public UpdateChecker(KonsoleServer plugin, String urlRss){
		this.plugin = plugin;
		try {
			this.urlRss = new URL(urlRss);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public boolean isUpdateAvaiable(){
		try {
			InputStream rssPageStream = urlRss.openConnection().getInputStream();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rssPageStream);
			boolean badFile = true;
			Node lastestFile = null;
			NodeList children = null;
			int i = 0;
			
			while(badFile){
				lastestFile = document.getElementsByTagName("item").item(i);
				children = lastestFile.getChildNodes();
				if(children.item(1).getTextContent().contains("Server")){
					badFile = false;
				}
				i++;
			}
			this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
			this.link = children.item(3).getTextContent();
			if(this.version.equals(plugin.getDescription().getVersion())){
				return false;
			}
			else {
				return true;
			}
		}catch(NullPointerException e){
			// si c'est un nullpointer, c'est qu'il n'y a pas de fichier disponibles. Pas besoin d'informer l'utilisateur de ce problème.	
		}catch (Exception e) {
			// ici, on met juste une petite info dans le log mais pas de gros traceur.
			plugin.log.warn(plugin.lang.getMessage(KonsoleMessage.ERROR_WHILE_CHECKUPDATE).replace("[CODE]", e.toString()));
		}		

		return false;
	}

	public String getVersion(){
		return this.version;
	}

	public String getLink(){
		return this.link;
	}

}
