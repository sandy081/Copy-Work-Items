/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.link.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.ibm.team.links.common.IItemReference;
import com.ibm.team.links.common.IReference;
import com.ibm.team.links.common.registry.IEndPointDescriptor;
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;
import com.ibm.team.rtc.extensions.workitem.copy.internal.ItemResolver.ICallback;
import com.ibm.team.workitem.common.model.IAttachment;
import com.ibm.team.workitem.common.model.IWorkItemReferences;
import com.ibm.team.workitem.common.model.WorkItemEndPoints;

@SuppressWarnings("restriction")
public class AttachmentProcessor implements ILinkProcessor {

	@Override
	public void prepareTargetLink(final IWorkItemReferences targetReferences, IEndPointDescriptor endPoint, IReference sourceValue, final EvaluationContext context, final IProgressMonitor monitor) throws TeamRepositoryException {
		context.sourceContext.itemResolver.resolve(((IItemReference)sourceValue).getReferencedItem(), monitor).success(new ICallback<IItem>() {
			@Override
			public void with(IItem result, IProgressMonitor monitor) throws TeamRepositoryException {
				copyAttachments(targetReferences, (IAttachment)result, context, monitor);
			}
		});
	}

	private void copyAttachments(IWorkItemReferences targetReferences, IAttachment sourceValue, EvaluationContext context, IProgressMonitor monitor) throws TeamRepositoryException {
		File file= new File(sourceValue.getName());
		OutputStream stream;
		try {
			stream= new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new TeamRepositoryException(e);
		}
		context.sourceContext.contentManager.retrieveContent(sourceValue.getContent(), stream, SubMonitor.convert(monitor, 1));
		targetReferences.add(WorkItemEndPoints.ATTACHMENT, com.ibm.team.workitem.rcp.ui.internal.util.Utils.createReference(file));
	}
}
