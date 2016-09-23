package com.minecraftnews.theadri1900.address.view;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JOptionPane;


import com.minecraftnews.theadri1900.address.Konsole_Client;
import com.minecraftnews.theadri1900.address.UpdateChecker;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

public class RootLayoutController {

	private Konsole_Client mainApp;

	@FXML
	private MenuBar menuBar;



	public void initialize(){

	}

	public void setMenuBarDisable(boolean disable){
		this.menuBar.setDisable(disable);
	}

	public void setMainApp(Konsole_Client mainApp){
		this.mainApp  = mainApp;
	}

	@FXML
	private void handleDisconnect(){
		this.mainApp.getKonsolePageController().finish(false);
		this.mainApp.getMainStage().close();
		Platform.runLater(() -> this.mainApp.start(new Stage()));
	}

	@FXML
	private void handleExit(){
		this.mainApp.getKonsolePageController().finish(true);
	}
	@FXML
	private void handleBukkitDev(){
		onBrowser("http://dev.bukkit.org/bukkit-plugins/konsole/");
	}

	@FXML
	private void handleDonate(){
		onBrowser("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3Y2Z66KUFHGKS");
	}

	@FXML
	private void handleCheckUpdate(){
		setMenuBarDisable(true);
		UpdateChecker checker = new UpdateChecker(mainApp, "http://dev.bukkit.org/bukkit-plugins/konsole/files.rss");
		if(checker.isUpdateAvaiable()){
			JOptionPane.showMessageDialog(null, "An update is avaiable ! \n version : " + checker.getVersion() + "\n Link : " + checker.getLink(), "Update Avaiable !", JOptionPane.INFORMATION_MESSAGE);
		}
		else {
			JOptionPane.showMessageDialog(null, "No updates avaiable or error while checking update. \n BukkitDev link if you want to manually check : \n http://dev.bukkit.org/bukkit-plugins/konsole/", "No updates found", JOptionPane.INFORMATION_MESSAGE);
		}
		setMenuBarDisable(false);
	}
	
	private void onBrowser(String link){
		String os = System.getProperty("os.name").toLowerCase();
		if(Desktop.isDesktopSupported()){
			try {
				Desktop desk = Desktop.getDesktop();
				desk.browse(new URL(link).toURI());
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			if(os.contains("windows")){
				try {
					runtime.exec("start " + link);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(os.contains("mac")){
				try {
					runtime.exec("open " + link);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(os.contains("nix") || os.contains("nux")){
				try {
					runtime.exec("xdg-open " + link);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {

			}
		}
	}

}
