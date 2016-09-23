package com.minecraftnews.theadri1900.address.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import com.minecraftnews.theadri1900.address.Konsole_Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AuthPageController {

	private Konsole_Client mainApp;
	private final static String styleError = "-fx-background-color: #e8333b";
	private final static String styleNormal = "-fx-background-color: #0087ff";

	//variable de controle des elements de la fenetre
	// l'annotation @FXML permet de donner l'acces aux variable pour le fichier FXML.

	@FXML
	private TextField user;
	@FXML
	private TextField password;
	@FXML
	private TextField portServer;
	@FXML
	private TextField portClient;
	@FXML
	private TextField ipServer;

	private TextField[] allTextField;


	@FXML
	private Label infoRight;
	@FXML
	private Label infoLoading;
	@FXML
	private Label infoPortServer;
	@FXML
	private Label infoPortClient;
	@FXML
	private Label infoUser;
	@FXML
	private Label infoPassword;
	@FXML
	private Label infoIpServer;

	private Label[] allLabel;

	@FXML
	private CheckBox advencedParam;


	@FXML
	private ProgressIndicator indicator;

	@FXML
	private Button validate;
	
	private Socket serverConnection;
	
	@FXML
	private ImageView topImage;

	public AuthPageController() {

	}

	public void setMainApp(Konsole_Client mainApp){
		this.mainApp  = mainApp;
	}

	@FXML
	private void initialize(){
		// on get tout ces labels (si on a beosin de faire des opérations pour tous les labels)
		allLabel = new Label[]{infoLoading, infoPassword, infoPortClient, infoPortServer, infoRight, infoUser,infoIpServer};
		allTextField = new TextField[]{password,portServer,portClient,user,ipServer};
		
		String finalPath = URI.create("Konsole.jpg").normalize().getPath(); 
		topImage.setImage(new Image(AuthPageController.class.getResourceAsStream(finalPath)));
	}
	// quelques animations sur le bouton
	@FXML
	private void onConnectAir(){
		this.validate.setStyle("-fx-background-color: #0087ff; -fx-border-color: #00ecf0;");
	}

	@FXML
	private void onConnectPressed(){
		this.validate.setStyle("-fx-background-color: #03a4ec; -fx-border-color: #00ecf0;");
	}

	@FXML
	private void onConnectExit(){
		this.validate.setStyle("-fx-background-color: #0087ff; -fx-border-color: #0550f9;");
	}

	@FXML
	private void onConnectRelease(){
		onConnectExit();
	}



	@FXML
	private void onConnectClick(){
		int portToConnect, portHere;
		InetAddress adress;
		// on controle chaque form avant de lancer une éventuelle co

		// reset des erreurs...
		for(Label label : allLabel){
			label.setStyle(styleNormal);
		}		

		// test de l'adresse ip valide
		try {
			adress = InetAddress.getByName(ipServer.getText());
		} catch (UnknownHostException e1) {
			infoIpServer.setStyle(styleError);
			infoRight.setStyle(styleError);
			infoRight.setText("Error : \n can't resolve IP");
			return;
		}

		// test si il y a des identifiants 
		if(user.getText().equalsIgnoreCase("") || password.getText().equalsIgnoreCase("")){
			infoRight.setStyle(styleError);
			infoRight.setText("You must provide creditentials.");
			infoUser.setStyle(styleError);
			infoPassword.setStyle(styleError);
			return;
		}

		// test du port correct server
		try{
			portToConnect = Integer.parseInt(portServer.getText());
			if(portToConnect < 1){
				throw new NumberFormatException("numberError");
			}
		}catch(NumberFormatException e){
			infoRight.setStyle(styleError);
			if(e.getMessage().equals("numberError")){
				infoRight.setText("Error : \n Port must be between \n 1 to 65535");	
			}else{
				infoRight.setText("Error : \n Port Server not a valid number.");
			}
			infoPortServer.setStyle(styleError);
			return;
		}
		// test du port correct ici
		try{
			portHere = Integer.parseInt(portClient.getText());
			if(portHere < 1){
				throw new NumberFormatException("numberError");
			}
		}catch(NumberFormatException e){
			// si c'est pas possible de convertir, c'est peut être la valeur AUTO, si non, ben on lance l'erreur;
			if(!portClient.getText().equalsIgnoreCase("auto")){
				infoRight.setStyle(styleError);
				if(e.getMessage().equals("numberError")){
					infoRight.setText("Error : \n Port must be between \n 1 to 65535");	
				}else{
					infoRight.setText("Error : \n Port Server not a valid number.");
				}
				infoPortClient.setStyle(styleError);
				portHere = -1;
				return;
			}
			else {
				portHere = 0;
			}
		}
		// ici, on change des paramètres pour que la tentative d'authentification se passe correctement;

		infoRight.setText("Wait...");
		infoLoading.setText("Connecting ...");
		// on disable les textFields
		for(TextField textfield : allTextField){
			textfield.setDisable(true);
		}
		validate.setDisable(true);
		advencedParam.setDisable(true);
		indicator.setVisible(true);
		infoLoading.setVisible(true);

		//lancement de la task de connexion.
		//ici je suis obligé de faire ça, pk ? je sais pas...
		int portHere2 = portHere;

		Thread connectProcess = new Thread(new Runnable(){
			PrintWriter send;
			BufferedReader read;
			@Override
			public void run() {
				try{
					updateLoader("Connection ...");
					serverConnection = new Socket(adress.getHostAddress(), portToConnect, null, portHere2);
					send = new PrintWriter(serverConnection.getOutputStream());
					read = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
					// si ce n'est pas le message d'accueil mais un autre truc ...
					if(!read.readLine().startsWith("1-")){
						onError("Konsole plugin not found.");
						return;
					}
					// on lance la méthode d'auth

					//on récupère la version, on check si elle est compatible avec la notre.
					String response = read.readLine();
					if(!response.startsWith("8-")){
						// si ce n'est pas le protocol info : 
						onError("Protocol error. \n Try to update the client & server !");
						return;
					}
					updateLoader("Checking protocol version ...");
					// on met dans un try/catch pour si ya un prob dans le spliting...
					String version = null;
					try{
						String[] versionServerRaw = response.split(":");
						String[] versionSpliter = versionServerRaw[1].split("=");
						version = versionSpliter[1];
					}catch(NullPointerException | ArrayIndexOutOfBoundsException e){
						onError("Protocol error. \n Try to update the client & server !");
						return;
					}

					if(!version.equals(Konsole_Client.PROTOCOL_VERSION)){
						onError("Not same protocol version ! \n Client :" + Konsole_Client.PROTOCOL_VERSION + "\n Server : " + version);
						return;
					}

					// a ce stade, on communique avec le même language. On a plus qu"à s'identifier !

					send.println("USER " + user.getText());
					send.flush();
					response = read.readLine();
					if(!response.startsWith("2-")){
						if (response.startsWith("8-")){
							// jailed :'(
							onError("You are jailed ! \n Need to wait unjailed.");
						}
						else {
							onError("Error : Bad reception. Try to reconnect.");
						}
						return;
					}

					updateLoader("Sending creditentials ...");
					send.println("PASSWD " + password.getText());
					send.flush();
					response = read.readLine();

					if(!response.startsWith("2-")){
						//si on a pas "bonne réception" alors erreur
						onError("Error : Bad reception. Try to reconnect.");
						return;
					}

					send.println("VALIDATE");
					send.flush();

					response = read.readLine();
					if(!response.startsWith("5-")){
						if(response.startsWith("4-")){
							// invalid creditentials
							String[] tryInfo = response.split(";");
							String[] tryByTryWait = tryInfo[1].split("=");
							String[] tryByTry = tryByTryWait[1].split("/");

							if(Integer.parseInt(tryByTry[0]) < Integer.parseInt(tryByTry[1])){
								onError("Error : Wrong user/passwd. \n Try " + tryByTry[0] + " On " + tryByTry[1]);
							}
							else {
								//maximum reached.
								response = read.readLine();
								if(response.startsWith("8-")){
									JOptionPane.showMessageDialog(null, response.substring(2), "Fatal Auth", JOptionPane.ERROR_MESSAGE);
									onError("Error : \n Max try reached !");
								}
								else {
									onError("Fatal Auth Error. \n max try reached ?");
								}
							}
						}
						else {
							onError("Error : Unknown error.");
						}
						return;
					}

					updateLoader("Getting rights ...");

					response = read.readLine();

					//si pas d'info, erreur de protocole normalement...
					if(!response.startsWith("8-")){
						onError("Error : \n Protocol error");
						return;
					}

					boolean canSend;
					
					try{
						String[] firstSpliter = response.split(":");
						String[] secondSpliter = firstSpliter[1].split("=");
						canSend = Boolean.parseBoolean(secondSpliter[1]);
					}catch(NullPointerException |ArrayIndexOutOfBoundsException e){
						onError("Error: \n Protocol error");
						return;
					}

					updateLoader("Connected ! ");

					setProgressFinished();
					Runnable launchKonsolePage = new Runnable() {
						@Override
						public void run() {
							mainApp.configureKonsolePage(serverConnection, canSend);
							}
					};

					if (Platform.isFxApplicationThread()) {
						// Nous sommes déjà dans le thread graphique
						launchKonsolePage.run();
					} else {
						// Nous ne sommes pas dans le thread graphique
						// on utilise runLater.
						Platform.runLater(launchKonsolePage);
					}


				}catch(IOException e){
					onError("Error while etablishing connection with server.");
				} finally{

				}

				return;
			}
			private void onError(String errorMessage){
				try{
					read.close();
					send.close();
					serverConnection.close();
				}catch(IOException  e){
					JOptionPane.showInternalMessageDialog(null, e.toString(), "Fatal Auth", JOptionPane.ERROR_MESSAGE);
				} catch(NullPointerException e){
					// si ya un nullpointer, alors c'est une erreur de co donc pas besoin de faire qqch en particulier
				}
				Runnable command = new Runnable() {
					@Override
					public void run() {
						// Le label étant le label JavaFX dont tu
						// souhaites modifier le texte.
						infoRight.setText(errorMessage);
						infoRight.setStyle(styleError);
						indicator.setVisible(false);
						infoLoading.setVisible(false);
						validate.setDisable(false);
						for(TextField textField : allTextField){
							textField.setDisable(false);
						}
						if(!advencedParam.isSelected()){
							// si la textbox n'est pas sélectionné, on n'active pas les param avancé.
							portServer.setDisable(true);
							portClient.setDisable(true);
						}
					}
				};
				if (Platform.isFxApplicationThread()) {
					// Nous sommes déjà dans le thread graphique
					command.run();
				} else {
					// Nous ne sommes pas dans le thread graphique
					// on utilise runLater.
					Platform.runLater(command);
				}
			}

			
			private void updateLoader(String message){
				Runnable command = new Runnable() {
					@Override
					public void run() {
						// Le label étant le label JavaFX dont tu
						// souhaites modifier le texte.
						infoLoading.setText(message);
					}
				};
				if (Platform.isFxApplicationThread()) {
					// Nous sommes déjà dans le thread graphique
					command.run();
				} else {
					// Nous ne sommes pas dans le thread graphique
					// on utilise runLater.
					Platform.runLater(command);
				}
			}


			private void setProgressFinished(){
				Runnable updater = new Runnable(){
					@Override
					public void run(){
						indicator.setLayoutY(indicator.getLayoutY() + 15);
						indicator.setLayoutX(indicator.getLayoutX() + 5);
						indicator.setProgress(100.00d);
					}
				};
				if(Platform.isFxApplicationThread()){
					updater.run();
				}
				else {
					Platform.runLater(updater);
				}
			}

		});
		
		connectProcess.start();
	}

	@FXML
	private void onAdvencedParamClick(){
		if(advencedParam.isSelected()){
			portServer.setDisable(false);
			portClient.setDisable(false);
			infoPortClient.setDisable(false);
			infoPortServer.setDisable(false);

		}
		else {
			portServer.setDisable(true);
			portClient.setDisable(true);
			infoPortClient.setDisable(true);
			infoPortServer.setDisable(true);
			portServer.setText("2199");
			portClient.setText("auto");
		}
	}

	@FXML
	private void onPasswdPressed(KeyEvent e){
		if(e.getCode() == KeyCode.ENTER){
			onConnectClick();
		}
	}



}
