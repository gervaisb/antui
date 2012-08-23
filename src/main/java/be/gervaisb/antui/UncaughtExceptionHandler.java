/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui;

import java.awt.GraphicsEnvironment;

import be.gervaisb.ogam.commons.ui.components.OgErrorPane;
import be.gervaisb.antui.core.Deployer;


/**
 * {@link java.lang.Thread.UncaughtExceptionHandler} for the {@link Deployer} 
 * application. She display and error message to the current error stream and 
 * pop a {@link OgErrorPane} if the application is not in headless mode.
 * 
 * @author Gervais
 * @version $REV$
 */
public class UncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		final String message = new StringBuilder()
			.append("An ").append(e.getClass().getSimpleName()).append(" has been received.\n")
			.append("The deployer will exit.")
			.toString();
		
		System.err.println(message);
		e.printStackTrace(System.err);
		if ( !GraphicsEnvironment.isHeadless() ) {
			new OgErrorPane(e, message).setExpanded(true)
				.createDialog(null).setVisible(true);					
		}
		System.exit(-4);
	}

}
