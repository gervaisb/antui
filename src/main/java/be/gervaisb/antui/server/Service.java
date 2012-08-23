/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;

import sun.misc.BASE64Encoder;
import be.gervaisb.ogam.commons.logging.Logger;
import be.gervaisb.ogam.commons.logging.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public class Service extends Thread {

	private final CommandHandler root;
	private final Socket socket;
	private final Server server;
	private final Logger log;
	
	
	public Service(final Server server, final Socket socket, final CommandHandler root) {
		this.log = LoggerFactory.getLogger(Service.class);
		this.socket = socket;
		this.server = server;
		this.root = root;
	}
	
	@Override
	public void run() {
		BufferedReader request = null;
		PrintWriter out;
		Object result = null;
		try {
			request = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			BASE64Encoder encoder = new BASE64Encoder();
			XStream xStream = new XStream();
			String line = null;
			while ( socket.isConnected() ) {
				while ( !socket.isClosed() && (line = request.readLine())!=null ) {
					try {
						log.debug("Receiving command ["+line+"].");
						Command command = Command.parse(line);
						if ( Command.Verbs.CLOSE.toString().equals(command.getVerb()) ) {
							log.info("Close command received. Closing socket.");
							socket.close();
						} else {
							result = root.handle(server, command);
							if ( result!=null ) {
								log.info("Handler for command ["+line+"] returned ["+result+"].");
								String responseString = xStream.toXML(result);
								log.debug("Sending response ##\n"+responseString+"\n##.");
								out.println(encoder.encode(responseString.getBytes()));
								out.println("EOR");
								out.flush();
							} else {
								log.info("No handler found for command ["+line+"]. No response.");
							}	
						}					
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (request!=null) { request.close();} } catch (IOException e) {
				log.error("Cannot close reader on client stream correctly.", e);
			}
			try { socket.close(); } catch (IOException e) {
				log.error("Cannot close client socket correctly.", e);
			}		
			log.info("Service finished.");
		}
	}
		
}
