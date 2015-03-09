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

import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.ItemResolver.ICallback;
import com.ibm.team.workitem.common.internal.model.DefaultModel;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

@SuppressWarnings("restriction")
public class SubscribersProcessor implements IValueProcessor<List<IContributorHandle>> {

	private final ContributorProcessor fContributorProcessor= new ContributorProcessor();

	public void prepareTargetValue(final IWorkItem target, final IAttribute targetAttribute, IAttribute sourceAttribute, List<IContributorHandle> sourceValue, final EvaluationContext context, final IProgressMonitor monitor) throws TeamRepositoryException {
		if (sourceValue == null || sourceValue.isEmpty()) {
			return;
		}

		for (IContributorHandle source : sourceValue) {
			context.sourceContext.itemResolver.resolve(source).success(new ICallback<IItem>() {
				public void with(IItem result) throws TeamRepositoryException {
					IContributorHandle targetValue= fContributorProcessor.getMapping((IContributor)result, context, monitor);
					if (!DefaultModel.NULL_CONTRIBUTOR_ITEM_ID.equals(targetValue.getItemId())) {
						target.getSubscriptions().add(targetValue);
					}
				}
			});
		}
	}
}
