/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.server;

import be.gervaisb.antui.core.Target;
import be.gervaisb.antui.core.impls.remote.RemoteTarget;
import be.gervaisb.antui.server.Command.Qualifiers;
import be.gervaisb.antui.server.Command.Verbs;

public class GetCommandHandler implements CommandHandler {

	@Override
	public Object handle(final Server server, final Command command) throws Exception {
		if ( !Verbs.GET.toString().equals(command.getVerb()) &&
			 command.getQualifier()!=null ) {
			return null;
		}
		
		Object result = null;
		if ( Target.class.getName().equals(command.getQualifier()) ) {
			final Target[] targets = server.getDeployer().getTargets();
			for (int i=0; i<targets.length; i++) {
				targets[i] = new RemoteTarget(targets[i]);
			}
			result = targets;
		} else {		
			switch (Qualifiers.valueOf(command.getQualifier())) {
			case AppName:
				result = server.getDeployer().getAppName();
				break;
			case IsWorking:
				result = Boolean.valueOf(server.getDeployer().isWorking());
				break;
			case PendingTargets:
				final Target[] targets = server.getDeployer().getPendingTargets();
				for (int i=0; i<targets.length; i++) {
					targets[i] = new RemoteTarget(targets[i]);
				}
				result = targets;
			}
		}
		return result;		
	}
	
}
