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
