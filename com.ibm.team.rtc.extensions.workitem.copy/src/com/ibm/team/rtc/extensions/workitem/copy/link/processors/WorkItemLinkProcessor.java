/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.link.processors;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.links.common.IItemReference;
import com.ibm.team.links.common.IReference;
import com.ibm.team.links.common.factory.IReferenceFactory;
import com.ibm.team.links.common.registry.IEndPointDescriptor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemReferences;

public class WorkItemLinkProcessor implements ILinkProcessor {

	@Override
	public void prepareTargetLink(final IWorkItemReferences targetReferences, IEndPointDescriptor endPoint, IReference sourceValue, final EvaluationContext context, final IProgressMonitor monitor) throws TeamRepositoryException {
		IWorkItemHandle sourceHandle= (IWorkItemHandle)((IItemReference)sourceValue).getReferencedItem();
		IWorkItemHandle targetHandle= context.sourceContext.getPair(sourceHandle);
		if (targetHandle == null) {
			return;
		}

		for (IReference reference : targetReferences.getReferences(endPoint)) {
			if (((IItemReference)reference).getReferencedItem().sameItemId(sourceHandle)) {
				return;
			}
		}

		targetReferences.add(endPoint, IReferenceFactory.INSTANCE.createReferenceToItem(targetHandle));
	}
}
