/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.core;

import java.util.Properties;


/**
 * Commons interface for the Deployer. The deployer is the controller class
 * 
 * @author Gervais
 */
public interface Deployer {

	String getAppName();
	
	Target[] getTargets();
	
	Target findTarget(String target);
	
	Target[] getPendingTargets();
	
	/**
	 * Execute the given target or put it into a stack to execute it in background
	 * @param target
	 * @param properties
	 * @param background
	 */
	void execute(final Target target, final Properties properties, 
			boolean background);
	
	void exit();

	boolean isWorking();	
	
	void addListener(final DeployerListener listener);
	
	void removeListener(final DeployerListener listener);

	
	
}
