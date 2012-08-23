/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.tools.ant.Project;

import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.DeployerEvent;
import be.gervaisb.antui.core.DeployerListener;

/**
 * A {@link JDialog} who show deployer's logs. She display a set of radio 
 * buttons for each message level. The user can select/unselect these buttons to 
 * display/hide messages for a level. The sate of these buttons is persisted 
 * into the user preferences.  
 */
public class ConsoleDialog extends JDialog {
	
	/** Serial version UID */
	private static final long serialVersionUID = 2334468526872875011L;
	protected static final Preferences prefs = Preferences.userNodeForPackage(ConsoleDialog.class);
		
	protected final boolean[] levels = new boolean[5];
	protected final JTextArea console;
	
	public ConsoleDialog(final Deployer deployer) {
		super((Frame)null, deployer.getAppName()+" deployer's console");
		setLayout(new BorderLayout());
		// SE6 setIconImage(new ImageIcon(DeployerFrame.class.getClassLoader()
		//		.getResource("be/gervaisb/ogam/deployer/client/ui/swing/resources/icons/Icon.png"))
		//		.getImage());
	
		final JToolBar tools = new JToolBar();
		tools.add(new InternalLogLevelRadio(Project.MSG_DEBUG, "Debug"));
		tools.add(new InternalLogLevelRadio(Project.MSG_VERBOSE, "Verbose"));		
		tools.add(new InternalLogLevelRadio(Project.MSG_INFO, "Info"));
		tools.add(new InternalLogLevelRadio(Project.MSG_WARN, "Warning"));
		tools.add(new InternalLogLevelRadio(Project.MSG_ERR, "Error"));
		
		console = new JTextArea(new InternalConsoleModel(deployer));
		Color background = console.getBackground();
		console.setBackground(console.getForeground());
		console.setForeground(background);
		console.setFont(getFont());
		
		add(tools, BorderLayout.PAGE_START);
		add(new JScrollPane(console), BorderLayout.CENTER);
		
		setSize(800, 600);	
		setLocationByPlatform(true);
	}
	
	@Override
	protected void finalize() throws Throwable {
		prefs.flush();
		super.finalize();
	}
	
	// ~ Internal classes ------------------------------------------------------
	
	protected final class InternalConsoleModel extends PlainDocument 
			implements DeployerListener {
		private static final long serialVersionUID = 4064158010238439471L;

		public InternalConsoleModel(final Deployer deployer) {
			deployer.addListener(this);
		}
		
		@Override
		public void messageLogged(DeployerEvent event) {
			if ( levels[event.getPriority()] ) {
				append("    [").append(getLevelName(event.getPriority()))
					.append("] ").append(event.getMessage()==null?"":event.getMessage()).append("\n");
			}
		}

		@Override
		public void targetStarted(DeployerEvent event) {
			append("[")
				.append(event.getTarget()!=null
						?event.getTarget().getName()
						:"<Init>")
				.append("]\n");			
		}
		
		@Override
		public void targetFinished(DeployerEvent event) {
			append("\n");		
			if ( event.getException()!=null && 
				(levels[Project.MSG_WARN] || levels[Project.MSG_ERR]) ) {
				append(" Target ")
					.append(event.getTarget()!=null
							?event.getTarget().getName()
							:"<Init>")
				.append("failed :\n").append(event.getException()).append("\n");
			}
		}
		
		@Override
		public void taskFailed(DeployerEvent event) {
			if ( (levels[Project.MSG_WARN] || levels[Project.MSG_ERR]) ) {
				append(" Task ").append(event.getTask())
					.append(" failed :\n").append(event.getException())
					.append("\n");
			}
		}
		
		protected InternalConsoleModel append(final Throwable throwable) {
			StringWriter writer = new StringWriter();
			throwable.printStackTrace(new PrintWriter(writer));
			return append(writer.toString());
		}
		
		protected InternalConsoleModel append(final String string) {
			try {
				insertString(getLength(), string, null);
				if ( string.contains("\n") ) {
					console.scrollRectToVisible(new Rectangle(0, console.getHeight()-2, 1, 1) );
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			return this;
		}
		
		private String getLevelName(final int level) {
			String name = null;
			switch (level) {
			case Project.MSG_VERBOSE:
				name = "VERBOSE";	break;
			case Project.MSG_DEBUG:
				name = "  DEBUG";	break;
			case Project.MSG_INFO:
				name = "   INFO";	break;
			case Project.MSG_WARN:
				name = "   WARN";	break;
			case Project.MSG_ERR:
				name = "  ERROR";	break;
			}
			return name;
		}
		
	}
	
	protected final class InternalLogLevelRadio extends JCheckBox {
		private static final long serialVersionUID = 1478228210076431460L;
		private final int level;
		
		public InternalLogLevelRadio(final int level, final String name) {
			super(name);
			this.level = level;
			levels[level] = prefs.getBoolean("level."+level, true);
			setSelected(levels[level]);
			addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					setSelected(!levels[level]);
				}
			});
		}
				
		@Override
		public void setSelected(boolean b) {
			levels[level] = b;		
			prefs.putBoolean("level."+level, b);
			super.setSelected(b);
		}		
	}

}
