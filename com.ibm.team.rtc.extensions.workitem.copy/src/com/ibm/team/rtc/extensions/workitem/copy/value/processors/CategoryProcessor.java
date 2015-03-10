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
	public void prepareTargetValue(IWorkItem targetWorkItem, IAttribute targetAttribute, IAttribute sourceAttribute, ICategoryHandle sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
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
