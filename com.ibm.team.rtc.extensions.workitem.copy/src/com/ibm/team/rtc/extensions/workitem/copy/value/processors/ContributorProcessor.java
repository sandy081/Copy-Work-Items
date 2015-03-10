/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sandeep Somavarapu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
