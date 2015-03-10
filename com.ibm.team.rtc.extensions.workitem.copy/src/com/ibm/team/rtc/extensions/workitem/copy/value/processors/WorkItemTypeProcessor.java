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

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext.Key;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemType;

public class WorkItemTypeProcessor extends DefaultValueProcessor<String> {

	@Override
	public void prepareTargetValue(IWorkItem target, IAttribute targetAttribute, IAttribute sourceAttribute, String sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		setValue(target, targetAttribute, getMapping(target, targetAttribute, sourceValue, context, monitor));
	}

	public String getMapping(IWorkItem target, IAttribute targetAttribute, String sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		Key<List<IWorkItemType>> key= new Key<List<IWorkItemType>>(context.targetContext.teamRepository.getId().getUuidValue() + context.targetContext.projectArea.getItemId().getUuidValue() + targetAttribute.getIdentifier());
		List<IWorkItemType> workItemTypes= context.get(key);
		if (workItemTypes == null) {
			workItemTypes= context.targetContext.workItemClient.findWorkItemTypes(context.targetContext.projectArea, monitor);
		}

		for (IWorkItemType workItemType : workItemTypes) {
			if (workItemType.getIdentifier().equals(sourceValue)) {
				return workItemType.getIdentifier();
			}
		}

		if ("com.ibm.team.apt.workItemType.epic".equals(sourceValue)) {
			// Epic is mapped to Plan Item in RTC legacy PA
			return "rtc.planItem.v2";
		}

		return null;
	}

}
