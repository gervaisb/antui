/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import sun.misc.BASE64Encoder;
import be.gervaisb.ogam.commons.logging.Logger;
import be.gervaisb.ogam.commons.logging.LoggerFactory;
import be.gervaisb.antui.core.DeployerEvent;
import be.gervaisb.antui.core.DeployerListener;
import be.gervaisb.antui.core.impls.remote.RemoteTarget;

import com.thoughtworks.xstream.XStream;

public class LogServer extends Thread implements DeployerListener {

	private final static Logger LOG = LoggerFactory.getLogger(LogServer.class);
	
	private final BASE64Encoder encoder = new BASE64Encoder();
	private final XStream xStream = new XStream();
	private final ServerSocket socket;
	
	private final List<Socket> sockets = new ArrayList<Socket>();	
	
	public LogServer(final int port) throws IOException {
		socket = new ServerSocket(port);		
	}

	@Override
	public void run() {
		Socket remote = null;
		try {
			while ( !isInterrupted() && (remote = socket.accept())!=null ) {
				LOG.info("Adding remote evente listener from ["+remote.getInetAddress()+"].");
				sockets.add(remote);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void messageLogged(DeployerEvent event) {
		send(event);		
	}
	
	@Override
	public void targetFinished(DeployerEvent event) {
		send(event);
	}
	
	@Override
	public void targetStarted(DeployerEvent event) {
		send(event);
	}
	
	@Override
	public void taskFailed(DeployerEvent event) {
		send(event);
	}	
	
	private void send(DeployerEvent event) {
		DeployerEvent sendable;
		// Avoid marshalling LocalTargets
		if ( event.getTarget()!=null ) {
			sendable = new DeployerEvent(null, event.getPriority(), 
				new RemoteTarget(event.getTarget()), event.getMessage(), 
				event.getTask(), event.getException());
		} else {
			sendable = event;
		}
		
		for (final Socket remote : sockets) {
			if ( remote.isClosed() ) {
				sockets.remove(remote);
			} else {
				PrintWriter out = null;
				try {
					out = new PrintWriter(remote.getOutputStream());
					String message = xStream.toXML(sendable);
					out.println(encoder.encode(message.getBytes()));
					out.println("EOE");
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}
	}

}
