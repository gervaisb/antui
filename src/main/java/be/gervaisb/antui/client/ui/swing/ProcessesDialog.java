/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import be.gervaisb.ogam.commons.ui.builders.GridBagConstraintsBuilder;
import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.DeployerEvent;
import be.gervaisb.antui.core.DeployerListener;
import be.gervaisb.antui.core.Target;

/**
 * Will be display current process and allow user to manage these process
 * @author Gervais
 */
public class ProcessesDialog extends JDialog {
	
	/** Serial version UID */
	private static final long serialVersionUID = 2324685256982875581L;
	protected static final Preferences prefs = Preferences.userNodeForPackage(ProcessesDialog.class);
		
	protected final Deployer deployer;
	
	public ProcessesDialog(final Deployer deployer) {
		super((Frame)null, deployer.getAppName()+" deployer's processes");
		this.deployer = deployer;
		
		setLayout(new BorderLayout());
		// SE6 setIconImage(new ImageIcon(DeployerFrame.class.getClassLoader()
		//		.getResource("be/gervaisb/ogam/deployer/client/ui/swing/resources/icons/Icon.png"))
		//		.getImage());
	
		final Component container = new InternalTargetList(
				new InternalProcessModel(deployer));		
		setContentPane(new JScrollPane(container));
		
		setSize(450, 250);	
		setLocationByPlatform(true);
	}
	
	@Override
	protected void finalize() throws Throwable {
		prefs.flush();
		super.finalize();
	}
	
	
	// ~ Internal classes ------------------------------------------------------
		
	protected final static class InternalProcessModel extends AbstractListModel
			implements DeployerListener {
		private static final long serialVersionUID = -3567087500734488306L;
		
		private final Map<Target, String> messages = new HashMap<Target, String>();
		private final List<Target> targets;
		
		
		public InternalProcessModel(final Deployer deployer) {
			deployer.addListener(this);
			targets = new LinkedList<Target>(Arrays.asList(
					deployer.getPendingTargets()));
		}		

		@Override
		public int getSize() {
			return targets.size();
		}
		
		@Override
		public Object getElementAt(int index) {
			return targets.get(index);
		}		
		
		@Override
		public void targetStarted(DeployerEvent event) {
			targets.add(event.getTarget());	
			messages.put(event.getTarget(), event.getMessage());
			fireIntervalAdded(this, getSize()-1, getSize());
		}
		
		@Override
		public void targetFinished(DeployerEvent event) {
			int index = targets.indexOf(event.getTarget());
			if ( index!=-1 ) {
				targets.remove(event.getTarget());
				fireIntervalRemoved(this, index, index);
			}
		}
		
		@Override
		public void taskFailed(DeployerEvent event) {/**/}
		
		@Override
		public void messageLogged(DeployerEvent event) {
			int index = targets.indexOf(event.getTarget());
			if ( index!=-1 ) {
				messages.put(event.getTarget(), event.getMessage());
				fireContentsChanged(this, index, index);
			}
		}
	}
	
	protected final class InternalTargetList extends Box 
			implements ListDataListener {
		private static final long serialVersionUID = 9027856204347076962L;
		
		private final InternalProcessModel model;
		
		public InternalTargetList(final InternalProcessModel model) {
			super(BoxLayout.Y_AXIS);
			setBackground(Color.WHITE);
			
			this.model = model;		
			this.model.addListDataListener(this);
			intervalAdded(null);
		}
				
		@Override
		public void intervalAdded(ListDataEvent e) {
			invalidate();
			removeAll();
			for (int i=0; i<model.getSize(); i++) {
				add(new InternalProcessRenderer((Target) model.getElementAt(i)));				
			}
			validate();
		}
		
		@Override
		public void contentsChanged(ListDataEvent e) {
			invalidate();
			for (int i=e.getIndex0(); i<e.getIndex1(); i++) {
				//InternalProcessRenderer renderer = (InternalProcessRenderer) 
				//		getComponent(i);
				//Target target = (Target) model.getElementAt(i);
				//renderer.refresh(target, model.messages.get(target));
			}
			validate();			
		}		
		
		@Override
		public void intervalRemoved(ListDataEvent e) {
			invalidate();
			for (int i=e.getIndex0(); i<=e.getIndex1(); i++) {				
				remove(getComponent(i));
			}
			validate();
		}		
	}
	
	protected final static class InternalProcessRenderer extends JPanel {
		private static final long serialVersionUID = 569766215588194208L;
		
		private final JLabel lblLatestMessage;
		private final JProgressBar progress;
		
		public InternalProcessRenderer(final Target target) {
			super(new GridBagLayout());
			setBackground(Color.WHITE);
			final GridBagConstraintsBuilder c = new GridBagConstraintsBuilder();
		
			add(new JLabel(target.getName()), c.pos(0, 0).get());
			
			add(progress = new JProgressBar(), c.pos(0, 1).wx(0.5).both().get());
			add(new JButton(new ImageIcon(DeployerFrame.class.getClassLoader()
					.getResource("be/gervaisb/ogam/deployer/client/ui/swing/resources/icons/Delete.png"))), c.pos(1, 1).get());
			
			add(lblLatestMessage = new JLabel(""), 
					c.pos(0, 2).wx(0.5).cs(2).get());		
			
			progress.setIndeterminate(false);
			setAlignmentY(TOP_ALIGNMENT);
		}
		
		public void refresh(final Target target, final String message) {
			lblLatestMessage.setText(message);
		}
		
	}
	
}
