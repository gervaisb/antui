/* 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-
 * NoDerivs 3.0 Unported License. To view a copy of this license, visit http://
 * creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to Creative 
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, 
 * USA.*/
package be.gervaisb.antui.core.impls.local;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.helper.ProjectHelper2;

import be.gervaisb.ogam.commons.logging.Logger;
import be.gervaisb.ogam.commons.logging.LoggerFactory;
import be.gervaisb.ogam.commons.ui.components.OgErrorPane;
import be.gervaisb.antui.core.Deployer;
import be.gervaisb.antui.core.DeployerEvent;
import be.gervaisb.antui.core.DeployerListener;
import be.gervaisb.antui.core.Target;
import be.gervaisb.antui.core.TargetExecutor;
import be.gervaisb.antui.core.Utils;

public class LocalDeployer implements Deployer {
	
	protected final static Logger LOG = LoggerFactory.getLogger(Deployer.class);
	
	protected final LinkedList<TargetExecutor> queue = new LinkedList<TargetExecutor>();
	
	protected final LocalDeployerEventDispatcher eventsDispatcher; 
	protected final Project project;
	
	private final List<Target> targets;
	
	protected boolean working = false;
	
	public LocalDeployer(String projectName) {
		final URL tasksUrl = Utils.getTasksFile(projectName);
		if ( tasksUrl==null ) {
			final String message = "No tasks file found for project ["+projectName+
				"]. Please check if your project jar is in the classpath. \n"+
				"Current class path : "+System.getProperty("java.class.path");
			LOG.error(message);
			new OgErrorPane(new Error(message), message)
				.createDialog(null).setVisible(true);
		}
		
		project = new Project();
		project.setBaseDir(new File("."));

		LOG.info("Preparing project [{0}] from [{1}]. [Basedir={2}]", 
				projectName, tasksUrl, project.getBaseDir().getAbsolutePath());
		project.init();
		prepare(project, tasksUrl);
		
		ProjectHelper helper = new ProjectHelper2();
		project.addReference("ant.projectHelper", helper);
		helper.parse(project, tasksUrl);
				
		targets = new ArrayList<Target>();
		for (Object targetKey : project.getTargets().keySet()) {
			org.apache.tools.ant.Target antTarget = 
				(org.apache.tools.ant.Target) project.getTargets().get(targetKey);
			if ( antTarget.getName().length()>1 && antTarget.getName().charAt(0)=='+' ) {
				targets.add(new LocalTarget(antTarget));
			}			
		}
		
		eventsDispatcher = new LocalDeployerEventDispatcher(this);
		project.addBuildListener(eventsDispatcher);
	}
	
	@Override
	public void execute(final Target target, final Properties properties, boolean background) {		
		if ( background ) {
			enqueueTask(target, properties);
		} else {
			LOG.info("Starting target [{0}] with properties [{1}]", 
					target.getName(), properties);
			working = true;
			LocalDeployer.this.eventsDispatcher.fireTargetStarted(
					new DeployerEvent(LocalDeployer.this, DeployerEvent.LEVEL_INFO, 
							target, null, null, null));
			
			new TargetExecutor(target, properties).execute(project);
			working = !working;
			
			LocalDeployer.this.eventsDispatcher.fireTargetFinished(
					new DeployerEvent(LocalDeployer.this, DeployerEvent.LEVEL_INFO, 
							target, null, null, null));
			LOG.info("Target [{0}] terminated.", target.getName());
		}		
	}
	
	@Override
	public void addListener(final DeployerListener listener) {
		eventsDispatcher.addListener(listener);
	}
	
	@Override
	public void removeListener(final DeployerListener listener) {
		eventsDispatcher.removeListener(listener);
		
	}		
	
	@Override
	public Target findTarget(String id) {
		Target target = null;
		for (int i=0; target==null && i<targets.size(); i++) {
			target = targets.get(i).getId().equals(id)
					?targets.get(i):null;
		}
		return target;
	}

	@Override
	public void exit() {
		System.exit(0);
	}

	@Override
	public String getAppName() {
		return project.getName().replace('_', ' ');
	}

	@Override
	public Target[] getPendingTargets() {
		return queue.toArray(new Target[queue.size()]);
	}

	@Override
	public Target[] getTargets() {
		return targets.toArray(new Target[targets.size()]);
	}

	@Override
	public boolean isWorking() {
		return working;
	}
	
	private final void prepare(final Project project, final URL tasks) {
		//Document document = new SAXBuilder().build(tasks);
		//XPath.selectNodes(document, "/project/");
	}
	
	
	// ~ Private methods -------------------------------------------------------
	
	private void enqueueTask(final Target target, final Properties properties) {		
		synchronized (queue) {
			LOG.info("Enqueing target [{0}] with properties [{1}] in target pool.", 
					target.getName(), properties);
			queue.add(new TargetExecutor(target, properties));
			new Thread(new Runnable() {				
				@Override
				public void run() {
					working = true;
					while ( !queue.isEmpty() ) {
						LOG.info("Starting queued target [{0}] with properties [{1}].", 
								target.getName(), properties);
						LocalDeployer.this.eventsDispatcher.fireTargetStarted(
								new DeployerEvent(LocalDeployer.this, DeployerEvent.LEVEL_INFO, 
										target, null, null, null));
						
						final TargetExecutor executor = queue.poll();
						executor.execute(project);
						
						LocalDeployer.this.eventsDispatcher.fireTargetFinished(
								new DeployerEvent(LocalDeployer.this, DeployerEvent.LEVEL_INFO, 
										target, null, null, null));
					}				
					working = !working;
				}
			}).start();
		}		
	}

}
