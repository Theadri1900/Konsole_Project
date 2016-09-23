package com.minecraftnews.theadri1900.address.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import com.minecraftnews.theadri1900.address.KonsoleType;
import com.minecraftnews.theadri1900.address.Konsole_Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

public class KonsolePageController {
	
	private Konsole_Client mainApp;
	private Socket konsoleServer;
	private boolean canSendCommand;
	private PrintStream sender;
	private BufferedReader reader;
	
	@FXML
	private Button send;
	
	@FXML
	private TextField command;
	
	@FXML
	private TextArea monitor;
	
	@FXML
	private ProgressBar progress;
	
	@FXML
	private Label infos;

	@FXML
	private Slider sizeChoice;

	@FXML
	private CheckBox blackVersion;
	
	
	public void setMainApp(Konsole_Client mainApp){
		this.mainApp  = mainApp;
		mainApp.launchKonfiguratorKonsole();
	}
	
	
	
	@FXML
	private void initialize(){
		sizeChoice.valueProperty().addListener((observable, oldvalue, newvalue) -> onSizeChoiceChange());
	}
	
	public void manualInit(Socket socket, boolean canSendCommand){
		try{
		this.konsoleServer = socket;
		this.sender = new PrintStream(socket.getOutputStream());	
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		this.canSendCommand = canSendCommand;
		if(!canSendCommand){
		this.command.setDisable(true);
		this.command.setText("Sorry, you don't have the rights to send commands :'(");
		this.send.setDisable(true);
		}
		else {
			this.command.setDisable(false);
			this.command.setText("");
			this.send.setDisable(false);
		}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void getKonsole(KonsoleType konsoleType, String[] args){
		if(konsoleType == KonsoleType.SinceBegun){
			sender.println("GET BEGUN");
			sender.flush();
			prepare();
			// utilisation de lambdas
			new Thread(() -> getMessages()).start();
		}
		else if(konsoleType == KonsoleType.NumberLastMessages){
			int numberMessages = Integer.parseInt(args[0]);
			sender.println("GET " + numberMessages);
			sender.flush();
			prepare();
			new Thread(() -> getMessages()).start();
		}
		else if(konsoleType == KonsoleType.Now){
			sender.println("GET NOW");
			sender.flush();
			ready();
		}
	
	}
	
	private void getMessages(){
		try {			
			// ici, message pour savoir combiens de messages vont être envoyés
			String numberMessagesRaw = reader.readLine();
			
			if(!numberMessagesRaw.startsWith("8- ")){
				// si il n'y a pas le bon truc ...
				JOptionPane.showInputDialog(null, "Protocol error :'(", "Protocol error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			numberMessagesRaw = numberMessagesRaw.substring(3); // on enlève le nombre du message et l'espace qu'il y a 
			String[] grabNumberMessages = numberMessagesRaw.split(" ");
			int numberMessages = Integer.parseInt(grabNumberMessages[0]);
			int numberMessagesReceived = 0;
			String message = null;

			while(numberMessagesReceived < numberMessages){
				final String toSetText = "Downloading " + numberMessagesReceived + " of " + numberMessages + " messages...";
				Platform.runLater(() -> infos.setText(toSetText));
				message = reader.readLine();
				if(message.startsWith("7- ")){
				final String messageFinal = message.substring(3) + "\n";
				Platform.runLater(() -> monitor.appendText(messageFinal));
				numberMessagesReceived++;
				final double avancement = (double) numberMessagesReceived/ (double) numberMessages;
				Platform.runLater(() -> progress.setProgress(avancement) );
				}
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
		Platform.runLater(() -> ready());
		
	}
	
	private void prepare(){
		progress.setVisible(true);
		infos.setVisible(true);
		command.setVisible(false);
		send.setVisible(false);
	}
	
	public void ready(){
		progress.setVisible(false);
		infos.setVisible(false);
		command.setVisible(true);
		send.setVisible(true);
		// ceci est le metteur à jour du moniteur.
		new Thread(() -> {
			while(!konsoleServer.isClosed()){
				try{
					String newLine = reader.readLine();
					if(newLine.startsWith("7- ")){
						final String finalLine = newLine.substring(3).concat("\n");
						Platform.runLater(() -> monitor.appendText(finalLine) );
					}
					else if(newLine.startsWith("8-")){
						if(newLine.equals("8- reload process")){
							konsoleServer.close();
							Platform.runLater(() -> {
								monitor.appendText("---------------------------------------");
								monitor.appendText("--------- SERVER IS RELOADING ---------");
								monitor.appendText("---------------------------------------");
							});
							
							Platform.runLater(() -> onConnectionLost());
							
						}
					}
					else {
						System.err.println(newLine);
					}
				}catch(IOException | NullPointerException e){
					try{
						reader.close();
						sender.close();
						konsoleServer.close();
						Platform.runLater(() -> onConnectionLost());
					}catch(IOException e1){
						e.printStackTrace();
					}
					
				}
			}
		} ).start();
	}
	
	
	private void onConnectionLost(){
		command.setText("Server is disconnected.");
		command.setDisable(true);
		send.setDisable(true);
	}
	
	@FXML
	private void onSizeChoiceChange(){
		int newSize = (int)sizeChoice.getValue();
		monitor.setFont(new Font("Courier New", newSize));
	}
	
	@FXML
	private void onSendAir(){
		this.send.setStyle("-fx-background-color: #0087ff; -fx-border-color: #00ecf0;");
	}

	@FXML
	private void onSendPressed(){
		this.send.setStyle("-fx-background-color: #03a4ec; -fx-border-color: #00ecf0;");
	}

	@FXML
	private void onSendExit(){
		this.send.setStyle("-fx-background-color: #0087ff; -fx-border-color: #0550f9;");
	}

	@FXML
	private void onSendRelease(){
		onSendExit();
	}
	
	@FXML
	private void onBlackVersionClick(){
		if(blackVersion.isSelected()){
			// si il veut la version black & white
			monitor.setStyle("-fx-text-inner-color:white;");
			Region region = ( Region ) monitor.lookup( ".content" );
			region.setStyle("-fx-background-color: black;");
		}
		else {
			monitor.setStyle("-fx-text-inner-color:black;");
			Region region = ( Region ) monitor.lookup( ".content" );
			region.setStyle("-fx-background-color: white;");
		}
	}
	
	
	@FXML
	private void onSendClicked(){
		if(command.getText().length() > 1){
		new Thread(() -> {
			sender.println("COMMAND ".concat(command.getText()));
			sender.flush();
			Platform.runLater(() -> command.setText(""));
		}).start();
		}
	}
	
	@FXML
	private void onCommandKeyPressed(KeyEvent e){
		if(e.getCode() == KeyCode.ENTER){
			onSendClicked();
		}
	}
	
	public void finish(boolean closeProgramm){
		try{
		konsoleServer.close();
		if(closeProgramm) System.exit(0);
		
		}catch(IOException e){
			System.err.println(e.toString());
			if(closeProgramm)	System.exit(0);
		}
	}
	
}
