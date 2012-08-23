/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import be.gervaisb.ogam.commons.logging.Logger;
import be.gervaisb.ogam.commons.logging.LoggerFactory;
import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.impls.local.LocalDeployer;

public class Server extends Thread {
	
	/** 
	 * Start the deployer server. The server will start for a given project on 
	 * a port. The server use two ports, who are used for the server itself and 
	 * for logs server who use the server port +1. 
	 * 
	 * @param args Where the fisrt is the project name and the second the port 
	 * to use.
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if ( args==null || args.length!=2 ) {
			System.out.println("Usage : "+
				"  server.jar <project> <port>\n"+
				"  Arguments : \n"+
				"     project   The name of the project who will be managed with the\n"+
				"               deployer.     This argument is mandatory.\n"+
				"     port      The server port. Note that teh server use the given\n"+
				"               port and this port +1.     This argument is mandatory.\n");
			System.exit(-3);
		} else {
			new Server(args[0], Integer.parseInt(args[1])).start();
		}
	}
	
	// ~ ------------------------------------------------------------------ ~ //
	
	private final static Logger LOG = LoggerFactory.getLogger(Server.class);
	
	private final CommandHandler rootHandler;
	private final ServerSocket socket;
	private final LogServer logServer;
	private final Deployer deployer;
	
	
	public Server(final String project, final int port) throws IOException {
		this(project, port, new RootHandler());
	}
	
	public Server(final String projectName, final int port, 
			final CommandHandler handler) throws IOException {
		this.rootHandler = handler;
		this.socket = new ServerSocket(port);	
		this.logServer = new LogServer(port+1);
		
		deployer = new LocalDeployer(projectName);
		deployer.addListener(logServer);
		
	}
	
	public Deployer getDeployer() {
		return deployer;
	}
	
	@Override
	public void run() {
		LOG.info("Starting server on port ["+socket.getLocalPort()+
				"] and ["+(socket.getLocalPort()+1)+"].");
		logServer.start();
		Socket remote = null;
		try {
			while ( !isInterrupted() && (remote = socket.accept())!=null ) {
				LOG.info("Connection with ["+remote.getInetAddress()+"] started.");
				new Service(this, remote, rootHandler).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
