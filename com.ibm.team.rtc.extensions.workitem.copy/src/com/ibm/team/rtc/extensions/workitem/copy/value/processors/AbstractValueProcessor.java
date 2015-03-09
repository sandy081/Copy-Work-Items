/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.value.processors;

import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

public abstract class AbstractValueProcessor<T> implements IValueProcessor<T> {

	protected void setValue(IWorkItem targetWorkItem, IAttribute targetAttribute, T value) {
		if (!targetWorkItem.hasAttribute(targetAttribute)) {
			targetWorkItem.addCustomAttribute(targetAttribute);
		}
		targetWorkItem.setValue(targetAttribute, value);
	}

}
