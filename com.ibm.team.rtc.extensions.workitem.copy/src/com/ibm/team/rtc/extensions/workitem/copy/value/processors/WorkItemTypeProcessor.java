/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.value.processors;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext.Key;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemType;

public class WorkItemTypeProcessor extends DefaultValueProcessor<String> {

	@Override
	public void prepareTargetValue(IWorkItem target, IAttribute targetAttribute, IAttribute sourceAttribute, String sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		setValue(target, targetAttribute, getMapping(target, targetAttribute, sourceValue, context, monitor));
	}

	public String getMapping(IWorkItem target, IAttribute targetAttribute, String sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		Key<List<IWorkItemType>> key= new Key<List<IWorkItemType>>(context.targetContext.teamRepository.getId().getUuidValue() + context.targetContext.projectArea.getItemId().getUuidValue() + targetAttribute.getIdentifier());
		List<IWorkItemType> workItemTypes= context.get(key);
		if (workItemTypes == null) {
			workItemTypes= context.targetContext.workItemClient.findWorkItemTypes(context.targetContext.projectArea, monitor);
		}

		for (IWorkItemType workItemType : workItemTypes) {
			if (workItemType.getIdentifier().equals(sourceValue)) {
				return workItemType.getIdentifier();
			}
		}

		if ("com.ibm.team.apt.workItemType.epic".equals(sourceValue)) {
			// Epic is mapped to Plan Item in RTC legacy PA
			return "rtc.planItem.v2";
		}

		return null;
	}

}
