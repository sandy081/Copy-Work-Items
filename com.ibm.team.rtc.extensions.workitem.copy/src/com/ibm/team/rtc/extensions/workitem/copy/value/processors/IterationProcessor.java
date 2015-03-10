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

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.apt.internal.common.Iterations;
import com.ibm.team.apt.internal.common.Iterations.BacklogPair;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext.Key;
import com.ibm.team.rtc.extensions.workitem.copy.internal.ItemResolver.ICallback;
import com.ibm.team.rtc.extensions.workitem.copy.internal.RepositoryContext;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

@SuppressWarnings("restriction")
public class IterationProcessor extends ItemHandleValueSetProcessor<IIterationHandle> {

	@Override
	public void prepareTargetValue(final IWorkItem target, final IAttribute targetAttribute, IAttribute sourceAttribute, IIterationHandle sourceValue, final EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		if (sourceValue != null) {
			Map<String, BacklogPair> sourceBacklogs= getBacklogInformation(context.sourceContext, monitor);
			for (BacklogPair pair : sourceBacklogs.values()) {
				if (pair.iteration.sameItemId(sourceValue)) {
					setTargetBacklog(target, targetAttribute, sourceValue, context, monitor);
					return;
				}
			}
		}
		super.prepareTargetValue(target, targetAttribute, sourceAttribute, sourceValue, context, monitor);
	}

	private void setTargetBacklog(final IWorkItem target, final IAttribute targetAttribute, final IIterationHandle sourceValue, final EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		final Key<IIterationHandle> key= new Key<IIterationHandle>(IterationProcessor.class.getName() + sourceValue.getItemId().getUuidValue());
		IIterationHandle targetValue= context.get(key);
		if (targetValue != null) {
			setValue(target, targetAttribute, targetValue);
			return;
		}

		final Map<String, BacklogPair> targetBacklogs= getBacklogInformation(context.targetContext, monitor);
		context.targetContext.itemResolver.resolve(context.targetContext.projectArea, monitor).success(new ICallback<IItem>() {
			public void with(IItem result, IProgressMonitor monitor) throws TeamRepositoryException {
				IIterationHandle targetValue= null;
				IProjectArea projectArea= (IProjectArea)result;
				if (projectArea.getProjectDevelopmentLine() != null) {
					for (BacklogPair pair : targetBacklogs.values()) {
						if (projectArea.getProjectDevelopmentLine().sameItemId(pair.timeLine)) {
							targetValue= pair.iteration;
							break;
						}
					}
				}
				if (targetValue == null) {
					targetValue= targetBacklogs.values().iterator().next().iteration;
				}
				context.set(key, targetValue);
				setValue(target, targetAttribute, targetValue);
			}
		});
	}

	private Map<String, BacklogPair> getBacklogInformation(RepositoryContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		return Iterations.getBacklogInformation(context.projectArea, context.iterationPlanClient, context.planItemStore, monitor);
	}
}
