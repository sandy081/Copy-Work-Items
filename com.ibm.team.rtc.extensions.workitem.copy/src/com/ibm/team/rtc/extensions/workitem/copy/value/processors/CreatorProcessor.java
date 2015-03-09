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

import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;

public class CreatorProcessor extends ContributorProcessor {

	@Override
	protected IContributorHandle getDefaultValue(IContributor source, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		return context.targetContext.auditableClient.getUser();
	}

}
