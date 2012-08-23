/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.core;

import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Wrapper around a Target. She is used to store a target into a stack and 
 * execute it when neeed. 
 * She hold the target and a set of properties passed when the user put the 
 * target into the stack.
 * 
 * @author Gervais
 */
public class TargetExecutor {
	
	private final Properties properties;
	private final Target target;
	
	public TargetExecutor(final Target target, final Properties properties) {
		this.properties = new Properties();
		this.properties.putAll(properties);
		this.target = target;
	}
	
	public void execute(final Project project) {
		for (Object name : properties.keySet()) {
			project.setProperty((String)name, (String)properties.get(name));
		}
		
		try {
			project.executeTarget(target.getId());
		} catch (BuildException e) {
			e.printStackTrace();
		}
	}

}
