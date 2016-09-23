package com.minecraftnews.theadri1900;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class KonsoleNetworkProcess extends BukkitRunnable{
	// si il es identifié
	private boolean isAuth;
	private String user;
	// pour écrire et lire le flux des données
	private PrintStream writeString = null;
	private BufferedReader readString = null;
	private Socket client = null;
	private KonsoleServer plugin;
	private boolean canSend = false;
	private String adress;
	// permet d'éviter l'attente de la fin de session, si true, la session ne SE TERMINE PAS
	private boolean abortEndSession = false;
	private boolean isReconnected = false;
	private boolean isConnectable = false;


	public KonsoleNetworkProcess(KonsoleServer plugin,Socket client) {
		isAuth = false;
		this.client = client;
		this.plugin = plugin;
		this.adress = client.getInetAddress().getHostAddress();
	}

	public void getBackSession(Socket newClient){
		isAuth = false;
		this.client = newClient;
		this.adress = client.getInetAddress().getHostAddress();
		isReconnected = true;
	}
	
	public boolean isReconnected() {
		return isReconnected;
	}

	public boolean isConnectable() {
		return isConnectable;
	}

	@Override
	public void run() {

		while(!client.isClosed()){

			try {
				writeString = new PrintStream(client.getOutputStream(), false);
				readString = new BufferedReader(new InputStreamReader(client.getInputStream()));

				if(!isAuth){
					authentificate();
				}

				String command = readString.readLine();

				if(command == null){
					throw new IOException("connection reset by peer.");
				}



				if(command.equalsIgnoreCase("DISCONNECT")){
					disconnect();
					writeString.println("5- Disconnected !");
					writeString.flush();
				}
				else if(command.startsWith("GET ")){
					getManager(command);
				}
				else if(command.startsWith("COMMAND ")){
					if(!plugin.grabber.isClient(this.user)){
						// si il a pas récupéré la konsole, alors pas possibilité d'envoyer de messages.
						writeString.println("9- You haven't get the Konsole !");
					}
					else if(!canSend){
						// si l'utilisateur peut pas envoyer de message, alors on lui dit qu'il ne peut pas ...
						writeString.println("9- You don't have the right to send commands.");
						writeString.flush();
					}
					else{
						plugin.log.info(plugin.lang.getMessage(KonsoleMessage.NEW_CLIENT_COMMAND).replace("[COMMAND]", command.substring(8)).replace("[USER]",this.user));
						plugin.internLog(plugin.lang.getMessage(KonsoleMessage.NEW_CLIENT_COMMAND).replace("[COMMAND]", command.substring(8)).replace("[USER]",this.user));
						if(command.substring(8).equalsIgnoreCase("reload")){
							plugin.consoleCommandGrabber.stopAllSession();
						}
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.substring(8));
					}
				}
				else {
					writeString.println("10- Error: unknown command");
					writeString.flush();
				}


			}catch(IOException e){
				// quand la connexion est fermé
				stopSesson(KonsoleProcessEnd.DELAYED_END);
			}
		}
	}

	public String getIp(){
		return this.adress;
	}


	public void stopSesson(KonsoleProcessEnd typeEnd){
		try{			
			if(typeEnd == KonsoleProcessEnd.RELOAD_END){
				writeString.println("8- reload process"); 
				writeString.flush();
			}

			if(user != null){
				plugin.log.info(plugin.lang.getMessage(KonsoleMessage.LOST_KNOWN_CONNECTION).replace("[USER]", this.user));
				plugin.internLog(plugin.lang.getMessage(KonsoleMessage.LOST_KNOWN_CONNECTION).replace("[USER]", this.user));
				disconnect();
			}
			
			else if(user == null || typeEnd == KonsoleProcessEnd.DELAYED_END) {
				// si l'user n'est pas co, savoir que le client se déco/reco pour tenter une nouvelle authentification si la précédente
				// avait raté, il faut donc mettre en mémoire l'heure de déconnexion afin d'éviter de charger la console en message de déco/reco
				// alors qu'en fait il tente de se co. De plus, ça évite les flood de personnes mal intentionés.
				client.close();
				writeString.close();
				readString.close();
				
				long timeDelayed = 0;
				try {
					timeDelayed = Long.parseLong(plugin.configManadger.getConfig("nb_seconds_keep_session_time"));
					if(timeDelayed < -1 || timeDelayed == 0) throw new NumberFormatException();
				}catch(NumberFormatException e){
					plugin.log.warn(plugin.lang.getMessage(KonsoleMessage.ERROR_PARSE_DELAYCONNECT));
					timeDelayed = 45;
				}
				int i = 0;
				isConnectable = true;
				while(i < timeDelayed && !abortEndSession){
					try {
						Thread.sleep(1_000);
						if(isReconnected){
							isConnectable = false;
							isReconnected = false;
							return;
						}
						else {
							i++;
						}
					} catch (InterruptedException e) {
						stopSesson(KonsoleProcessEnd.INSTANT_END);
					}


				}
				if(!abortEndSession){
				plugin.log.info(plugin.lang.getMessage(KonsoleMessage.LOST_CONNECTION).replace("[IP]", this.adress));
				plugin.internLog(plugin.lang.getMessage(KonsoleMessage.LOST_CONNECTION).replace("[IP]", this.adress));
				plugin.listen.removeSession(this.getTaskId());
				this.cancel();
				return;
				}
				else {
					return;
				}

			}
			else if (user == null){
				plugin.log.info(plugin.lang.getMessage(KonsoleMessage.LOST_CONNECTION).replace("[IP]", this.adress));
				plugin.internLog(plugin.lang.getMessage(KonsoleMessage.LOST_CONNECTION).replace("[IP]", this.adress));
			}

			plugin.listen.removeSession(this.getTaskId());
			client.close();
			writeString.close();
			readString.close();
			this.cancel();
			return;
			
		}catch(IOException e){
			try{
				client.close();
				writeString.close();
				readString.close();
				this.cancel();
			}catch(IOException e1){
				e.printStackTrace();
			}
		}catch(NullPointerException e){
			try{
				client.close();
				writeString.close();
				readString.close();
				this.cancel();
			}catch(IOException e1){
				e.printStackTrace();
			}
		}

		// si la session ne s'est pas terminé correctement et que on étein le serv, alors memory leak ...
		plugin.log.error(plugin.lang.getMessage(KonsoleMessage.MEMORY_LEAK_RELOAD));
		this.cancel();
	}


	private void getManager(String command){
		if(plugin.grabber.isClient(this.user)){
			// si il est déjà connecté au grabber, on lui dit qu'il ne peut pas se co une deuxième fois ..
			writeString.println("9- You already get Konsole !");
			writeString.flush();
			return;
		}
		// on doit check si le client veut le fichier depuis le début ou si il souhaite démarrer directement ou une certaine qqtt de lignes.
		boolean isNumber = false;
		int numberLines = 0;
		try{
			numberLines = Integer.parseInt(command.replace("GET ", "")); // on test de savoir si il nous a envoyé des nombres
			isNumber = true;
		}catch(NumberFormatException e){
			isNumber = false;
		}
		if(isNumber){
			// si il veut récupérer les x dernières lignes, il faut alors calculer le nb de lignes et envoyer tout.
			// calcul nb lignes et déplacement (il ne faut pas modifier le nb de lignes)
			Path logPath = Paths.get("logs/latest.log");
			int numberLineFile = 0;
			BufferedReader readerFile = null;
			try {
				readerFile = new BufferedReader(new FileReader(logPath.toFile()));
				// on LOCK le stream de logs : plus de flush possible normalement ...
				plugin.grabber.lock(this.user);
				while(readerFile.readLine() != null)
					numberLineFile++;
				// maintenant que l'on sait le nb de lignes, on reprend les x dernières
				int numberLineWait = numberLineFile - numberLines;
				if(numberLineWait < 1){
					// si il y a moins de lignes que celles demandés ...
					numberLineWait = 0;
					numberLines = numberLineFile;
				}

				// parcour du fichier ...
				readerFile.close();
				readerFile = new BufferedReader(new FileReader(logPath.toFile()));
				int numberLineParcoured = 0;
				while(numberLineParcoured < numberLineWait){
					readerFile.readLine();
					numberLineParcoured++;
				}


				writeString.println("8- " + numberLines + " Message(s) will be sent.");

				String lineToSend = readerFile.readLine();
				// on lit jusqua la fin du fichier
				do {
					writeString.println("7- " + lineToSend);
					writeString.flush();
					lineToSend = readerFile.readLine();
				}while(lineToSend != null);

				// une fois les fichiers envoyés, on l'inscrit !
				plugin.grabber.addClient(this.user, writeString);
				plugin.grabber.unLock(this.user);
			} catch (FileNotFoundException e) {
				writeString.println("6- Latest log file not found on server.");
				writeString.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					readerFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if(command.equals("GET BEGUN")){
			// si le client souhaite récupérer l'intégralité des logs...
			Path logPath = Paths.get("logs/latest.log");
			BufferedReader reader = null;
			try{
				// on lock pour pouvoir éviter les pertes de logs.
				plugin.grabber.lock(this.user);
				reader = new BufferedReader(new FileReader(logPath.toFile()));
				int numberLineFile = 0;
				while(reader.readLine() != null)
					numberLineFile++;

				// ici on sait cb de lignes fait le fichier, on peut informer le client.
				writeString.println("8- " + numberLineFile + " Messages will be sent.");
				writeString.flush();

				reader.close();
				reader = new BufferedReader(new FileReader(logPath.toFile()));

				String lineToSend = reader.readLine();
				// on envoi tout le fichier au client.
				do {
					writeString.println("7- " + lineToSend);
					writeString.flush();
					lineToSend = reader.readLine();
				}while(lineToSend != null);

				// on l'inscrit et on l'unlock
				plugin.grabber.addClient(this.user, writeString);
				plugin.grabber.unLock(this.user);

			}catch(IOException e){
				e.printStackTrace();
			}
			finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}


		}
		else if(command.equals("GET NOW")){
			// si le client ne veut qu'à partir de maintenant ...
			plugin.grabber.addClient(this.user, writeString);
		}
		else{
			writeString.println("4- Error: unknown get ! (BEGUN/NOW/number)");
			writeString.flush();
		}
	}


	private void authentificate() throws SocketException, IOException{
		String userIn=null,passwdIn=null;
		writeString.println("1- Welkome Konsole Client - Insert your creditentials");
		writeString.println("8- Info server : protocol_version=" + plugin.PROTOCOL_VERSION);
		writeString.flush();
		String response;
		int numberTryMax = 5, numberSecondJail = 300;
		// petit test pour savoir si le nb de try max est bien défini. Si non, alors on le met avec le défaut qui est 5 tentatives.

		try{
			numberTryMax = Integer.parseInt(plugin.configManadger.getConfig("nb_max_try_connect"));
			if(numberTryMax < 1){
				throw new NumberFormatException();
			}
		}catch(NumberFormatException e){
			// si on y arrive pas, on prévient par un log mais on laisse l'user s'auth avec 5 tentatives
			plugin.log.warn(plugin.lang.getMessage(KonsoleMessage.ERROR_PARSE_NBMAXTRY));
			numberTryMax = 5;
		}

		// pareil pour le nombre de Seconde de jail

		try{
			numberSecondJail = Integer.parseInt(plugin.configManadger.getConfig("nb_seconds_ban"));
			if(numberSecondJail < 1){
				throw new NumberFormatException();
			}
		}catch(NumberFormatException e){
			plugin.log.warn(plugin.lang.getMessage(KonsoleMessage.ERROR_PARSE_BANTIME));
			numberSecondJail = 300;
		}

		while(!isAuth){

			response = readString.readLine();

			// si la réponse est null, alors on doit fermer la session !
			if(response == null){
				throw new IOException("Connection reset by peer");
			}

			if(plugin.listen.isJail(this.adress)){
				// si il est banni
				writeString.println("8- You are still in jail.");
				writeString.flush();
			}
			else {
				if(response.startsWith("USER ")){
					// l'user est ce qu'il y a après le mot clé "user" et un espace, donc les 5 premiers caractères.
					userIn = response.substring(5);
					writeString.println("2- User successfuly received");
				}
				else if(response.startsWith("PASSWD ")){
					// pareil pour le passwd
					passwdIn = response.substring(7);
					writeString.println("2- Passwd successfuly received");
				}
				else if(response.startsWith("VALIDATE")){
					if(passwdIn == null || userIn == null){
						writeString.println("3- Error : missing user or password");
					}
					else {
						try{
							String goodPasswd = plugin.configManadger.getConfig("users." + userIn + ".password");
							// on récupère et compare le passwd.
							if(goodPasswd.equalsIgnoreCase(passwdIn)){
								// après on regarde si il est déjà connecté. Si oui, alors on n'autorise pas la connexion avec cet user.
								if(!plugin.listen.isConnected(userIn)){
									writeString.println("5- Sucessfully authentified !");
									this.isAuth = true;
									this.user = userIn;
									this.canSend = Boolean.valueOf(plugin.configManadger.getConfig("users." + userIn +".canSendCommand"));
									plugin.listen.removeJail(this.adress);
									plugin.listen.addConnectedUser(userIn);
									plugin.log.info(plugin.lang.getMessage(KonsoleMessage.NEW_KNOWN_CONNECTION).replace("[USER]",userIn));
									plugin.internLog(plugin.lang.getMessage(KonsoleMessage.NEW_KNOWN_CONNECTION).replace("[USER]",userIn));
									writeString.println("8- Your rights : sendCommands=" + canSend);
								}
								else{
									// si déjà connecté quelque part ...
									writeString.println("6- Error : User already connected !");
								}
							}
							else {
								plugin.listen.incrementNumberTry(this.adress);
								writeString.println("4- Error : Invalid creditentials ; try=" + plugin.listen.getNumberTry(this.adress) + "/" + numberTryMax);
							}
						}catch(NullPointerException e){
							plugin.listen.incrementNumberTry(this.adress);
							writeString.println("4- Error : Invalid creditentials ; try=" + plugin.listen.getNumberTry(this.adress) + "/" + numberTryMax);
						}
					}
					// si il y a eu un prob lors de l'auth (on est a la fin des vérifs et il est pas co)
					if(!isAuth){
						// si le nb de tentatives est égale ou supérieurs au nb de tentatives max défini par l'admin ...
						if(plugin.listen.getNumberTry(this.adress) >= numberTryMax){
							// on le ban pour le temps défini soit maintenant + nb en seconde *1000 pour set en milis.
							long dateEndJail = System.currentTimeMillis() + (numberSecondJail * 1000);
							plugin.listen.addJail(this.adress, dateEndJail);
							plugin.log.warn(plugin.lang.getMessage(KonsoleMessage.TRY_MAX_REACHED).replace("[IP]",userIn).replace("[TIME]", String.valueOf(numberSecondJail)));
							writeString.println("8- You reached the maximum try tolerance. You've been jailed for " + numberSecondJail + " seconds.");
						}
					}
				}
				else {
					writeString.println("10- Error : unknown command");
				}
				writeString.flush();
			}
		}
	}

	private void disconnect(){
		plugin.listen.addUnconnectedUser(user);
		isAuth = false;
		plugin.log.info(plugin.lang.getMessage(KonsoleMessage.DISCONNECT_USER).replace("[USER]",user));
		plugin.internLog(plugin.lang.getMessage(KonsoleMessage.DISCONNECT_USER).replace("[USER]",user));
	}

	public void setEndAbortSession(boolean abort){
		this.abortEndSession = abort;
	}

}
