package com.minecraftnews.theadri1900;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class KonsolePersonalLogger {
	private Path archivePath, lastestPath;
	private LinkedList<String> buffer;
	private KonsolePersonalLoggerHeart autoFlusher;

	public KonsolePersonalLogger(KonsoleServer plugin, Path logPath) {

		try{
			Files.createDirectory(logPath);
		}catch(IOException e){
			if(e instanceof FileAlreadyExistsException){

			}
			else {
				e.printStackTrace();
			}
		}



		archivePath = Paths.get(logPath.toString().concat("/archive.zip"));
		lastestPath = Paths.get(logPath.toString().concat("/lastest.log"));

		// on doit vérifier si il existe le fichier lastest, afin de le renommer puis de le mettre dans l'archive zip.

		if(Files.exists(lastestPath)){
			// si il existe, on doit le placer dans le fichier archive.zip, mais attention, lui aussi doit exister, ou on doit le créer

			if(!Files.exists(archivePath)){
				// si il existe pas, alors on le créé !
				try {
					Map<String, String> env = new HashMap<>(); 
					env.put("create", "true");
					// locate file system by using the syntax 
					// defined in java.net.JarURLConnection
					URI uri = URI.create("jar:file:" + archivePath.toUri().getRawPath());
					FileSystem zipfs = FileSystems.newFileSystem(uri, env);
					zipfs.close();
				} catch (IOException  e) {
					e.printStackTrace();
				} 
			} 
			// on va lire la première ligne du fichier où est écrit la date du fichier afin de pouvoir l'archiver.
			try (FileSystem zip = FileSystems.newFileSystem(archivePath, null); BufferedReader reader = Files.newBufferedReader(lastestPath);) {

				String firstLine = reader.readLine();
				// la ligne : "Log at yyyy-MM-dd", on enlève le "Log at "
				String parsedLine = firstLine.substring(7);

				DirectoryStream<Path> stream = Files.newDirectoryStream(zip.getPath("/"));

				String finalFileName = null;
				boolean finalFileNameFound = false;
				try{
					// on ouvre la liste des fichiers dans le zip pour vérifier si un autre log du même jour existe. Si oui, alors on change le nom.
					int i = 1;
					String tempFileName = parsedLine.concat(" -").concat(String.valueOf(i).concat(".log"));
					LinkedList<Path> allFileName = getLinkedList(stream.iterator());
					while(!finalFileNameFound){
						boolean sameName = false;

						for(Path cursorPath : allFileName){
							if(cursorPath.getFileName().toString().equals(tempFileName)){
								sameName = true;
								break;
							}
						}
						if(!sameName){
							finalFileName = tempFileName;
							finalFileNameFound = true;
						}
						else {
							i++;
							tempFileName = parsedLine.concat(" -").concat(String.valueOf(i).concat(".log"));
						}

					}
				}finally{
					stream.close();
				}

				Path target = zip.getPath("/", finalFileName);

				Files.copy(lastestPath, target, StandardCopyOption.COPY_ATTRIBUTES);

				Files.delete(lastestPath);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// là on est sur que le fichier lastest n'existe pas !

		try{
			// on écrit juste la date de début de fichier.
			String firstLine = "Log at ".concat(new SimpleDateFormat("yyyy-MM-dd").format(new Date()).concat("\n"));
			Files.write(lastestPath, firstLine.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		buffer = new LinkedList<String>();
		autoFlusher = new KonsolePersonalLoggerHeart(this);

		autoFlusher.runTaskTimer(plugin, 400L, 400L);

	}

	public void localLog(String message){
		Date date = new Date();
		String prefix = new SimpleDateFormat("[yyyy-MM-dd").format(date).concat(" at ")
				.concat(new SimpleDateFormat("HH").format(date).concat("h")
						.concat(new SimpleDateFormat("mm").format(date)).concat(":")
						.concat(new SimpleDateFormat("ss").format(date)).concat("] :"));
		buffer.add(prefix.concat(message));
	}

	public void closeLogger(){
		flush();
		autoFlusher.cancel();
	}

	public void flush(){
		//lors d'un flush on ouvre le flux avec le fichier et on écrit la suite dedans.

		//on commence tout le bordel si ya au moins 1 petit message à écrire !!
		if(buffer.size() > 0){
			try(FileWriter fw = new FileWriter(lastestPath.toFile(), true) ; BufferedWriter bw = new BufferedWriter(fw) ; PrintWriter printer = new PrintWriter(bw)){				

				for(String message : buffer){
					printer.println(message);
				}

				printer.flush();
				buffer = new LinkedList<String>();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private LinkedList getLinkedList(Iterator it){
		LinkedList list = new LinkedList<>();
		while(it.hasNext()){
			list.add(it.next());
		}
		return list;
	}


}
