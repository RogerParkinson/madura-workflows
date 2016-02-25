/**
 * 
 */
package nz.co.senanque.workflowconf;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.workflow.WorkflowManagerImpl;

/**
 * An extension of the usual WorkflowManagerImpl which adds the component annotation.
 * @author Roger Parkinson
 *
 */
@org.springframework.stereotype.Component("workflowManager")
public class WorkflowManagerConf extends WorkflowManagerImpl {
	
	@Autowired private transient ValidationEngine validationEngine;
    @Value("${nz.co.senanque.workflow.WorkflowManager.schema}")
	private Resource m_schema;
    @Value("${nz.co.senanque.workflow.WorkflowManager.processes}")
	private Resource m_processes;

	@PostConstruct
	public void init() {
		super.setValidationEngine(validationEngine);
		super.init();
	}
	public Resource getSchema() {
		return m_schema;
	}
	public Resource getProcesses() {
		return m_processes;
	}

}
