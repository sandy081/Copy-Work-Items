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
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.ItemNotFoundException;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext.Key;
import com.ibm.team.rtc.extensions.workitem.copy.internal.ItemResolver.ICallback;
import com.ibm.team.workitem.common.internal.model.DefaultModel;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

@SuppressWarnings("restriction")
public class ContributorProcessor extends AbstractValueProcessor<IContributorHandle> {

	public void prepareTargetValue(final IWorkItem target, final IAttribute targetAttribute, IAttribute sourceAttribute, IContributorHandle sourceValue, final EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		if (sourceValue == null) {
			return;
		}

		context.sourceContext.itemResolver.resolve(sourceValue, monitor).success(new ICallback<IItem>() {
			public void with(IItem result, IProgressMonitor monitor) throws TeamRepositoryException {
				IContributorHandle targetValue= getMapping((IContributor)result, context, monitor);
				setValue(target, targetAttribute, targetValue);
			}
		});
	}

	public IContributorHandle getMapping(IContributor sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		Key<IContributorHandle> key= new Key<IContributorHandle>(sourceValue.getItemId().getUuidValue());
		IContributorHandle target= context.get(key);
		if (target == null) {
			try {
				target= context.targetContext.contributorManager.fetchContributorByUserId(sourceValue.getUserId(), monitor);
			} catch (ItemNotFoundException e) {
				// ignore
			}
			if (target == null) {
				target= getDefaultValue(sourceValue, context, monitor);
			}
			context.set(key, target);
		}
		return target;
	}

	protected IContributorHandle getDefaultValue(IContributor source, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		return (IContributorHandle)IContributor.ITEM_TYPE.createItemHandle(DefaultModel.NULL_CONTRIBUTOR_ITEM_ID, null);
	}
}
