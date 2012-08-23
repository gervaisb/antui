/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.core.impls.local;

import javax.swing.event.EventListenerList;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.DeployerEvent;
import be.gervaisb.antui.core.DeployerListener;

public class LocalDeployerEventDispatcher implements BuildListener {
	
	private final EventListenerList listeners = new EventListenerList();
	
	private final Deployer deployer;	
	
	public LocalDeployerEventDispatcher(final Deployer deployer) {
		this.deployer = deployer;
	}
	
	public void addListener(final DeployerListener lstnr) {
		listeners.add(DeployerListener.class, lstnr);
	}
	
	public void removeListener(final DeployerListener lstnr) {
		listeners.remove(DeployerListener.class, lstnr);
	}
	
	@Override
	public void buildFinished(BuildEvent buildEvent) {/**/}

	@Override
	public void buildStarted(BuildEvent buildEvent) {/**/}
	
	@Override
	public void taskStarted(BuildEvent buildEvent) {/**/}
	
	@Override
	public void targetStarted(BuildEvent buildEvent) {/**/}
	
	@Override
	public void targetFinished(BuildEvent buildEvent) {/**/}

	@Override
	public void messageLogged(BuildEvent buildEvent) {
		final DeployerListener[] lstnrs = listeners.getListeners(DeployerListener.class);
		DeployerEvent event = null;
		for (int i=lstnrs.length-1; i>=0; i--) {
			if ( event==null ) {
				event = convert(buildEvent);
			}
			lstnrs[i].messageLogged(event);
		}		
	}
	
	void fireTargetFinished(DeployerEvent event) {
		final DeployerListener[] lstnrs = listeners.getListeners(DeployerListener.class);
		for (int i=lstnrs.length-1; i>=0; i--) {
			lstnrs[i].targetFinished(event);
		}
	}	

	
	void fireTargetStarted(DeployerEvent event) {
		final DeployerListener[] lstnrs = listeners.getListeners(DeployerListener.class);
		for (int i=lstnrs.length-1; i>=0; i--) {
			lstnrs[i].targetStarted(event);
		}
	}

	@Override
	public void taskFinished(BuildEvent buildEvent) {
		if ( buildEvent.getException()!=null ) {
			final DeployerListener[] lstnrs = listeners.getListeners(DeployerListener.class);
			DeployerEvent event = null;
			for (int i=lstnrs.length-1; i>=0; i--) {
				if ( event==null ) {
					event = convert(buildEvent);
				}
				lstnrs[i].taskFailed(event);
			}
		}	
	}	
	
	private DeployerEvent convert(final BuildEvent event) {
		final String targetName = event.getTarget()!=null
			?event.getTarget().getName():null;
		final String taskName = event.getTask()!=null
			?event.getTask().getTaskName():null;	
			
		return new DeployerEvent(deployer, 
				event.getPriority(), deployer.findTarget(targetName), 
				event.getMessage(), taskName,
				event.getException());
	}	

}
