/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.value.processors;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.workitem.common.internal.workflow.WorkflowInfo;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IState;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;

@SuppressWarnings("restriction")
public class StateProcessor extends AbstractValueProcessor<String> {

	@Override
	public void prepareTargetValue(IWorkItem target, IAttribute attribute, IAttribute sourceAttribute, String sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		IWorkflowInfo workflowInfo= context.targetContext.workItemClient.findWorkflowInfo(target, monitor);
		for (Identifier<IState> state : workflowInfo.getAllStateIds()) {
			String targetValue= state.getStringIdentifier();
			if (targetValue.equals(sourceValue)) {
				setValue(target, attribute, state.getStringIdentifier());
				return;
			}

			String workflowScope= workflowInfo.getIdentifier() + ".state.";
			if (targetValue.startsWith(workflowScope)) {
				targetValue= targetValue.substring(workflowScope.length());
			}

			if (sourceValue.startsWith(workflowScope)) {
				sourceValue= sourceValue.substring(workflowScope.length());
			}

			if (WorkflowInfo.stripOffPrefix(targetValue, 's').equals(WorkflowInfo.stripOffPrefix(sourceValue, 's'))) {
				setValue(target, attribute, state.getStringIdentifier());
				return;
			}
		}
	}
}
