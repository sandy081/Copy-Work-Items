/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.value.processors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.workitem.common.internal.util.SeparatedStringList;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

@SuppressWarnings("restriction")
public class TagsProcessor extends AbstractValueProcessor<List<String>> {

	@Override
	public void prepareTargetValue(IWorkItem target, IAttribute targetAttribute, List<String> sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		List<String> targetValue= new ArrayList<String>(sourceValue);
		if (context.configuration.tags != null) {
			SeparatedStringList list= new SeparatedStringList(context.configuration.tags);
			targetValue.addAll(list);
		}
		setValue(target, targetAttribute, targetValue);
	}

}
