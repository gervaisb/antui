/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.client.ui.swing.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import be.gervaisb.antui.client.ui.swing.ConsoleDialog;
import be.gervaisb.antui.client.ui.swing.DeployerFrame;
import be.gervaisb.antui.core.Deployer;

/**
 * Manage the the console dialog to open or close it.
 * 
 * @author Gervais
 */
public class DisplayConsoleAction extends AbstractAction {
	
	/** Serial version UID */
	private static final long serialVersionUID = 5769004682842808076L;
	
	public static final String PROPERTY_CONSOLE_VISIBILITY = "CONSOLE_VISIBILITY";
		
	private static boolean visible = false;
	
	private final ConsoleDialog dialog;
			
	public DisplayConsoleAction(final Deployer deployer, final Component parent) {
		super("Display console", new ImageIcon(DeployerFrame.class.getClassLoader()
				.getResource("be/gervaisb/antui/client/ui/swing/resources/icons/Console.png")));
		putValue(SHORT_DESCRIPTION, "Open a frame who display the application messages");
		
		this.dialog = new ConsoleDialog(deployer);
		this.dialog.setLocationRelativeTo(parent);
		this.dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				changeDialogVisibility(false);					
			}				
		});
	}
			
	@Override
	public void actionPerformed(ActionEvent event) {
		changeDialogVisibility(!visible);
	}	
	
	public boolean isConsoleVisible() {
		return visible;
	}
	
	protected void changeDialogVisibility(final boolean visibility) {
		boolean oldValue = DisplayConsoleAction.visible;
		DisplayConsoleAction.visible = visibility;
		dialog.setVisible(visibility);
		
		PropertyChangeListener[] lstnrs = getPropertyChangeListeners();
		PropertyChangeEvent event = null;
		for (int i=lstnrs.length-1; i>=0; i--) {
			if ( event==null ) {
				event = new PropertyChangeEvent(this, PROPERTY_CONSOLE_VISIBILITY, 
						Boolean.valueOf(oldValue), Boolean.valueOf(visibility));
			}
			lstnrs[i].propertyChange(event);
		}
	}
}

