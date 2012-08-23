/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.core.impls.local;

import be.gervaisb.antui.core.Target;
import be.gervaisb.antui.core.Utils;

class LocalTarget implements Target {
	
	private org.apache.tools.ant.Target decorated;
	
	public LocalTarget(final org.apache.tools.ant.Target decorated) {
		this.decorated = decorated;
	}

	@Override
	public String getName() {
		return Utils.formatTargetName(decorated.getName());
	}
	
	@Override
	public String getId() {
		return decorated.getName();
	}
	
	@Override
	public String getDescription() {
		return decorated.getDescription();
	}
		
}
