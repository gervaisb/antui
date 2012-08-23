/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.client.ui.swing;

import java.awt.GridBagLayout;
import java.util.Map;
import java.util.Properties;

import javax.swing.JPanel;

/**
 * Abstract class used to describe a target to the user and force the required 
 * methods for any view. 
 * <p>This class is a {@link JPanel} with a {@link GridBagLayout}
 * 
 * @author Gervais
 */
public abstract class View extends JPanel {
	
	/** Serial version UID */
	private static final long serialVersionUID = -7857765225815414985L;

	public View() {
		this(null);
	}
	
	public View(final Map<String, Object> attributes) {
		super(new GridBagLayout());
	}
	
	/**
	 * Used to gather properties required for the target represented wit this 
	 * view. 
	 */
	public abstract Properties getProperties();
	
}
