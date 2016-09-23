package com.minecraftnews.theadri1900;

import org.bukkit.scheduler.BukkitRunnable;

public class KonsolePersonalLoggerHeart extends BukkitRunnable{
	
	private KonsolePersonalLogger personalLogger;
	
	public KonsolePersonalLoggerHeart(KonsolePersonalLogger personalLogger) {
		super();
		this.personalLogger = personalLogger;
	}
	
	@Override
	public void run() {
		personalLogger.flush();
	}

}
