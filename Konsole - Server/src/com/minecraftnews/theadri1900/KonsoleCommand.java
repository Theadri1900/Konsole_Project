package com.minecraftnews.theadri1900;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class KonsoleCommand implements Listener {
	
	private KonsoleServer plugin;
	
	public KonsoleCommand(KonsoleServer plugin){
		this.plugin = plugin;
	}
	// ces event permettent de savoir toutes les commandes faîtes et de voir si c'est un reload. 
	// et donc prévenir toutes les sessions connectés.
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent e){
		if(e.getMessage().equals("reload")){
			stopAllSession();
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void onServerCommand(ServerCommandEvent e){
		if(e.getCommand().equals("reload")){
			stopAllSession();
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void onRCONcommand(RemoteServerCommandEvent e){
		if(e.getCommand().equals("reload")){
			stopAllSession();
		}
	}
	
	public void stopAllSession(){
		plugin.log.info(plugin.lang.getMessage(KonsoleMessage.RELOAD_TASK_DETECTED));
		plugin.listen.stopServer(true);
	}
	
}
