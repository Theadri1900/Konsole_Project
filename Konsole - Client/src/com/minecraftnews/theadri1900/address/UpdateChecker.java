package com.minecraftnews.theadri1900.address;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateChecker {
	
	private URL urlRss;
	private String version, link;
	private Konsole_Client mainApp;

	public UpdateChecker(Konsole_Client mainApp, String urlRss){
		try {
			this.mainApp = mainApp;
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
				if(children.item(1).getTextContent().contains("Client")){
					badFile = false;
				}
				i++;
			}
			this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
			this.link = children.item(3).getTextContent();
			if(this.version.equals(mainApp.VERSION)){
				return false;
			}
			else {
				return true;
			}
		}catch(NullPointerException e){
			// si c'est un nullpointer, c'est qu'il n'y a pas de fichier disponibles. Pas besoin d'informer l'utilisateur de ce problème.	
		}catch (Exception e) {
			// ici,  rien de spécial, si ya une erreur, tant pis ...
		}		

		return false;
	}

	/*private String determineOs() {
		String rawOs = System.getProperty("os.name").toLowerCase();
		if(rawOs.contains("windows")){
			return "Windows";
		}
		else if(rawOs.contains("mac")){
			return "Mac";
		}
		else if(rawOs.contains("nix") || rawOs.contains("nux")){
			return "Linux";
		}
		return "unknown";
	} */

	public String getVersion(){
		return this.version;
	}

	public String getLink(){
		return this.link;
	}

}
