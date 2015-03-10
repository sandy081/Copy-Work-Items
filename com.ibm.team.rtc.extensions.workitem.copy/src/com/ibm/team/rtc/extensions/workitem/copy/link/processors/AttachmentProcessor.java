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
