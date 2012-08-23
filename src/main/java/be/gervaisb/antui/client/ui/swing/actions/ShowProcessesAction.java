/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.client.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import be.gervaisb.antui.client.ui.swing.DeployerFrame;
import be.gervaisb.antui.client.ui.swing.ProcessesDialog;
import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.DeployerEvent;
import be.gervaisb.antui.core.DeployerListener;

/** Will be used to display process */
public class ShowProcessesAction extends AbstractAction {

	/** Serial version UID */
	private static final long serialVersionUID = -3988490660355964075L;
	
	public static final String PROPERTY_PROCESSES_VISIBILITY = "PROCESSES_VISIBILITY";
	private static boolean visible = false;
	private static ProcessesDialog dialog;
	
	protected final Deployer deployer;
	
	public ShowProcessesAction(final Deployer deployer) {
		super("Show processes", new ImageIcon(DeployerFrame.class.getClassLoader()
				.getResource("be/gervaisb/antui/client/ui/swing/resources/icons/Processes.png")));
		putValue(SHORT_DESCRIPTION, "Display and control active processes");
		this.deployer = deployer;
		deployer.addListener(new InternalDeployerListener());
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if ( dialog==null ) {
			dialog = new ProcessesDialog(deployer);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					changeDialogVisibility(false);					
				}				
			});
		}
		changeDialogVisibility(!visible);
	}	
	
	protected void changeDialogVisibility(final boolean visibility) {
		boolean oldValue = ShowProcessesAction.visible;
		ShowProcessesAction.visible = visibility;
		dialog.setVisible(visibility);
		
		PropertyChangeListener[] lstnrs = getPropertyChangeListeners();
		PropertyChangeEvent event = null;
		for (int i=lstnrs.length-1; i>=0; i--) {
			if ( event==null ) {
				event = new PropertyChangeEvent(this, PROPERTY_PROCESSES_VISIBILITY, 
							Boolean.valueOf(oldValue), Boolean.valueOf(visibility));
			}
			lstnrs[i].propertyChange(event);
		}
	}
	
	protected final static class InternalDeployerListener implements DeployerListener {
		
		@Override
		public void targetFinished(DeployerEvent event) {
			//ShowProcessesAction.this.setEnabled(
			//		deployer.getPendingTargets().length>0);
		}
		
		@Override
		public void targetStarted(DeployerEvent event) {
			//ShowProcessesAction.this.setEnabled(
			//		deployer.getPendingTargets().length>0);			
		}
		
		@Override
		public void taskFailed(DeployerEvent event) {/**/}
		
		@Override
		public void messageLogged(DeployerEvent event) {/**/}
		
	}
	
}
