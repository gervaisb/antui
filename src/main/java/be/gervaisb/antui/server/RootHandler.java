/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.server;

import java.util.ArrayList;
import java.util.List;

public class RootHandler implements CommandHandler {

	private final static List<CommandHandler> HANDLERS = new ArrayList<CommandHandler>();
	static {
		HANDLERS.add(new GetCommandHandler());
		HANDLERS.add(new ExecuteCommandHandler());
	}
		
	@Override
	public Object handle(final Server server, final Command command) throws Exception {
		Object result = null;
		for (int i=0; result==null && i<HANDLERS.size(); i++) {
			result = HANDLERS.get(i).handle(server, command);
		}	
		return result;
	}
	
}
