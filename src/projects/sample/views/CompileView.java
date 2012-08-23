package sample.views;

import java.awt.FlowLayout;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import be.gervaisb.antui.client.ui.swing.View;

public class CompileView extends View {
	
	private final JTextField property1;
	private final JComboBox propertyX;
	
	public CompileView() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		JPanel description = new JPanel(new FlowLayout(FlowLayout.LEADING));
		description.add(new JLabel(
				"<html>You can create good looking views." +
				"<br />This one require two properties who can comes from " +
				"more complex controls.."));
		
		JPanel input1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		input1.add(new JLabel("First property"));
		input1.add(property1 = new JTextField(35));
		
		JPanel input2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		input2.add(new JLabel("Second property"));
		input2.add(propertyX = new JComboBox(new String[]{"One", "Two", "Three"}));
		
		add(description);
		add(input1);
		add(input2);
	}

	@Override
	public Properties getProperties() {
		// Returning "null" may abort the process.
		if ( property1.getText().trim().isEmpty() || propertyX.getSelectedIndex()==1 )
			return null;
		
		Properties properties = new Properties();
		properties.put("Property1", property1.getText());
		properties.put("PropertyX", String.valueOf(propertyX.getSelectedItem()));
		return properties;
	}

}
