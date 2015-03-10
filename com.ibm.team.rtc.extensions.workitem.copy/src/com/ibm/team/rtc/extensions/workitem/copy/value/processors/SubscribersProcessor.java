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
			context.sourceContext.itemResolver.resolve(source, monitor).success(new ICallback<IItem>() {
				public void with(IItem result, IProgressMonitor monitor) throws TeamRepositoryException {
					IContributorHandle targetValue= fContributorProcessor.getMapping((IContributor)result, context, monitor);
					if (!DefaultModel.NULL_CONTRIBUTOR_ITEM_ID.equals(targetValue.getItemId())) {
						target.getSubscriptions().add(targetValue);
					}
				}
			});
		}
	}
}
