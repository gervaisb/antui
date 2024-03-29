/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.core;

import java.util.EventListener;

/**
 * Descriptor for any class who want to receive events from a {@link Deployer}
 * 
 * @author Gervais
 */
public interface DeployerListener extends EventListener {
	
	void messageLogged(DeployerEvent event);
	
	void targetStarted(DeployerEvent event);
	
	void targetFinished(DeployerEvent event);
	
	void taskFailed(DeployerEvent event);
}
