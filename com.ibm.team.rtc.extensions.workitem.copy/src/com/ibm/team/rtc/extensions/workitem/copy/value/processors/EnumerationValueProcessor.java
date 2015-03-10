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
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext.Key;
import com.ibm.team.rtc.extensions.workitem.copy.internal.RepositoryContext;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IEnumeration;
import com.ibm.team.workitem.common.model.ILiteral;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.Identifier;

public class EnumerationValueProcessor extends AbstractValueProcessor<Identifier<?>> {

	@Override
	public void prepareTargetValue(IWorkItem targetWorkItem, IAttribute targetAttribute, IAttribute sourceAttribute, Identifier<?> sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		IEnumeration<ILiteral> sourceEnumeration= getEnumeration(sourceAttribute, context.sourceContext, context, monitor);
		IEnumeration<ILiteral> targetEnumeration= getEnumeration(targetAttribute, context.targetContext, context, monitor);
		ILiteral targetLiteral= getMappedValue(targetEnumeration, sourceEnumeration, sourceValue);
		setValue(targetWorkItem, targetAttribute, targetLiteral.getIdentifier2());
	}

	private ILiteral getMappedValue(IEnumeration<ILiteral> targetEnumeration, IEnumeration<ILiteral> sourceEnumeration, Identifier<?> sourceValue) {
		ILiteral targetValue= getLiteralById(targetEnumeration, sourceValue);
		if (targetValue == null) {
			ILiteral sourceLiteral= getLiteralById(sourceEnumeration, sourceValue);
			if (sourceLiteral != null) {
				targetValue= getLiteralByName(targetEnumeration, sourceLiteral.getName());
			}
		}

		if (targetValue == null) {
			targetValue= targetEnumeration.findNullEnumerationLiteral();
		}

		return targetValue;
	}

	private ILiteral getLiteralById(IEnumeration<ILiteral> enumeration, Identifier<?> value) {
		for (ILiteral literal : enumeration.getEnumerationLiterals()) {
			if (literal.getIdentifier2().getStringIdentifier().equals(value.getStringIdentifier())) {
				return literal;
			}
		}
		return null;
	}

	private ILiteral getLiteralByName(IEnumeration<ILiteral> enumeration, String value) {
		for (ILiteral literal : enumeration.getEnumerationLiterals()) {
			if (literal.getName().equals(value)) {
				return literal;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private IEnumeration<ILiteral> getEnumeration(IAttribute attribute, RepositoryContext context, EvaluationContext evaluationContext, IProgressMonitor monitor) throws TeamRepositoryException {
		Key<IEnumeration<ILiteral>> key= new Key<IEnumeration<ILiteral>>(context.teamRepository.getId().getUuidValue() + context.projectArea.getItemId().getUuidValue() + attribute.getIdentifier());
		IEnumeration<ILiteral> enumeration= evaluationContext.get(key);
		if (enumeration == null) {
			enumeration= (IEnumeration<ILiteral>)context.workItemClient.resolveEnumeration(attribute, monitor);
			evaluationContext.set(key, enumeration);
		}
		return enumeration;
	}

}
