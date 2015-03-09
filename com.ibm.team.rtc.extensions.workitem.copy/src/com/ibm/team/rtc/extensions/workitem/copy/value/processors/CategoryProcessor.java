/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.value.processors;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext.Key;
import com.ibm.team.workitem.common.internal.util.CategoriesHelper;
import com.ibm.team.workitem.common.model.CategoryId;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.ICategory;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IWorkItem;

@SuppressWarnings("restriction")
public class CategoryProcessor extends AbstractValueProcessor<ICategoryHandle> {

	private static final Key<ICategoryHandle> TARGET_CATEGORY_KEY= new Key<ICategoryHandle>(CategoryProcessor.class.getName() + "_targetCategory");

	@Override
	public void prepareTargetValue(IWorkItem targetWorkItem, IAttribute targetAttribute, ICategoryHandle sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		ICategoryHandle targetValue= context.get(TARGET_CATEGORY_KEY);
		if (targetValue == null) {
			// Get the mapped value
			ICategory unassigned= context.targetContext.workItemClient.findUnassignedCategory(context.targetContext.projectArea, ICategory.SMALL_PROFILE, monitor);
			if (context.configuration.categoryPath != null && !context.configuration.categoryPath.isEmpty()) {
				CategoryId categoryId= CategoryId.createCategoryId(unassigned.getCategoryId().getInternalRepresentation() + context.configuration.categoryPath + CategoriesHelper.DELIMITER);
				targetValue= context.targetContext.workItemClient.findCategoryById2(context.targetContext.projectArea, categoryId, monitor);
			}

			// Get the first unassigned value
			if (targetValue == null) {
				for (ICategory category : context.targetContext.workItemClient.findCategories(context.targetContext.projectArea, ICategory.SMALL_PROFILE, monitor)) {
					if (!category.isUnassigned()) {
						targetValue= category;
						break;
					}
				}
			}

			// Fallback to unassigned value
			if (targetValue == null) {
				targetValue= unassigned;
			}

			context.set(TARGET_CATEGORY_KEY, targetValue);
		}
		setValue(targetWorkItem, targetAttribute, targetValue);
	}
}
