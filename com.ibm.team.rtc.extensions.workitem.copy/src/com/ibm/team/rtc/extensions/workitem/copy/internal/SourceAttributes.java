/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IAttributeHandle;
import com.ibm.team.workitem.common.model.IWorkItem;

public class SourceAttributes {

	private static final List<String> fgIgnoredAttributes= Arrays.asList(IItem.CONTEXT_ID_PROPERTY, IWorkItem.ID_PROPERTY, IWorkItem.PROJECT_AREA_PROPERTY);

	private final EvaluationContext fEvaluationContext;
	private final RepositoryContext fContext;

	public SourceAttributes(EvaluationContext evaluationContext) {
		fEvaluationContext= evaluationContext;
		fContext= evaluationContext.sourceContext;
	}

	public Collection<IAttribute> get(IProgressMonitor monitor) throws TeamRepositoryException {
		ArrayList<IAttribute> result= new ArrayList<IAttribute>();
		List<IAttributeHandle> builtInAttributes= fContext.workItemClient.findBuiltInAttributes(fContext.projectArea, monitor);
		for (IAttribute attribute : fContext.auditableClient.resolveAuditables(builtInAttributes, IAttribute.FULL_PROFILE, monitor)) {
			if (attribute.isInternal()) {
				continue;
			}

			if (fgIgnoredAttributes.contains(attribute.getIdentifier())) {
				continue;
			}

			result.add(attribute);
		}
		result.add(fContext.workItemClient.findAttribute(fContext.projectArea, "com.ibm.team.apt.attribute.complexity", monitor));
		if (fEvaluationContext.configuration.copyRankingValue) {
			result.add(fContext.workItemClient.findAttribute(fContext.projectArea, "com.ibm.team.apt.attribute.planitem.newRanking._pm7NmRYUEd6L1tNIGdz5qQ", monitor));
		}
		return result;
	}

}
