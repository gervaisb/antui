/* StorePropertiesTask.java created on 9 déc. 2010 by Gervais */
/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.ant.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;


/**
 * @author Gervais
 * @version $REV$
 */
public class WriteProperties extends Task {
	
	// Optionnals attributes
	private String prefix = null;
	private boolean includePrefix = false;
	private String format = "classic";
	
	// Required attributes
	private File destination;
	
	public void setDestination(final File destination) {
		this.destination = destination;
	}
	
	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}
	
	public void setIncludePrefix(boolean includePrefix) {
		this.includePrefix = includePrefix;
	}
	
	public void setFormat(String format) {
		if ( !("xml".equalsIgnoreCase(format) ||
			   "classic".equalsIgnoreCase(format)) ) {
			throw new BuildException("Invalid format value \""+format+
					"\". Only \"classic\" or \"xml\" are supported.");
		}
		this.format = format;
	}
	
	@Override @SuppressWarnings("rawtypes")
	public void execute() throws BuildException {		
		final Properties result = new Properties();		
		final Hashtable projectPropertiess = getProject().getProperties();
		for (final Object keyObj : getProject().getProperties().keySet()) {			
			final String key = keyObj.toString();
			if ( prefix==null || (includePrefix && key.startsWith(prefix)) ) {
				getProject().log("Writing property with key \""+key+"\".", Project.MSG_DEBUG);
				result.put(key, projectPropertiess.get(keyObj));
			} else if ( key.startsWith(prefix) ) {
				getProject().log("Removing prefix from key \""+key+"\" before writing.", Project.MSG_DEBUG);
				result.put(key.substring(prefix.length()), projectPropertiess.get(keyObj));
			}
		}
		
		OutputStream out = null;
		try {
			if ( (destination.exists() && !destination.delete()) || !destination.createNewFile() ) {
				throw new IOException("Destination file "+destination.getAbsolutePath()+
					" is not writable or cannot be created.");
			} else if ( "classic".equals(format) ) {
				result.store(out = new FileOutputStream(destination), "");
			} else if ( "xml".equals(format) ) {
				result.storeToXML(out = new FileOutputStream(destination), "");
			}
		} catch (IOException ioe) {
			getProject().log("Cannot store properties on "+destination+". : "+
					ioe.getLocalizedMessage()+".", ioe, Project.MSG_ERR);
		} finally {
			if ( out!=null ) { try { out.close(); } catch (IOException e) {
				getProject().log("Stream on "+destination+" was not succesfully closed.", Project.MSG_WARN);
			}}
		}
	} 
	
}
