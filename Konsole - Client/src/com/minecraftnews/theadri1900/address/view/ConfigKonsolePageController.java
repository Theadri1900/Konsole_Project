package com.minecraftnews.theadri1900.address.view;

import com.minecraftnews.theadri1900.address.KonsoleType;
import com.minecraftnews.theadri1900.address.Konsole_Client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ConfigKonsolePageController {
	
	private Konsole_Client mainApp;
	
	@FXML
	private RadioButton choiceSinceBegun;
	@FXML
	private RadioButton choiceLastMessage;
	@FXML
	private RadioButton choiceNow;
	
	private RadioButton[] allRadio;

	
	@FXML
	private TextField numberLastMessages;
	
	
	@FXML
	private Button getKonsole;
	
	@FXML
	private Label firstLabel;
	@FXML
	private Label secondLabel;
	@FXML
	private Label firdLabel;
	
	private Label[] treeLabels;
	
	
	
	public void setMainApp(Konsole_Client mainApp){
		this.mainApp  = mainApp;
	}
	
	
	
	@FXML
	private void initialize(){
		allRadio = new RadioButton[]{choiceSinceBegun, choiceLastMessage, choiceNow };
		treeLabels = new Label[] {firstLabel, secondLabel, firdLabel};
	}
	
	@FXML
	private void onGetKonsoleAir(){
		this.getKonsole.setStyle("-fx-background-color: #0087ff; -fx-border-color: #00ecf0;");
	}

	@FXML
	private void onGetKonsolePressed(){
		this.getKonsole.setStyle("-fx-background-color: #03a4ec; -fx-border-color: #00ecf0;");
	}

	@FXML
	private void onGetKonsoleExit(){
		this.getKonsole.setStyle("-fx-background-color: #0087ff; -fx-border-color: #0550f9;");
	}

	@FXML
	private void onGetKonsoleRelease(){
		onGetKonsoleExit();
	}
	
	@FXML
	private void onClickOnFirstLabel(){
		this.choiceSinceBegun.setSelected(true);
		onRadioChanged();
	}
	
	@FXML
	private void onClickOnSecondLabel(){
		this.choiceLastMessage.setSelected(true);
		onRadioChanged();
	}
	
	@FXML
	private void onClickOnFirdLabel(){
		this.choiceNow.setSelected(true);
		onRadioChanged();
	}
	
	@FXML
	private void onRadioChanged(){
		for(int i=0 ; i < this.allRadio.length ; i++){
			if(allRadio[i].isSelected()){
				treeLabels[i].setStyle("-fx-background-color: #0087ff; -fx-border-color:white;");
			}
			else {
				treeLabels[i].setStyle("-fx-background-color: #0087ff;");
			}
		}
	}
	
	@FXML
	private void onGetKonsoleClicked(){
		if(choiceSinceBegun.isSelected()){
			mainApp.getKonsoleFromKonfig(KonsoleType.SinceBegun, null);
		}
		else if(choiceLastMessage.isSelected()){
			try{
				int numberLastMessagesParsed = Integer.parseInt(numberLastMessages.getText());
				if(numberLastMessagesParsed < 1){
					throw new NumberFormatException();
				}
				
				mainApp.getKonsoleFromKonfig(KonsoleType.NumberLastMessages, new String[]{String.valueOf(numberLastMessagesParsed)});
			}catch(NumberFormatException e){
				numberLastMessages.setStyle("-fx-text-inner-color: #00aeff; -fx-border-color: red;");
			}
		}
		else if(choiceNow.isSelected()){
			mainApp.getKonsoleFromKonfig(KonsoleType.Now, null);
		}
	}
	
	@FXML
	private void onNumberMessageKeyPressed(KeyEvent e){
		if(e.getCode() == KeyCode.ENTER){
			onGetKonsoleClicked();
		}
	}
	
	
}
