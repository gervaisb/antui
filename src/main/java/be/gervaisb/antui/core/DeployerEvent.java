/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.core;

/**
 * DTO for any event who are produced by a {@link Deployer}.
 * 
 * @author Gervais
 */
public class DeployerEvent {

	public final static int LEVEL_DEBUG		= 4;
	public final static int LEVEL_VERBOSE	= 3;		
	public final static int LEVEL_INFO		= 2;
	public final static int LEVEL_WARN		= 1;
	public final static int LEVEL_ERROR		= 0;
	
	private final int priority;
	private final Target target;
	private final String task;
	private final String message;
	private final Throwable exception;
	
	public DeployerEvent(Deployer source, int priority, final Target target,
			final String message, final String task, final Throwable exception) {
		this.priority = priority;
		this.target = target;
		this.task = task;
		this.message = message;
		this.exception = exception;
	}
	
	public int getPriority() {
		return priority;
	}

	public String getMessage() {
		return message;
	}

	public Target getTarget() {
		return target;
	}

	public Throwable getException() {
		return exception;
	}
	
	public String getTask() {
		return task;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this==obj ) {
			return true;
		}
		if ( !(obj instanceof DeployerEvent) ) {
			return true;
		}
		
		final DeployerEvent that = (DeployerEvent) obj;
		return	(priority==that.priority) &&
				(task!=null
						?task.equals(that.task)
						:that.task==null) &&
				(target!=null
						?target.equals(that.target)
						:that.target==null) &&
				(message!=null
						?message.equals(that.message)
						:that.message==null) &&
				(exception!=null
						?exception.equals(that.exception)
						:that.exception==null);
	}
	
	@Override
	public int hashCode() {
		return priority+
			(task!=null?task.hashCode():0) +
			(target!=null?target.hashCode():0) +
			(message!=null?message.hashCode():0) +
			(exception!=null?exception.hashCode():0) * 42 ;
	}
	
	
}
