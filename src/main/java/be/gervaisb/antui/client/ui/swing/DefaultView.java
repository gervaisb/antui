/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.client.ui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** 
 * A default view who is used when no specific view is found for a target. She 
 * display each properties one a line with his key as label, a text field and 
 * a description if one is found.
 *   
 * @author Gervais
 */
class DefaultView extends View {

	/** Serial version UID */
	private static final long serialVersionUID = 4330162442760248888L;
	
	private final Map<String, JTextField> fields = new HashMap<String, JTextField>(3);
	private final GridBagConstraints c = new GridBagConstraints();
	
	public DefaultView() {
		super(null);
		setLayout(new GridBagLayout());
		c.gridy = 0;	
		c.insets = new Insets(2, 5, 2, 2);
		c.anchor = GridBagConstraints.NORTHWEST;
	}

	@Override
	public Properties getProperties() {
		Properties properties = new Properties();
		for (String key : fields.keySet()) {
			properties.put(key, fields.get(key).getText());
		}
		return properties;
	}
	
	public void addProperty(final String name, final String label, final String description) {
		final JTextField field = new JTextField(20);
		field.setName(name);		
		
		final JLabel desc = new JLabel("<html>"+
				description.trim().replaceAll("\\s{2,}", "<br>\n"));
		desc.setFont(desc.getFont().deriveFont(10.0f));
		desc.setVerticalAlignment(SwingConstants.TOP);
		desc.setForeground(SystemColor.infoText);				
		
		c.gridx = 0; 	c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		add(new JLabel(label), c);
		
		c.gridx = 1;	c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(field, c);
		
		c.gridx = 2;	c.weightx = 0.2;	//c.weighty = 0.2;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		add(desc, c);	
		
		fields.put(name, field);		
		c.gridy ++;
	}
		
}
