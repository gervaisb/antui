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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.Map.Entry;

import sun.misc.BASE64Decoder;
import be.gervaisb.ogam.commons.logging.Logger;
import be.gervaisb.ogam.commons.logging.LoggerFactory;
import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.DeployerListener;
import be.gervaisb.antui.core.Target;
import be.gervaisb.antui.server.Command;
import be.gervaisb.antui.server.Command.Qualifiers;
import be.gervaisb.antui.server.Command.Verbs;

import com.thoughtworks.xstream.XStream;

public class RemoteDeployer implements Deployer {
	
	private final static Logger LOG = LoggerFactory.getLogger(RemoteDeployer.class);
	private final RemoteDeployerEventsDispatcher dispatcher;
	private final BASE64Decoder decoder;
	private final XStream xStream;
	private final Socket socket;
	
	private BufferedReader serverIn;
	private PrintWriter serverOut;
	
	private Target[] targets;
	private String appName;
	
	
	public RemoteDeployer(InetAddress address, String project) throws IOException {
		this(address, 6543, project);
	}
	
	public RemoteDeployer(InetAddress address, int port, String project) throws IOException {
		try {
			socket = new Socket(address, port);
			dispatcher = new RemoteDeployerEventsDispatcher(address, port+1);
			dispatcher.start();
		} catch (IOException ioe) {
			LOG.error("Failed to connect to server ["+address+":"+port+"] : "+
					ioe.getMessage());
			throw ioe;
		}
		
		try {
			serverIn = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			serverOut = new PrintWriter(socket.getOutputStream());
		} catch (IOException ioe) {
			LOG.error("Unable to open communications stream on ["+address+"] : "
					+ioe.getMessage());
			
			if ( socket!=null && socket.isConnected() ) {
				socket.close();
			}
			if ( serverOut!=null ) { serverOut.close(); }
			if ( serverIn!=null ) { serverIn.close(); }
			
			throw ioe;
		}
		
		decoder = new BASE64Decoder();
		xStream = new XStream();
	}
	
	
	@Override
	public String getAppName() {
		if ( appName==null ) {
			appName = execute(Command.New(Verbs.GET, Qualifiers.AppName));
		}
		return appName;
	}

	@Override
	public Target[] getTargets() {
		if ( targets==null ) {
			targets = execute(Command.New(Verbs.GET, Target.class.getName()));
		}
		return targets;
	}
	
	@Override
	public void addListener(DeployerListener listener) {
		dispatcher.addListener(listener);
	}
	
	@Override
	public void removeListener(DeployerListener listener) {
		dispatcher.removeListener(listener);
	}
	
	@Override
	public Target findTarget(String target) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void execute(Target target, Properties properties, boolean background) {
		Properties props = new Properties();
		for (final Entry<Object, Object> property : properties.entrySet()) {
			props.put(property.getKey(), property.getValue());
		}
		props.put("background", Boolean.valueOf(background));
		execute(Command.New(Verbs.EXECUTE, target.getId(), props));
	}

	@Override
	public void exit() {
		execute(Command.New(Verbs.CLOSE));
		try {
			socket.close();
		} catch (IOException e) {/**/}
		System.exit(0);
	}	

	@Override
	public Target[] getPendingTargets() {
		return execute(Command.New(Verbs.GET, Qualifiers.PendingTargets));
	}

	@Override
	public boolean isWorking() {
		return ((Boolean) execute(Command.New(Verbs.GET, Qualifiers.IsWorking)))
			.booleanValue();
	}
	
	// ~ Protected methods -----------------------------------------------------
	
	/** Used to close the socket connection when this object is destroyed. */
	@Override
	protected void finalize() throws Throwable {
		if ( socket!=null && !socket.isClosed() ) {
			socket.close();
		}
	}
	
	// ~ Private methods -------------------------------------------------------
	
	@SuppressWarnings("unchecked")
	private <T> T execute(final Command command) {
		LOG.info("Executing command ["+command+"]");
		T result = null;

		serverOut.println(command.toString());
		serverOut.flush();
		try {	
			String line = null;
			StringBuffer response = new StringBuffer();
			while ( (line = serverIn.readLine())!=null && !"EOR".equals(line)) {
				response.append(line);
			}
			
			if ( response.length()>0 ) {
				String decoded = new String(decoder.decodeBuffer(response.toString()));
				LOG.debug("Receiving response ##\n"+decoded+"\n##.");
				result = (T) xStream.fromXML(decoded);				
			} else {
				LOG.debug("Receiving empty response.");
			}
		} catch (IOException respEx) {
			LOG.error("Failed to read command response.", respEx);
		}
		return result;
	}

}
