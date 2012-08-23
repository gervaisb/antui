/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.swing.JOptionPane;

import be.gervaisb.ogam.commons.ui.components.OgSplashWindow;
import be.gervaisb.antui.client.ui.swing.DeployerFrame;
import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.impls.local.LocalDeployer;
import be.gervaisb.antui.core.impls.remote.RemoteDeployer;

/** 
 * Main class used to start the application. She check and parse command line 
 * arguments to initialize and start the good {@link Deployer} implementation.
 * 
 * @author Gervais
 */
public class Launcher {
	
	/** 
	 * Location of the splash image used when the deployer gui start. This 
	 * location is relative to the Launcher class. 
	 * <p>If <tt>null</tt> no splash screen is displayed.
	 */
	private final static String SPLASH_IMAGE = null;

	/**
	 * Allow user to start the deployer. The deployer can work in many mode 
	 * regarding the received parameters. If it fail it return a negative status
	 * code.
	 * 
	 * <p><b>Allowed Launching parameters:</b><br><code>
	 * 	deployer.jar &lt;project[@server:port]&gt; [target [arg1[,arg2[,..]]]] [-noui]</code>
	 * <dl>
	 * <dt><tt>project</tt>		<dd>The name of the project who will be managed
	 * 							with the deployer. <b>This argument is mandatory
	 * 							.</b>
	 * <dt><tt>server</tt>		<dd>The name or ip address for the server. If 
	 * 							this argument is found the deployer start as 
	 * 							client.
	 * <dt><tt>port</tt>		<dd>The port number of the server to connect on. 
	 * 							If none, the default <strong>6543</strong> is 
	 * 							used.
	 * <dt><tt>target</tt>		<dd>The target to be executed. If this argument 
	 * 							is found the deployer always run as console
	 * <dt><tt>arg</tt>			<dd>An argument for the target to execute. If 
	 * 							the argument is a property, it will be on the 
	 * 							key value form : key=value.
	 * <dt><tt>-noui</tt>	  	<dd>Force the deployer to be executed in console.
	 * </dl>
	 * 
	 * <p><b>Status code:</b><br>
	 * <dl>
	 * <dt><tt>-1</tt>	<dd>Unknown host. The server with the given hostname or
	 * 					IP address was not found.
	 * <dt><tt>-2</tt>	<dd>Cannot connect to server. The server cannot be found
	 * 					or reject received commands.
	 * <dt><tt>-3</tt>	<dd>Bad usage, invalid arguments. One or more received 
	 * 					arguments is misplaced or unknown.
	 * <dt>
	 * <dt>
	 * </dl>
	 */
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		int port = 6543;
		String server = null;
		String target = null;
		String project = null;	
		String[] arguments  = null;
		//boolean noui = false;
		
		if ( args.length==0 ) {
			usage();
		} else {
			for (int i=0; i<args.length; i++) {
				switch (i) {
				case 0:
					if ( args[i].indexOf('@')!=-1 ) {
						project = args[i].substring(0, args[i].indexOf('@'));
						server = args[i].substring(args[i].indexOf('@')+1);
						if ( server.indexOf(':')!=-1 ) {
							port = Integer.parseInt(server.substring(server.indexOf(':')+1));
							server = server.substring(0, server.indexOf(':'));
						}
						
					} else {
						project = args[i];	
					}
					break;
				case 1:
					if ( "-noui".equals(args[i]) ) {
						//noui = true;	
					} else {
						target = args[i];
					}
					
					break;
				case 2:
					if ( "-noui".equals(args[i]) ) {
						//noui = true;	
					} else {
						arguments = args[i].split(",");
					}
					break;
				default :
					System.err.println("Bad arguments. Valid usage is :");
					usage();			
				}
			}
			
			Deployer deployer = null;
			if ( server!=null ) {
				try {
					deployer = new RemoteDeployer(InetAddress.getByName(server), port, project);
				} catch (UnknownHostException e) {
					deployer = null;
					System.err.println("Unknown host "+server+".");
					System.exit(-1);
				} catch (IOException e) {
					deployer = null;
					System.err.println("Cannot connect to "+server+".");
					System.exit(-2);
				}
			} else {
				deployer = new LocalDeployer(project);				
			}
				
			/* If we have a target and this target is found. We have to execute 
			 * it in console mode. Else we have to start the Gui. */ 
			if ( target!=null && deployer.findTarget(target)!=null ) {
				Properties properties = new Properties();
				for (String argument : arguments) {
					String[] parts = argument.split("[=|:]");
					properties.put(parts[0].trim(), parts[1].trim());
				}
				deployer.execute(deployer.findTarget(target), properties, false);
			} else {
				if ( SPLASH_IMAGE!=null ) {
					URL imageUrl = Launcher.class.getClassLoader().getResource(
							Launcher.class.getPackage().getName().replace('.', '/')+'/'+SPLASH_IMAGE);
					OgSplashWindow.splash(imageUrl);
				}
				
				new DeployerFrame(deployer, project).setVisible(true);
				
				if ( SPLASH_IMAGE!=null ) {
					OgSplashWindow.disposeSplash();
				}
			}					
		}
		
	}
	
	public static void usage() {
		final String usage = "Usage : "+
		 " 	deployer.jar <project[@server:port]project> [target [arg1[,arg2[,..]]]] [-noui]\n"+
		 "  Arguments : \n"+
		 "     server    The name or ip address for the server. If this argument\n"+ 
		 "               is found the deployer start as client.\n"+
		 "     project   The name of the project who will be managed with the\n" +
		 "               deployer.     This argument is mandatory.\n"+
		 "     target    The target to be executed. If this argument is found\n"+ 
		 "               the deployer always run as console.\n"+
		 "     arg       An argument for the target to execute. If the argument\n"+
		 "               is a property, it will be on the key value form : \n"+
		 "               key=value.\n"+
		 "     -noui     Force the deployer to be executed in console.\n";	
		
		System.out.println(usage);
		if ( !GraphicsEnvironment.isHeadless() ) {
			JOptionPane.showMessageDialog(null, usage, "Bad arguments", 
					JOptionPane.WARNING_MESSAGE);
					
		}
		System.exit(-3);
	}
	
}
