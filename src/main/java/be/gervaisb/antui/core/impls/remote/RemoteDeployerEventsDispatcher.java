/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.core.impls.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.event.EventListenerList;

import sun.misc.BASE64Decoder;
import be.gervaisb.ogam.commons.logging.Logger;
import be.gervaisb.ogam.commons.logging.LoggerFactory;
import be.gervaisb.antui.core.DeployerEvent;
import be.gervaisb.antui.core.DeployerListener;

import com.thoughtworks.xstream.XStream;

/** 
 * A {@link Thread} who read events from the server and dispatch these events to
 * all {@link DeployerListener}.
 * 
 * @author Gervais
 */
public class RemoteDeployerEventsDispatcher extends Thread {

	private final static Logger LOG = LoggerFactory.getLogger(RemoteDeployerEventsDispatcher.class);
	private final EventListenerList listeners = new EventListenerList();

	private final BASE64Decoder decoder;
	private final XStream xStream;
	private final Socket socket;
	
	private DeployerEvent previous;
	
	public RemoteDeployerEventsDispatcher(final InetAddress address, int port) 
		throws IOException {
		try {
			socket = new Socket(address, port);
		} catch (IOException ioe) {
			LOG.error("Failed to connect to server ["+address+":"+port+"] : "+
					ioe.getMessage());
			throw ioe;
		}
		
		decoder = new BASE64Decoder();
		xStream = new XStream();
	}
	
	public void addListener(final DeployerListener lstnr) {
		listeners.add(DeployerListener.class, lstnr);
	}
	
	public void removeListener(final DeployerListener lstnr) {
		listeners.remove(DeployerListener.class, lstnr);
	}
	
	protected void dispatch(DeployerEvent event) {	
		if ( previous!=null && previous.equals(event) )
			return;
		
		previous = event;
		final DeployerListener[] lstnrs = listeners.getListeners(DeployerListener.class);
		if ( event.getTask()!=null && event.getException()!=null ) {
			for (int i=lstnrs.length-1; i>=0; i--) {
				lstnrs[i].taskFailed(event);
			}
		} else if ( event.getTarget()!=null && event.getTask()==null ) {
			for (int i=lstnrs.length-1; i>=0; i--) {
				lstnrs[i].targetStarted(event);
			}
		} else {
			for (int i=lstnrs.length-1; i>=0; i--) {
				lstnrs[i].messageLogged(event);
			}
		}
				
	}
	
	@Override
	public void run() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
			String line;
			StringBuffer eventString = new StringBuffer();
			while ( !socket.isClosed() ) {
				while ( (line = in.readLine())!=null && !"EOE".equals(line) ) {
					eventString.append(line);
				}
				if ( eventString.length()>0 ) {
					String decoded = new String(decoder.decodeBuffer(eventString.toString()));
					LOG.debug("Receiving response ##\n"+decoded+"\n##.");
					dispatch((DeployerEvent) xStream.fromXML(decoded));	
					eventString.replace(0, eventString.length(), "");
				}
			}			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
}
