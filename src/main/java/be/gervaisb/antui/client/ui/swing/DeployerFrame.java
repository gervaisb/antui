/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.client.ui.swing;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import be.gervaisb.ogam.commons.ui.components.OgBannerPanel;
import be.gervaisb.ogam.commons.ui.components.OgErrorPane;
import be.gervaisb.ogam.commons.ui.components.OgOptionsDialogs;
import be.gervaisb.antui.client.ui.swing.actions.DisplayConsoleAction;
import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.DeployerEvent;
import be.gervaisb.antui.core.DeployerListener;
import be.gervaisb.antui.core.Target;
import be.gervaisb.antui.core.Utils;
import be.gervaisb.antui.core.impls.local.LocalDeployer;

/**
 * Main frame for controlling a {@link Deployer}. She display a tab for each 
 * public target. And allow the user to execute these targets. 
 * <p>She is created by querying the {@link Deployer} and use the "views.xml" 
 * file to discover the view used to represent a target. If no specific view 
 * is found she display a default view.
 * 
 * @author Gervais
 */
public class DeployerFrame extends JFrame {

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { /**/ }
	}
		
	/** Serial version UID */
	private static final long serialVersionUID = -3582669376024706610L;
	
	private static final String CARD_BUTTONS = "BUTTONS";
	private static final String CARD_WORKING = "WORKING";
	
	private static final int MSG_INFO = JOptionPane.INFORMATION_MESSAGE;
	private static final int MSG_ALERT = JOptionPane.WARNING_MESSAGE;
	private static final int MSG_ERROR = JOptionPane.ERROR_MESSAGE;
	
	private TrayIcon trayIcon = null;
	
	protected final DisplayConsoleAction displayConsoleAction;
	protected final ProgressPanel progressPane;
	protected final JTabbedPane tabbedPane;	
	protected final JPanel bottomPane;
	protected final Deployer deployer;	
	
	protected Target selectedTask;
	protected Target runningTask;	
	
	
	public DeployerFrame(final Deployer deployer, final String project) {
		super("Deployer for "+deployer.getAppName());
		this.deployer = deployer;
			
		setIconImage(new ImageIcon(DeployerFrame.class.getClassLoader()
				.getResource("be/gervaisb/antui/client/ui/swing/resources/icons/Icon.png"))
				.getImage());
		
		setJMenuBar(new JMenuBar());
		
		displayConsoleAction = new DisplayConsoleAction(deployer, this);
			
		final InternalQuitDeployerAction quitAction = new InternalQuitDeployerAction();
		addWindowListener(quitAction);
		
		
		final JMenu mnuFile = new JMenu("File");
		mnuFile.add(new JMenuItem(quitAction));
		getJMenuBar().add(mnuFile);
		
		final JMenu mnuView = new JMenu("View");
		mnuView.add(new InternalManageConsoleMenuItem(displayConsoleAction));
		//mnuView.add(new ShowProcessesAction(deployer));
		getJMenuBar().add(mnuView);
						
		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		getRootPane().setDefaultButton((JButton)
				buttons.add(new JButton(new InternalExecuteAction("Execute", false))));
		buttons.add(new JButton(new InternalExecuteAction("Execute in background", true)));
				
		bottomPane = new JPanel(new CardLayout());
		bottomPane.add(buttons, CARD_BUTTONS);	
		bottomPane.add(progressPane = new ProgressPanel(deployer), CARD_WORKING);
				
		final Image banner = new ImageIcon(DeployerFrame.class.getClassLoader()
			.getResource("be/gervaisb/antui/client/ui/swing/resources/icons/Banner.png"))
			.getImage();
		add(new OgBannerPanel(banner, getTitle()), BorderLayout.PAGE_START);
		add(tabbedPane = new JTabbedPane(), BorderLayout.CENTER);
		add(bottomPane, BorderLayout.PAGE_END);
		
		tabbedPane.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
				DeployerFrame.this.selectedTask = DeployerFrame.this.deployer
						.getTargets()[tabbedPane.getSelectedIndex()];				
			}
		});
		
		try {
			URL viewsUrl = Utils.getViewsFile(project);
			if ( viewsUrl!=null ) {
				Document document = new SAXBuilder().build(viewsUrl);
				for (Target target : deployer.getTargets()) {
					final View view = buildView(
						(Element) XPath.selectSingleNode(document,  
									"/project/view[@task=\""+target.getId()+"\"]"), target);
					if ( view!=null ) {
						tabbedPane.add(target.getName(), view); 
					}
				}
			} else {
				for (Target target : deployer.getTargets()) {
					tabbedPane.add(target.getName(), buildView(null, target));
				}
			}
		} catch (JDOMException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
				
		deployer.addListener(new InternalEventsDispatcher());
		
		addWindowListener(new InternalQuitDeployerAction());
		setMinimumSize(new Dimension(
				(int) (Toolkit.getDefaultToolkit().getScreenSize().width*0.25),
				(int) (Toolkit.getDefaultToolkit().getScreenSize().height*0.25)));
		setLocationByPlatform(true);
		
		
		final String size = Preferences.userNodeForPackage(DeployerFrame.class)
				.get("size", null);		
		if ( size!=null && size.indexOf('|')!=-1 ) {
			setSize(
					Integer.parseInt(size.substring(0, size.indexOf('|'))), 
					Integer.parseInt(size.substring(size.indexOf('|')+1)) );
		} else {
			pack();
		}
	}
	
	@Override
	public void dispose() {
		Preferences.userNodeForPackage(DeployerFrame.class).put("size", 
				getSize().width+"|"+getSize().height);
		super.dispose();
	}
		
	// ~ Private methods -------------------------------------------------------
	
	@SuppressWarnings("unchecked")
	private View buildView(final Element descriptor, final Target target) {
		View view = null;
		if ( descriptor==null || descriptor.getAttributeValue("name")==null ) {
			DefaultView defaultView = new DefaultView();
			String metas = target.getDescription();
			for ( int index=-1; metas!=null && (index = metas.indexOf("[", index))!=-1; index++) {
				String[] meta = metas.substring(index+1, metas.indexOf("]", index)).split(":");
				String label = null;
				if ( descriptor!=null ) {
					try {
						Element element = (Element) XPath.selectSingleNode(descriptor,  
								"labels/label[@key=\""+meta[0].trim()+"\"]");
						if ( element!=null ) {
							label = element.getAttributeValue("value");
						} 
					} catch (JDOMException e) {/**/}
				}
				defaultView.addProperty(meta[0], label==null?meta[0]:label, meta[1]);
			}
			view = defaultView;
		} else {
			try {
				Class<View> clazz = (Class<View>) Class.forName(descriptor.getParentElement()
						.getAttributeValue("basepackage")+"."+descriptor.getAttributeValue("name"));
				if ( descriptor.getChildren().isEmpty() ) {
					view = clazz.getConstructor((Class<?>[]) null)
							.newInstance((Object[]) null);
				} else {
					view = clazz.getConstructor(Map.class)
							.newInstance(new HashMap<String, Object>(0));
				}
			} catch (Exception e) {
				new OgErrorPane(e, "Failed to create view for target "+target.getName())
					.createDialog(this).setVisible(true);
				view = null;
			}
		}
		return view;
	}
	
	private void showMessage(final String title, final String content, int level) {
		boolean failed = true;
		if ( SystemTray.isSupported() ) {
			if ( trayIcon==null ) {
				trayIcon = new TrayIcon(DeployerFrame.this.getIconImage(), DeployerFrame.this.getTitle());
				trayIcon.setImageAutoSize(true);
							
				try {
					SystemTray.getSystemTray().add(trayIcon);
				} catch (AWTException e) {
					trayIcon = null;
				}
			}
			if ( trayIcon!=null ) {
				MessageType type = MessageType.NONE;
				switch (level) {
				case MSG_INFO:
					type = MessageType.INFO; break;
				case MSG_ALERT:
					type = MessageType.WARNING; break;
				case MSG_ERROR:
					type = MessageType.ERROR; break;
				}
				trayIcon.displayMessage(title, content, type);
				failed = false;
			}
		} 
		
		if ( failed ) {
			JOptionPane.showMessageDialog(DeployerFrame.this, content, title, 
					level);
		}
	}
	
	// ~ Internal classes ------------------------------------------------------
	
	protected final class InternalQuitDeployerAction extends AbstractAction implements WindowListener {
		private static final long serialVersionUID = 81064886815363368L;

		public InternalQuitDeployerAction() {
			super("Quit");
			putValue(SHORT_DESCRIPTION, "Quit the deployer and abort all pending tasks");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if ( deployer instanceof LocalDeployer ) {
				if ( (!deployer.isWorking() && deployer.getPendingTargets().length<=0) ||
					 OgOptionsDialogs.showConfirmationDialog(DeployerFrame.this, 
						 "Confirm quit", "Do you want to quit the deployer ?",
						 "Deployer as some pending or running tasks. If you exit it,"+
						 " current tasks will be aborted and pending task removed.\n"+
						 "Do you really want to exit the deployer and abort tasks ?")
						.yesSelected ) {
					dispose();
					deployer.exit();
				}
			} else {
				dispose();
				deployer.exit();
			}
		}
		
		@Override
		public void windowClosing(WindowEvent e) {
			actionPerformed(new ActionEvent(e.getSource(), e.getID(), "STATE_CLOSING"));			
		}
				
		@Override
		public void windowActivated(WindowEvent e) {/**/}
		
		@Override
		public void windowClosed(WindowEvent e) {/**/}
		
		@Override
		public void windowDeactivated(WindowEvent e) {/**/}
		
		@Override
		public void windowDeiconified(WindowEvent e) {/**/}
		
		@Override
		public void windowIconified(WindowEvent e) {/**/}
		
		@Override
		public void windowOpened(WindowEvent e) {/**/}
		
	}
	
	protected final static class InternalManageConsoleMenuItem extends JCheckBoxMenuItem 
			implements PropertyChangeListener {
		private static final long serialVersionUID = -272999012885286424L;
		
		public InternalManageConsoleMenuItem(final DisplayConsoleAction action) {
			super(action);
			getAction().addPropertyChangeListener(this);
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ( DisplayConsoleAction.PROPERTY_CONSOLE_VISIBILITY.equals(evt.getPropertyName()) ) {
				setSelected(((Boolean) evt.getNewValue()).booleanValue());
			}
		}		
	}
	
	protected final class InternalExecuteAction extends AbstractAction {
		private static final long serialVersionUID = -7076048817262642081L;
		
		private final boolean inBackground;
		
		public InternalExecuteAction(final String name, final boolean background) {
			super(name);
			this.inBackground = background;
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			final Properties properties = ((View) tabbedPane.getSelectedComponent())
				.getProperties();
			if ( properties==null )
				return;
			
			if ( inBackground ) {
				deployer.execute(selectedTask, properties, true);
			} else {
				((CardLayout) DeployerFrame.this.bottomPane.getLayout())
					.show(DeployerFrame.this.bottomPane, CARD_WORKING);
				progressPane.listen(selectedTask);
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						deployer.execute(selectedTask, properties, false);
						return null;
					}
					
					@Override
					protected void done() {
						try {
							get();
							((CardLayout) DeployerFrame.this.bottomPane.getLayout())
								.show(DeployerFrame.this.bottomPane, CARD_BUTTONS);
						} catch (InterruptedException e) {
							new OgErrorPane(e, "The task "+selectedTask.getName()
									+" may not be terminated successfully.")
								.createDialog(DeployerFrame.this, "Task interrupted");							
						} catch (ExecutionException e) {
							new OgErrorPane(e, "The task "+selectedTask.getName()
									+" may not be terminated successfully. An "
									+e.getCause().getClass().getSimpleName()+" error as occured : "
									+e.getCause().getMessage()+".")
								.createDialog(DeployerFrame.this, "Task in error.");
						}						
					}					
				}.execute();				
			}
		}		
	}
	
	/** Listen the deployer and popup the console when an error occur. */
	protected class InternalEventsDispatcher implements DeployerListener {
						
		@Override
		public void targetStarted(DeployerEvent event) {/**/}

		@Override
		public void messageLogged(final DeployerEvent event) {
			maybeShowConsole(event);
		}

		@Override
		public void targetFinished(final DeployerEvent event) {
			maybeShowConsole(event);
			showMessage("Task finished", 
					DeployerFrame.this.getTitle()+" has just finsihed to execute "
					+" the task "+event.getTarget().getName()+".", MSG_INFO);
		}

		@Override
		public void taskFailed(final DeployerEvent event) {
			maybeShowConsole(event);
		}
		
		protected void maybeShowConsole(final DeployerEvent event) {
			if ( event.getException()!=null && !displayConsoleAction.isConsoleVisible() ) {
				displayConsoleAction.actionPerformed(new ActionEvent(event, 
						(int) System.currentTimeMillis(), "ERROR_OCCURED"));
				showMessage("Task failed", 
						DeployerFrame.this.getTitle()+" received an error from task "
						+event.getTarget().getName()+" :"+event.getException().getMessage(), MSG_ERROR);
			}
		}			
	}
		
}
