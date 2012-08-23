/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.server;

import java.util.Properties;
import java.util.Map.Entry;

import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.Target;
import be.gervaisb.antui.server.Command.Verbs;

public class ExecuteCommandHandler implements CommandHandler {

	@Override
	public Object handle(final Server server, final Command command) throws Exception {
		if ( !Verbs.EXECUTE.toString().equals(command.getVerb()) ) {
			return null;
		}
		
		final Deployer deployer = server.getDeployer();
		final Target target = deployer.findTarget(command.getQualifier());
		Boolean background = Boolean.FALSE;
		Properties properties = new Properties();
		for (final Entry<String, String> parameter : command.getParameters().entrySet()) {
			if ( !"background".equals(parameter.getKey()) ) {
				properties.put(parameter.getKey(), parameter.getValue());
			} else {
				background = Boolean.valueOf(parameter.getValue());
			}
		}	
		deployer.execute(target, properties, background.booleanValue());		
		return Void.class;		
	}
	
}
