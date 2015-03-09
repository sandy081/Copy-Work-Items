/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
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
	public void prepareTargetValue(IWorkItem target, IAttribute targetAttribute, T sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
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
