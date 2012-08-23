/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.server;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import be.gervaisb.ogam.commons.logging.Logger;
import be.gervaisb.ogam.commons.logging.LoggerFactory;

public class Command {
	
	private final static Logger LOG = LoggerFactory.getLogger(Command.class);
	
	public enum Verbs {
		CLOSE,
		GET,
		EXECUTE
	}
	
	public enum Qualifiers {
		AppName,
		IsWorking,
		PendingTargets
	}
	
	public final static Command parse(final String line) throws ParseException {
		LOG.debug("Creating new command from ["+line+"].");
		final String[] parts = line.split(" ");
		if ( parts.length<1 ) {
			throw new ParseException(
					"Command will contains a verb: \"[VERB] [Qualifier] [arg1[,arg2[,...]]]\"", 0);
		}
		return new Command(parts[0], 
				parts.length>1?parts[1]:null, 
				parts.length>2?parts[2].split(","):null);
	}
	
	public final static Command New(final Verbs verb) {
		return New(verb, null, (Object[])null);
	}
	
	public final static Command New(final Verbs verb, final Object qualifier) {
		return New(verb, qualifier, (Object[])null);	
	}
	
	public final static Command New(final Verbs verb, final Object qualifier, final Map<Object, Object> args) {
		List<String> arguments = new ArrayList<String>(args.size());
		for (final Entry<Object, Object> arg : args.entrySet()) {
			if ( arg.getValue()!=null ) {
				arguments.add(String.valueOf(arg.getKey())+'='+String.valueOf(arg.getValue()));
			} else {
				arguments.add(String.valueOf(arg.getKey()));
			}
		}
		return New(verb, qualifier, (Object[])arguments.toArray(new String[arguments.size()]));	
	}
	
	public final static Command New(final Verbs verb, final Object qualifier,
			final Object... args) {
		final String[] strArgs = args!=null?new String[args.length]:null;
		for (int i=0; args!=null && i<args.length; i++) {
			strArgs[i] = String.valueOf(args[i]);
		}
		return new Command(verb.toString(), qualifier!=null?String.valueOf(qualifier):null, strArgs);
	}
	
	// ~ ------------------------------------------------------------------ ~ //
	
	private final Map<String, String> parameters;
	private final String qualifier;
	private final String verb;
	
	
	@SuppressWarnings("unchecked")
	private Command(final String verb, final String qualifier, final String[] args) {
		this.parameters = (Map<String, String>) (args==null
				?Collections.emptyMap()
				:new HashMap<String, String>(args.length));
		this.qualifier = qualifier;
		this.verb = verb;
		
		int index;
		for (int i=0; args!=null && i<args.length && (index=args[i].indexOf('='))!=-1; i++) {
			parameters.put(
					args[i].substring(0, index), 
					args[i].substring(index+1));
		}
	}
	
	public String getQualifier() {
		return qualifier;
	}
	
	public String getVerb() {
		return verb;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public String getParameter(final String name) {
		return parameters.get(name);
	}
	
	@Override
	public String toString() {
		final StringBuilder builder =  new StringBuilder(getVerb());
		if ( getQualifier()!=null ) {
			builder.append(' ').append(getQualifier());
		}
		if ( !getParameters().isEmpty() ) {
			builder.append(' ');
			for (final Entry<String, String> param : getParameters().entrySet()) {
				builder.append(param.getKey());
				if ( param.getValue()!=null ) {
					builder.append('=').append(param.getValue());
				}
				builder.append(',');
			}
			builder.replace(builder.length()-1, builder.length(), "");
		}
		return builder.toString();
	}
	
}
