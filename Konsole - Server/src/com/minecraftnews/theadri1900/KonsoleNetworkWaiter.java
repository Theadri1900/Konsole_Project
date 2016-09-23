package com.minecraftnews.theadri1900;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.LinkedList;

import org.bukkit.scheduler.BukkitRunnable;

public class KonsoleNetworkWaiter extends BukkitRunnable{
	private int port;
	private ServerSocket server = null;
	private boolean isRunning = true, stopProceded;
	private KonsoleServer plugin;
	private LinkedList<String> userConnected;	
	private Hashtable<String, Long> jail;
	private Hashtable<String, Long[]> nbTryList;
	private LinkedList<KonsoleNetworkProcess> sessionsNetworkProcess;

	public KonsoleNetworkWaiter(KonsoleServer plugin, int port) {
		try {
			this.port = port;
			this.plugin = plugin;
			// si le numéro de port n'existe pas (0 existe, mais c'est l'automatique et on a besoin de le savoir pour la co du client)
			if(port < 1 || port > 65535){
				throw new IOException(plugin.lang.getMessage(KonsoleMessage.ERROR_PARSE_PORT));
			}
			server = new ServerSocket(port);
			plugin.log.info(plugin.lang.getMessage(KonsoleMessage.LISTENNING));
			plugin.internLog(plugin.lang.getMessage(KonsoleMessage.LISTENNING));
			userConnected = new LinkedList<String>();
			jail = new Hashtable<String, Long>();
			nbTryList = new Hashtable<String, Long[]>();
			sessionsNetworkProcess = new LinkedList<KonsoleNetworkProcess>();
			stopProceded = false;
		}catch(IOException e){
			plugin.log.error(plugin.lang.getMessage(KonsoleMessage.FAILED_LOAD_SERVER).replace("[PORTNUMBER]", String.valueOf(port)));
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(isRunning){
			// on attent une connexion, ensuite on l'envoi à un gestionnaire de connexion.
			try{
				Socket client = server.accept();
				// il faut vérifier qu'il n'y a pas déjà qq connecté avec cet IP.
				boolean alReadyExist = false;
				boolean isConnectable = false;
				for(KonsoleNetworkProcess liste : sessionsNetworkProcess){
					if(liste.getIp().equals(client.getInetAddress().getHostAddress())){
						alReadyExist = true;
						isConnectable = liste.isConnectable();
						break;
					}
				}
				if(!alReadyExist){
					plugin.log.info(plugin.lang.getMessage(KonsoleMessage.NEW_UNKNOWN_CONNECTION).replace("[IP]", client.getInetAddress().getHostAddress()));
					plugin.internLog(plugin.lang.getMessage(KonsoleMessage.NEW_UNKNOWN_CONNECTION).replace("[IP]", client.getInetAddress().getHostAddress()));
					KonsoleNetworkProcess process = new KonsoleNetworkProcess(plugin,client);
					process.runTaskAsynchronously(plugin);
					sessionsNetworkProcess.add(process);
				}
				else if(alReadyExist && isConnectable){
					for(KonsoleNetworkProcess liste : sessionsNetworkProcess){
						if(liste.getIp().equals(client.getInetAddress().getHostAddress())){
							liste.getBackSession(client);
							break;
						}
					}
				}
				else {
					client.close();
				}
			}catch(SocketException e){
				// quand la connexion est fermé, et donc plus aucun retour pour le server.accept();
			} catch(IOException e){
				e.printStackTrace();	
			}
		}

	}

	public void stopServer(boolean isReload){
		// si on a déjà activé le processus de stoppage.
		if(stopProceded){
			return;
		}
		isRunning = false;
		// on ferme le serveur
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// on envoi le signal de fermeture aux sessions
		if(isReload){
			for(KonsoleNetworkProcess run : sessionsNetworkProcess){
				run.setEndAbortSession(true);
			}
			try {
				Thread.sleep(1_000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(KonsoleNetworkProcess run : sessionsNetworkProcess){
			run.stopSesson(KonsoleProcessEnd.RELOAD_END);
			}
		}
		else {
			for(KonsoleNetworkProcess run : sessionsNetworkProcess){
				run.setEndAbortSession(true);
			}
			try {
				Thread.sleep(1_000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(KonsoleNetworkProcess run : sessionsNetworkProcess){
			run.stopSesson(KonsoleProcessEnd.INSTANT_END);
			}
		}
		stopProceded = true;
		if(isReload){
			plugin.log.warn(plugin.lang.getMessage(KonsoleMessage.RELOAD_NOT_RECOMMENDED));
		}
		this.cancel(); 
	}

	public void addConnectedUser(String user){
		// pour rajouter un utilisateur connecté
		userConnected.add(user);
	}

	public boolean isConnected(String user){
		return userConnected.contains(user);
	}

	public void addUnconnectedUser(String user){
		if(userConnected.contains(user))
			userConnected.remove(user);
		plugin.grabber.deleteClient(user);
	}

	public void addJail(String ip, long timeEnd){
		jail.put(ip, timeEnd);
	}

	public boolean isJail(String ip){
		if(!jail.containsKey(ip)){
			return false; 
		}
		else {
			// si l'heure de fin de jail n'est pas arrivé
			if(jail.get(ip) > System.currentTimeMillis()){
				return true;
			}
			else {
				jail.remove(ip);
				return false;
			}
		}
	}

	public void removeJail(String ip) {
		if(!jail.containsKey(ip)){
			return;
		} else {
			jail.remove(ip);
		}

	}

	public int getNumberTry(String ip){
		if(nbTryList.containsKey(ip)){
			Long[] infos = nbTryList.get(ip);
			// dans les infos : LongDateApologize ; LongNbTry
			// si on a pas à pardonner, on envoit le nb d'essais
			if(infos[0] > System.currentTimeMillis()){
				return infos[1].intValue();
			}
			// si on pardonne, on supprime son "casier judiciaire" et retourne 0
			else {
				nbTryList.remove(ip);
				return 0;
			}
		}
		else {
			// si il n'a pas de casier, alors c'est la 0ème fois !! xD
			return 0;
		}
	}

	public void incrementNumberTry(String ip){
		if(nbTryList.containsKey(ip)){
			Long[] infos = nbTryList.get(ip);
			infos[1]++;
		}
		else {
			// on ajoute la date de fin qui est la date de now + le temps en secondes *1000 donné ds le fichier config
			long timeToApologize = 900000L;
			try{
				timeToApologize = Long.valueOf(plugin.configManadger.getConfig("nb_seconds_apologize")) * 1000;
				if(timeToApologize < 1000){
					throw new NumberFormatException();
				}
			}catch(NumberFormatException e){
				timeToApologize = 900000L;
				plugin.log.warn(plugin.lang.getMessage(KonsoleMessage.ERROR_PARSE_APOLOGIZE));
			}
			nbTryList.put(ip, new Long[] {System.currentTimeMillis() + timeToApologize, 1L});
		}
	}

	public void removeSession(int taskID){
		for(int i=0; i < sessionsNetworkProcess.size() ; i++){
			KonsoleNetworkProcess run = sessionsNetworkProcess.get(i);
			if(run.getTaskId() == taskID){
				sessionsNetworkProcess.remove(i);
				break;
			}
		}

	}	


}
