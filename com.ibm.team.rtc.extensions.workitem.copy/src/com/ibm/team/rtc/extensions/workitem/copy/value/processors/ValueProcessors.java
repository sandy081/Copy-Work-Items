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

import java.util.HashMap;

import com.ibm.team.workitem.common.model.AttributeTypes;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IDeliverableHandle;
import com.ibm.team.workitem.common.model.IWorkItem;

public class ValueProcessors {

	private static HashMap<String, IValueProcessor<?>> fProcessorsById= new HashMap<String, IValueProcessor<?>>();
	private static HashMap<String, IValueProcessor<?>> fProcessors= new HashMap<String, IValueProcessor<?>>();

	private static final EnumerationValueProcessor ENUM_PROCESSOR= new EnumerationValueProcessor();
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
		fProcessors.put(AttributeTypes.DELIVERABLE, new ItemHandleValueSetProcessor<IDeliverableHandle>());

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

		if (AttributeTypes.isEnumerationAttributeType(attributeType)) {
			return (IValueProcessor<T>)ENUM_PROCESSOR;
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
