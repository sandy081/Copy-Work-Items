/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.value.processors;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.ItemResolver.ICallback;
import com.ibm.team.workitem.common.internal.model.Comment;
import com.ibm.team.workitem.common.internal.model.DefaultModel;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IComment;
import com.ibm.team.workitem.common.model.IWorkItem;

@SuppressWarnings("restriction")
public class CommentsProcessor extends AbstractReplacementValueProcessor<List<IComment>> {

	private final ContributorProcessor fContributorProcessor= new ContributorProcessor();

	@Override
	public void prepareTargetValue(final IWorkItem target, final IAttribute attribute, IAttribute sourceAttribute, List<IComment> sourceValue, final EvaluationContext context, final IProgressMonitor monitor) throws TeamRepositoryException {
		for (final IComment sourceComment : sourceValue) {
			context.sourceContext.itemResolver.resolve(sourceComment.getCreator()).success(new ICallback<IItem>() {
				@Override
				public void with(IItem result) throws TeamRepositoryException {
					IContributorHandle creator= fContributorProcessor.getMapping((IContributor)result, context, monitor);
					if (DefaultModel.NULL_CONTRIBUTOR_ITEM_ID.equals(creator.getItemId())) {
						creator= context.targetContext.auditableClient.getUser();
					}
					XMLString content= XMLString.createFromXMLText(replace(sourceComment.getHTMLContent().getXMLText(), context));
					IComment comment= target.getComments().createComment(creator, content);
					((Comment)comment).setCreationDate(sourceComment.getCreationDate());
					target.getComments().append(comment);
				}
			});
		}
	}
}
