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

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext.Key;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

@SuppressWarnings("unchecked")
public abstract class ValueSetProcessor<T> extends AbstractValueProcessor<T> {

	@Override
	public void prepareTargetValue(IWorkItem target, IAttribute targetAttribute, IAttribute sourceAttribute, T sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		Key<Collection<String>> requiredAttributesKey= new Key<Collection<String>>("RequiredAttributes_" + target.getItemId().getUuidValue());
		Collection<String> requiredAttributes= context.get(requiredAttributesKey);
		if (requiredAttributes == null) {
			requiredAttributes= context.targetContext.workItemClient.findRequiredAttributes(target, null, monitor);
			context.set(requiredAttributesKey, requiredAttributes);
		}

		Key<T> key= new Key<T>(getClass().getName() + targetAttribute.getIdentifier());
		T targetValue= context.get(key);
		if (targetValue == null) {
			targetValue= (T)targetAttribute.getNullValue(context.targetContext.auditableClient, monitor);
			if (requiredAttributes.contains(targetAttribute.getIdentifier())) {
				targetValue= getNonNullValue(target, targetAttribute, context, monitor, targetValue);
			}
			context.set(key, targetValue);
		}

		if (targetValue != null) {
			setValue(target, targetAttribute, targetValue);
		}
	}

	protected T getNonNullValue(IWorkItem target, IAttribute targetAttribute, EvaluationContext context, IProgressMonitor monitor, T nullValue) throws TeamRepositoryException {
		if (targetAttribute.hasValueSet(context.targetContext.auditableClient, monitor)) {
			Object[] valueSet= targetAttribute.getValueSet(context.targetContext.auditableClient, target, monitor);
			for (Object value : valueSet) {
				if (!areSame((T)nullValue, (T)value)) {
					return (T)value;
				}
			}
		}
		return null;
	}

	protected abstract boolean areSame(T value1, T value2);

}
