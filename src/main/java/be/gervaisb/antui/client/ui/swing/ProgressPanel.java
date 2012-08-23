/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.client.ui.swing;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;

import be.gervaisb.antui.client.ui.swing.actions.DisplayConsoleAction;
import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.Target;

/** 
 * A component who display a message and a {@link JProgressBar} followed with a
 * {@link JButton} who allow user to open the console. This component display 
 * all in one line.
 *  
 * @author Gervais
 */
public class ProgressPanel extends JToolBar {

	/** Serial version UID */
	private static final long serialVersionUID = -4368233263981951949L;
	
	private final JProgressBar progressBar;
	private final JLabel progressName;
	
	public ProgressPanel(final Deployer deployer) {
		super();
		setFloatable(false);
		
		add(Box.createHorizontalGlue());
		add(progressName = new JLabel("Target %%% in progress"));
		add(Box.createHorizontalStrut(2));
		add(progressBar = new JProgressBar());
		add(Box.createHorizontalStrut(2));
		
		addSeparator();
		
		((JButton) add(new JButton(new DisplayConsoleAction(deployer, getParent()))))
			.setText(null);
		//((JButton) add(new JButton(new ShowProcessesAction(deployer))))
		//	.setText(null);
	}

	/** Tell to this component to display the given target */
	public void listen(final Target target) {
		progressName.setText("Target \""+target.getName()+"\" in progress.");
		progressBar.setIndeterminate(true);
	}
	
}
