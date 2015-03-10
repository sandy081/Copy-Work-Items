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

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.workitem.common.internal.workflow.WorkflowInfo;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IState;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;

@SuppressWarnings("restriction")
public class StateProcessor extends AbstractValueProcessor<String> {

	@Override
	public void prepareTargetValue(IWorkItem target, IAttribute attribute, IAttribute sourceAttribute, String sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		IWorkflowInfo workflowInfo= context.targetContext.workItemClient.findWorkflowInfo(target, monitor);
		for (Identifier<IState> state : workflowInfo.getAllStateIds()) {
			String targetValue= state.getStringIdentifier();
			if (targetValue.equals(sourceValue)) {
				setValue(target, attribute, state.getStringIdentifier());
				return;
			}

			String workflowScope= workflowInfo.getIdentifier() + ".state.";
			if (targetValue.startsWith(workflowScope)) {
				targetValue= targetValue.substring(workflowScope.length());
			}

			if (sourceValue.startsWith(workflowScope)) {
				sourceValue= sourceValue.substring(workflowScope.length());
			}

			if (WorkflowInfo.stripOffPrefix(targetValue, 's').equals(WorkflowInfo.stripOffPrefix(sourceValue, 's'))) {
				setValue(target, attribute, state.getStringIdentifier());
				return;
			}
		}
	}
}
