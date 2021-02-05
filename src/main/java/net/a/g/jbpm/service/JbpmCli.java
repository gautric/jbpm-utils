package net.a.g.jbpm.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.jbpm.kie.services.impl.CustomIdKModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.query.QueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JbpmCli implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(JbpmCli.class);

	@Autowired
	private DeploymentService ds;

	@Autowired
	private RuntimeDataService rds;

	@Autowired
	private ProcessService ps;

	@Autowired
	private UserTaskService uts;
	
	@Value( "${jbpm.utils.action}" )
	private String action;

	@Override
	public void run(String... args) throws Exception {

		DeploymentUnit unit = new CustomIdKModuleDeploymentUnit("mycontainerId", "net.a.g.jbpm", "bpmn-pattern-process",
				"3.0.0");

		ds.deploy(unit);
		

		if (action.compareTo("generation")==0) {
			Map<String, Object> params = new HashMap<String, Object>();

			params.put("booleanIn", true);
			params.put("integerIn", 42);
			params.put("stringIn", UUID.randomUUID().toString());

			for (int i = 0; i < 1000; i++) {
				Long piid = ps.startProcess(unit.getIdentifier(), "SignalTestProcess", params);
				System.out.println("  > " + piid);

			}
		} else if (action.compareTo("abort")==0){

			Collection<ProcessInstanceDesc> listProcessInstance = rds
					.getProcessInstancesByDeploymentId(unit.getIdentifier(), Arrays.asList(1), new QueryContext(0, 1000));


			for (Iterator iterator = listProcessInstance.iterator(); iterator.hasNext();) {
				ProcessInstanceDesc processInstanceDesc = (ProcessInstanceDesc) iterator.next();
				ps.abortProcessInstance(unit.getIdentifier(), processInstanceDesc.getId());

				System.out.println("Abort  > " + processInstanceDesc.getId());
			}

		}
	}
}