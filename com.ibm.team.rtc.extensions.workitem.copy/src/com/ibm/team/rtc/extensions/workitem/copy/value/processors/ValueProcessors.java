/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.value.processors;

import java.util.HashMap;

import com.ibm.team.workitem.common.model.AttributeTypes;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

public class ValueProcessors {

	private static HashMap<String, IValueProcessor<?>> fProcessorsById= new HashMap<String, IValueProcessor<?>>();
	private static HashMap<String, IValueProcessor<?>> fProcessors= new HashMap<String, IValueProcessor<?>>();

	private static final IValueProcessor<?> NULL_PROCESSOR= new VoidProcessor();
	private static final IValueProcessor<?> DEFAULT_PROCESSOR= new DefaultValueProcessor<Object>();

	static {
		fProcessorsById.put(IWorkItem.STATE_PROPERTY, new StateProcessor());
		fProcessorsById.put(IWorkItem.RESOLUTION_PROPERTY, new ResolutionProcessor());
		fProcessorsById.put(IWorkItem.CREATOR_PROPERTY, new CreatorProcessor());
		fProcessorsById.put(IWorkItem.TYPE_PROPERTY, new WorkItemTypeProcessor());

		fProcessors.put(AttributeTypes.CONTRIBUTOR, new ContributorProcessor());
		fProcessors.put(AttributeTypes.CATEGORY, new CategoryProcessor());
		fProcessors.put(AttributeTypes.TAGS, new TagsProcessor());
		fProcessors.put(AttributeTypes.SUBSCRIPTIONS, new SubscribersProcessor());
		fProcessors.put(AttributeTypes.COMMENTS, new CommentsProcessor());
		fProcessors.put(AttributeTypes.ITERATION, new IterationProcessor());
		fProcessors.put(AttributeTypes.DELIVERABLE, new DeliverableValueProcessor());

		for (String type : AttributeTypes.STRING_TYPES) {
			fProcessors.put(type, new StringValueProcessor());
		}

		for (String type : AttributeTypes.HTML_TYPES) {
			fProcessors.put(type, new HtmlValueProcessor());
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T> IValueProcessor<T> getProcessor(IAttribute attribute) {
		IValueProcessor<?> processor= fProcessorsById.get(attribute.getIdentifier());
		if (processor != null) {
			return (IValueProcessor<T>)processor;
		}

		return getProcessor(attribute.getAttributeType());
	}

	@SuppressWarnings("unchecked")
	public static <T> IValueProcessor<T> getProcessor(String attributeType) {
		IValueProcessor<?> processor= fProcessors.get(attributeType);
		if (processor != null) {
			return (IValueProcessor<T>)processor;
		}

		if (AttributeTypes.isItemAttributeType(attributeType) 
				|| AttributeTypes.APPROVAL_TYPE.equals(attributeType)
				|| AttributeTypes.APPROVAL_DESCRIPTORS.equals(attributeType)
				|| AttributeTypes.APPROVALS.equals(attributeType)
				|| AttributeTypes.APPROVAL_STATE.equals(attributeType)) {
			return (IValueProcessor<T>)NULL_PROCESSOR;
		}

		return (IValueProcessor<T>)DEFAULT_PROCESSOR;
	}

}
