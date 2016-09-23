package com.minecraftnews.theadri1900.address;

import java.awt.SplashScreen;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;

import com.minecraftnews.theadri1900.address.view.AuthPageController;
import com.minecraftnews.theadri1900.address.view.ConfigKonsolePageController;
import com.minecraftnews.theadri1900.address.view.KonsolePageController;
import com.minecraftnews.theadri1900.address.view.RootLayoutController;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Konsole_Client extends Application {

	public final static String VERSION = "0.0.1";
	public final static String PROTOCOL_VERSION = "0.0.2";
	private Stage authStage, mainStage, konfigStage;
	private AuthPageController authPageController;
	private ConfigKonsolePageController konsoleConfigPageController;
	private KonsolePageController konsolePageController;
	private RootLayoutController rootLayoutController;
	private BorderPane rootLayout;
	private AnchorPane konsolePage, authPane, konfigConsole;


	@Override
	public void start(Stage primaryStage) {
		resetAll();
		this.authStage = primaryStage;
		this.authStage.setTitle("Konsole - Connection");
		initAuthPage();
	}

	public void initAuthPage(){
		// on désactive la taille de la fenetre
		this.authStage.setResizable(false);

		try {		
			// chargement du FXML
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Konsole_Client.class.getResource("view/AuthPage.fxml"));
			// récupération de la scene
			authPane = (AnchorPane) loader.load();
			Scene scene = new Scene(authPane);
			this.authStage.setScene(scene);
			this.authStage.getIcons().add(new Image(this.getClass().getResourceAsStream("ressources/K256.png")));
			this.authStage.setOnCloseRequest((windowsEvent) -> System.exit(0));
			this.authPageController = loader.getController();
			this.authPageController.setMainApp(this);
			SplashScreen splash = SplashScreen.getSplashScreen();
			if(splash != null){
				splash.close();
			}
			this.authStage.show();
		}catch(IOException e){
			e.printStackTrace();
		}

	}


	private void resetAll() {
		this.authStage = null;
		this.mainStage = null;
		this.konfigStage = null;
		this.authPageController = null;
		this.konsoleConfigPageController = null;
		this.konsolePageController = null;
		this.rootLayoutController = null;
		this.rootLayout = null;
		this.konsolePage = null;
		this.authPane = null;
		this.konfigConsole = null;
	}

	public void initRootLayout(){

		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Konsole_Client.class.getResource("view/RootLayout.fxml"));
			this.rootLayout = (BorderPane) loader.load();
			this.rootLayoutController = loader.getController();			
			this.rootLayoutController.setMainApp(this);
			Scene scene = new Scene(rootLayout);
			this.mainStage = new Stage();
			this.mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					try{
						konfigStage.close();
					}catch(Exception e){

					}
				}
			});
			this.mainStage.setScene(scene);
			this.mainStage.getIcons().add(new Image(this.getClass().getResourceAsStream("ressources/K256.png")));
			this.mainStage.setOnCloseRequest((windowsEvent) -> konsolePageController.finish(true));
			this.authStage.close();
			this.mainStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void initKonsoleMain(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Konsole_Client.class.getResource("view/KonsolePage.fxml"));
			konsolePage = (AnchorPane) loader.load();
			this.konsolePageController = loader.getController();
			this.konsolePageController.setMainApp(this);
			rootLayout.setCenter(konsolePage);


		}catch(IOException e){
			e.printStackTrace();
		}
	}


	public void configureKonsolePage(Socket connection, boolean canSend){
		initRootLayout();
		initKonsoleMain();
		this.konsolePageController.manualInit(connection, canSend);
		this.mainStage.setTitle("Konsole - Remote Console");
	}


	public void launchKonfiguratorKonsole(){
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Konsole_Client.class.getResource("view/ConfigKonsolePage.fxml"));
			konfigConsole = (AnchorPane) loader.load();
			this.konsoleConfigPageController = loader.getController();
			setDisableMenu(true);
			konfigConsole.setBackground(new Background(new BackgroundImage((new Image(Konsole_Client.class.getResourceAsStream("ressources/backgroundFileSelector.jpg"))), BackgroundRepeat.ROUND, BackgroundRepeat.ROUND, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
			this.konsoleConfigPageController.setMainApp(this);
			Scene konfigScene = new Scene(konfigConsole);
			konfigStage = new Stage();
			konfigStage.setResizable(false);
			konfigStage.setOnCloseRequest((windowsEvent) -> System.exit(0));
			konfigStage.setScene(konfigScene);
			konfigStage.getIcons().add(new Image(Konsole_Client.class.getResourceAsStream("ressources/K256.png")));
			konfigStage.setTitle("Konsole - Get a konsole");
			konfigStage.show();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void getKonsoleFromKonfig(KonsoleType konsoleType, String[] args){
		konfigStage.close();
		setDisableMenu(false);
		konsolePageController.getKonsole(konsoleType, args);
	}

	private void setDisableMenu(boolean disable){
		rootLayoutController.setMenuBarDisable(disable);
	}

	public void konsolePageReady(){
		this.konsolePageController.ready();
	}



	public Stage getMainStage() {
		return mainStage;
	}

	public KonsolePageController getKonsolePageController() {
		return konsolePageController;
	}

	public static void main(String[] args) {
		String version = System.getProperty("java.version");
		String[] parsedVersion = version.split("\\.");
		int[] fullyParsedVersion = {Integer.parseInt(parsedVersion[0]), Integer.parseInt(parsedVersion[1])};
		if(fullyParsedVersion[0] >= 1 && fullyParsedVersion[1] >= 8)
			launch(args);
		else
			JOptionPane.showMessageDialog(null, "You don't have at least JAVA 1.8. \n Please download JAVA 1.8 at http://www.java.com/", "Too old version", JOptionPane.ERROR_MESSAGE);
	}
}
