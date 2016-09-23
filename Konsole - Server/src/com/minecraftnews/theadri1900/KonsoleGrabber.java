package com.minecraftnews.theadri1900;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;

public class KonsoleGrabber implements Filter {


	private Hashtable<String, PrintStream> toSend;
	private Calendar calendar = Calendar.getInstance();
	private LinkedList<String> locker;

	public KonsoleGrabber(KonsoleServer plugin) {
		super();
		plugin.log.addFilter(this);
		locker = new LinkedList<String>();
		toSend = new Hashtable<String, PrintStream>();
	}

	public void addClient(String user, PrintStream printer) {
		// évite les doublons ...
		if(!toSend.containsKey(user))
		toSend.put(user, printer);
	}

	public void deleteClient(String user){
		if(toSend.containsKey(user))
		toSend.remove(user);
	}
	
	public void lock(String user){
		if(!locker.contains(user))
			locker.add(user);
	}
	
	public void unLock(String user){
		if(locker.contains(user))
			locker.remove(user);
	}	
	
	public boolean isClient(String user){
		return toSend.containsKey(user);
	}
	
	

	@Override
	public Result getOnMismatch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result getOnMatch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, Level arg1,
			Marker arg2, Message arg3, Throwable arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, Level arg1,
			Marker arg2, Object arg3, Throwable arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result filter(org.apache.logging.log4j.core.Logger arg0, Level arg1,
			Marker arg2, String arg3, Object... arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result filter(LogEvent log) {
		// on vérifie que l'on fait au moins une opération pour qq (sinon on laisse tomber, pas de calculs dans le vide OPTI PUTAIN :')
		while(locker.size() > 0){
			try {
				this.wait(100L);
			} catch (InterruptedException e) {
			}
		}
		if(toSend.size() > 0){
			calendar.setTimeInMillis(log.getMillis());
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minut = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			String hourS = (hour > 10) ? String.valueOf(hour) : "0" + hour;
			String minutS = (minut > 10) ? String.valueOf(minut) : "0" + minut;
			String secondS = (second > 10) ? String.valueOf(second) : "0" + second;

			String prefix = "[" + hourS + ":" + minutS + ":" + secondS + " ";

			String level = log.getLevel().name();
			switch(level){
			case "INFO":
				prefix = prefix.concat("INFO]: ");
				break;
			case "WARNING":
				prefix = prefix.concat("WARN]: ");
				break;
			case "ERROR":
				prefix = prefix.concat("ERROR]: ");
				break;
			default:
				prefix = prefix.concat("TO DEFINE : " + log.getLevel().name() + "] ");
				break;
			}
			// on envois aux éventuels inscrits.
			String send = "7- " + prefix + log.getMessage().getFormattedMessage();
			
			Enumeration<PrintStream> printerList = toSend.elements();
			while(printerList.hasMoreElements()){
				PrintStream print = printerList.nextElement();
				print.println(send);
				print.flush();
			}
		}
		return null;
	}






	/*@Override
	public void close() throws SecurityException {
		toSend.close();
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord log) {
	 calendar.setTimeInMillis(log.getMillis());
	 int hour = calendar.get(Calendar.HOUR_OF_DAY);
	 int minut = calendar.get(Calendar.MINUTE);
	 int second = calendar.get(Calendar.SECOND);
	 String hourS = (hour > 10) ? String.valueOf(hour) : "0" + hour;
	 String minutS = (minut > 10) ? String.valueOf(minut) : "0" + minut;
	 String secondS = (second > 10) ? String.valueOf(second) : "0" + second;

	 String prefix = "[" + hourS + ":" + minutS + ":" + secondS + " ";

	 String level = log.getLevel().getName();
	 switch(level){
	 case "INFO":
	 	prefix = prefix.concat("INFO]: ");
	 	break;
	 case "WARNING":
		 prefix = prefix.concat("WARN]: ");
		break;
	 case "SEVERE":
		 prefix = prefix.concat("ERROR:] ");
		break;
	default:
		prefix = prefix.concat("TO DEFINE : " + log.getLevel().getName());
		break;
	 }

	 toSend.println("7- " + prefix + log.getMessage());
	 toSend.flush();
	}*/
}
