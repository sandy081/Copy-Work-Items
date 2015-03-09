/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.value.processors;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.apt.internal.common.Iterations;
import com.ibm.team.apt.internal.common.Iterations.BacklogPair;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext.Key;
import com.ibm.team.rtc.extensions.workitem.copy.internal.ItemResolver.ICallback;
import com.ibm.team.rtc.extensions.workitem.copy.internal.RepositoryContext;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

@SuppressWarnings("restriction")
public class IterationProcessor extends AbstractValueProcessor<IIterationHandle> {

	@Override
	public void prepareTargetValue(final IWorkItem target, final IAttribute targetAttribute, IIterationHandle sourceValue, final EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		if (sourceValue != null) {
			Map<String, BacklogPair> sourceBacklogs= getBacklogInformation(context.sourceContext, monitor);
			for (BacklogPair pair : sourceBacklogs.values()) {
				if (pair.iteration.sameItemId(sourceValue)) {
					setTargetBacklog(target, targetAttribute, sourceValue, context, monitor);
					return;
				}
			}
		}
	}

	private void setTargetBacklog(final IWorkItem target, final IAttribute targetAttribute, final IIterationHandle sourceValue, final EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		final Key<IIterationHandle> key= new Key<IIterationHandle>(IterationProcessor.class.getName() + sourceValue.getItemId().getUuidValue());
		IIterationHandle targetValue= context.get(key);
		if (targetValue != null) {
			setValue(target, targetAttribute, targetValue);
			return;
		}

		final Map<String, BacklogPair> targetBacklogs= getBacklogInformation(context.targetContext, monitor);
		context.targetContext.itemResolver.resolve(context.targetContext.projectArea).success(new ICallback<IItem>() {
			public void with(IItem result) throws TeamRepositoryException {
				IIterationHandle targetValue= null;
				IProjectArea projectArea= (IProjectArea)result;
				if (projectArea.getProjectDevelopmentLine() != null) {
					for (BacklogPair pair : targetBacklogs.values()) {
						if (projectArea.getProjectDevelopmentLine().sameItemId(pair.timeLine)) {
							targetValue= pair.iteration;
							break;
						}
					}
				}
				if (targetValue == null) {
					targetValue= targetBacklogs.values().iterator().next().iteration;
				}
				context.set(key, targetValue);
				setValue(target, targetAttribute, targetValue);
			}
		});
	}

	private Map<String, BacklogPair> getBacklogInformation(RepositoryContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		return Iterations.getBacklogInformation(context.projectArea, context.iterationPlanClient, context.planItemStore, monitor);
	}
}
