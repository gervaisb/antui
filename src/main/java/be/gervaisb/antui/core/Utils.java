/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.core;

import java.net.URL;

public abstract class Utils {

	/**
	 * Remove the visibility character, add spaces and change cases from the 
	 * received name. 
	 * 
	 * @param name Original name.
	 * @return The new formated name
	 */
	public static String formatTargetName(final String name) {
		String formated = name;
		if ( name.length()>1 ) {
			// Remove the visibility character, add spaces and change cases
			formated = name.trim().substring(1).replaceAll("[-|+|_]", " ");
			formated = Character.toUpperCase(formated.charAt(0))+formated.substring(1);
		}
		return formated;
	}
	
	public static URL getTasksFile(final String projectName) {
		return Utils.class.getClassLoader().getResource(projectName+"/tasks.xml");
	}
	
	public static URL getViewsFile(final String projectName) {
		return Utils.class.getClassLoader().getResource(projectName+"/views.xml");
	}
	
}
