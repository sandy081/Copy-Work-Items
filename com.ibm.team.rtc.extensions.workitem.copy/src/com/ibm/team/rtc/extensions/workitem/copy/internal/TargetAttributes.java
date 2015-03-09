/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IAttribute;

public class TargetAttributes {

	private final RepositoryContext fContext;

	private Map<String, IAttribute> fAttributes;

	public TargetAttributes(EvaluationContext evaluationContext) {
		fContext= evaluationContext.targetContext;
	}

	public IAttribute findAttribute(String id, IProgressMonitor monitor) throws TeamRepositoryException {
		return getAttributes(monitor).get(id);
	}

	private Map<String, IAttribute> getAttributes(IProgressMonitor monitor) throws TeamRepositoryException {
		if (fAttributes == null) {
			fAttributes= new HashMap<String, IAttribute>();
			for (IAttribute attribute : fContext.workItemClient.findAttributes(fContext.projectArea, monitor)) {
				fAttributes.put(attribute.getIdentifier(), attribute);
			}
		}
		return fAttributes;
	}

}
