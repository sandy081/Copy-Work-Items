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

import com.ibm.team.links.common.IReference;
import com.ibm.team.links.common.factory.IReferenceFactory;
import com.ibm.team.links.common.registry.IEndPointDescriptor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.workitem.common.model.IWorkItemReferences;

public class URILinkProcessor implements ILinkProcessor {

	@Override
	public void prepareTargetLink(IWorkItemReferences targetReferences, IEndPointDescriptor endPoint, IReference sourceValue, EvaluationContext context, IProgressMonitor progressMonitor) throws TeamRepositoryException {
		targetReferences.add(endPoint, IReferenceFactory.INSTANCE.createReferenceFromURI(sourceValue.createURI()));
	}

}
