/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.links.common.IReference;
import com.ibm.team.links.common.registry.IEndPointDescriptor;
import com.ibm.team.repository.common.Location;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.WorkItemsCopyPlugIn;
import com.ibm.team.rtc.extensions.workitem.copy.internal.WorkItemsResolver.IWorkItems;
import com.ibm.team.rtc.extensions.workitem.copy.link.processors.ILinkProcessor;
import com.ibm.team.rtc.extensions.workitem.copy.link.processors.LinkProcessors;
import com.ibm.team.rtc.extensions.workitem.copy.value.processors.IValueProcessor;
import com.ibm.team.rtc.extensions.workitem.copy.value.processors.ValueProcessors;
import com.ibm.team.rtc.extensions.workitem.copy.value.processors.WorkItemTypeProcessor;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IComment;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemReferences;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.WorkItemEndPoints;
import com.ibm.team.workitem.common.text.WorkItemTextUtilities;

public class CopyWorkItemsJob {

	private final EvaluationContext fContext;

	public CopyWorkItemsJob(EvaluationContext context) {
		fContext= context;
	}

	public void run(IProgressMonitor monitor) {
		SubMonitor progress= SubMonitor.convert(monitor);
		List<IWorkItemHandle> allSources= new ArrayList<IWorkItemHandle>();
		List<IWorkItemHandle> allTargets= new ArrayList<IWorkItemHandle>();
		try {
			WorkItemsResolver workItemsResolver= new WorkItemsResolver(fContext);
			IWorkItems workItems= workItemsResolver.resolve(progress.newChild(10));
			int totalResultsSize= workItemsResolver.getTotalResultsSize();

			progress.beginTask("Copy Work Items", (totalResultsSize * 5) + 1);

			TargetAttributes targetAttributes= new TargetAttributes(fContext);
			List<WorkItemWorkingCopy> targets= createTargets(workItems, targetAttributes, allSources, allTargets, totalResultsSize, progress);
			
			prepareAttributesForTargets(targets, targetAttributes, progress);
			copyTargets(targets, "Copying Work Items", progress);

			if (fContext.configuration.copyLinks || fContext.configuration.copyAttachments) {
				copyLinks(targets, progress);
			}

			fContext.status= Status.OK_STATUS;
		} catch (TeamRepositoryException e) {
			fContext.status= new Status(IStatus.ERROR, WorkItemsCopyPlugIn.ID, e.getMessage(), e);
		} finally {
			fContext.result= allTargets;
			for (IWorkItemHandle source : allSources) {
				fContext.sourceContext.workingCopyManager.disconnect(source);
			}
			fContext.sourceContext.workingCopyManager.dispose();
			for (IWorkItemHandle workItem : allTargets) {
				fContext.targetContext.workingCopyManager.disconnect(workItem);
			}
			fContext.targetContext.workingCopyManager.dispose();
		}
		progress.done();
	}

	private List<WorkItemWorkingCopy> createTargets(IWorkItems workItems, TargetAttributes targetAttributes, List<IWorkItemHandle> allSources, List<IWorkItemHandle> allTargets, int totalResultsSize, SubMonitor progress) throws TeamRepositoryException {
		List<WorkItemWorkingCopy> targets= new ArrayList<WorkItemWorkingCopy>();
		SubMonitor creationMonitor= progress.newChild(totalResultsSize);
		int counter= 1;
		while (workItems.hasNext()) {
			Collection<IWorkItem> batch= workItems.next();
			for (IWorkItem source : batch) {
				SubMonitor singleMonitor= creationMonitor.newChild(1);
				singleMonitor.setTaskName("Creating Work Item " + "(" + counter + " of " + totalResultsSize + ")");
				String targetType= new WorkItemTypeProcessor().getMapping(null, targetAttributes.findAttribute(IWorkItem.TYPE_PROPERTY, singleMonitor), source.getWorkItemType(), fContext, singleMonitor);
				if (targetType == null) {
					throw new TeamRepositoryException("Mapping not found for work item type: " + source.getWorkItemType());
				}
				WorkItemWorkingCopy target= newTarget(targetType, singleMonitor);
				
				fContext.sourceContext.addPair(source, target.getWorkItem());
				fContext.targetContext.addPair(target.getWorkItem(), source);
				
				targets.add(target);
				allSources.add(source);
				allTargets.add(target.getWorkItem());
				
				singleMonitor.done();
				counter++;
			}
		}
		creationMonitor.done();
		return targets;
	}

	private List<WorkItemWorkingCopy> prepareAttributesForTargets(List<WorkItemWorkingCopy> workingCopies, TargetAttributes targetAttributes, SubMonitor progress) throws TeamRepositoryException {
		Collection<IAttribute> sourceAttributes= new SourceAttributes(fContext).get(progress.newChild(1));
		BatchIterator<WorkItemWorkingCopy> iterator= new BatchIterator<WorkItemWorkingCopy>(workingCopies, WorkItemsResolver.BATCH_SIZE);
		int counter= 1;
		while (iterator.hasNext()) {
			Collection<WorkItemWorkingCopy> batch= iterator.next();
			SubMonitor preparingMonitor= progress.newChild(batch.size());
			
			for (WorkItemWorkingCopy target : batch) {
				SubMonitor singleMonitor= preparingMonitor.newChild(1);
				singleMonitor.setTaskName("Preparing copies " + "(" + counter + " of " + workingCopies.size() + ")");
				for (IAttribute sourceAttribute : sourceAttributes) {
					IValueProcessor<Object> processor= (IValueProcessor<Object>)ValueProcessors.getProcessor(sourceAttribute);
					IAttribute targetAttribute= targetAttributes.findAttribute(sourceAttribute.getIdentifier(), singleMonitor);
					IWorkItem source= fContext.targetContext.getPair(target.getWorkItem());
					if (source.hasAttribute(sourceAttribute)) {
						processor.prepareTargetValue(target.getWorkItem(), targetAttribute, sourceAttribute, source.getValue(sourceAttribute), fContext, singleMonitor);
					}
				}
				counter++;
				singleMonitor.done();
			}
			fContext.sourceContext.itemResolver.execute(preparingMonitor);
			fContext.targetContext.itemResolver.execute(preparingMonitor);
			preparingMonitor.done();
		}
		return workingCopies;
	}

	private void copyTargets(List<WorkItemWorkingCopy> workingCopies, String message, SubMonitor monitor) throws TeamRepositoryException {
		BatchIterator<WorkItemWorkingCopy> iterator= new BatchIterator<WorkItemWorkingCopy>(workingCopies, 25);
		int counter= 0;
		while (iterator.hasNext()) {
			Collection<WorkItemWorkingCopy> batch= iterator.next();
			for (WorkItemWorkingCopy target : batch) {
				XMLString copiedFromCommentText= XMLString.createFromXMLText("Copied from " + createTextLink((IWorkItem)fContext.targetContext.getPair(target.getWorkItem())));
				IComment comment= target.getWorkItem().getComments().createComment(fContext.targetContext.auditableClient.getUser(), copiedFromCommentText);
				target.getWorkItem().getComments().append(comment);
			}
			SubMonitor saveMonitor= monitor.newChild(batch.size());
			saveMonitor.setTaskName(message + " (" + (counter + batch.size()) + " of " + workingCopies.size() + ")");
			fContext.targetContext.workingCopyManager.save(batch.toArray(new WorkItemWorkingCopy[batch.size()]), saveMonitor);
			saveMonitor.done();
			counter+= 25;
		}
	}

	private void copyLinks(List<WorkItemWorkingCopy> targets, SubMonitor progress) throws TeamRepositoryException {
		if (fContext.configuration.copyLinks || fContext.configuration.copyAttachments) {
			int counter= 1;
			SubMonitor linksMonitor= progress.newChild(targets.size() * 2);
			for (WorkItemWorkingCopy target : targets) {
				SubMonitor singleMonitor= linksMonitor.newChild(1);
				String preparingMessage= "Copying links " + (fContext.configuration.copyAttachments ? "with attachments " : "");
				singleMonitor.setTaskName(preparingMessage + "(" + counter + " of " + targets.size() + ")");

				IWorkItemReferences sourceReferences= fContext.sourceContext.workingCopyManager.getWorkingCopy(fContext.targetContext.getPair(target.getWorkItem())).getReferences();
				IWorkItemReferences targetReferences= target.getReferences();
				for (IEndPointDescriptor endPoint : getEndPointsToCopy(sourceReferences)) {
					updateEndPoint(sourceReferences, targetReferences, endPoint, singleMonitor);
				}
				fContext.sourceContext.itemResolver.execute(singleMonitor);
				fContext.targetContext.itemResolver.execute(singleMonitor);

				if (!targetReferences.getChangedReferenceTypes().isEmpty()) {
					target.save(linksMonitor.newChild(1));
				}
				counter++;
			}
			linksMonitor.done();
		}
	}

	private WorkItemWorkingCopy newTarget(String type, IProgressMonitor monitor) throws TeamRepositoryException {
		IWorkItemType workitemType= fContext.targetContext.workItemClient.findWorkItemType(fContext.targetContext.projectArea, type, monitor);
		IWorkItemHandle workItemHandle= fContext.targetContext.workingCopyManager.connectNew(workitemType, monitor);
		return fContext.targetContext.workingCopyManager.getWorkingCopy(workItemHandle);
	}

	private List<IEndPointDescriptor> getEndPointsToCopy(IWorkItemReferences sourceReferences) {
		if (fContext.configuration.copyLinks) {
			List<IEndPointDescriptor> all= new ArrayList<IEndPointDescriptor>(sourceReferences.getTypes());
			if (!fContext.configuration.copyAttachments) {
				all.remove(WorkItemEndPoints.ATTACHMENT);
			}
			return all;
		}
		return Collections.singletonList(WorkItemEndPoints.ATTACHMENT);
	}

	private void updateEndPoint(IWorkItemReferences source, IWorkItemReferences target, IEndPointDescriptor endPoint, IProgressMonitor monitor) throws TeamRepositoryException {
		ILinkProcessor processor= LinkProcessors.getProcessor(endPoint);
		if (processor != null) {
			for (IReference reference : source.getReferences(endPoint)) {
				processor.prepareTargetLink(target, endPoint, reference, fContext, monitor);
			}
		}
	}

	private String createTextLink(IWorkItem workItem) {
		String linkText= WorkItemTextUtilities.getWorkItemText(workItem);
		String uri= Location.namedLocation(workItem, fContext.sourceContext.auditableClient.getPublicRepositoryURI()).toAbsoluteUri().toString();
		return String.format("<a href=\"%s\">%s</a>", uri, linkText); //$NON-NLS-1$
	}

	private static class BatchIterator<E> implements Iterator<Collection<E>> {

		private final List<E> fElements;
		private final int fBatchSize;

		public BatchIterator(Collection<E> elements, int batchSize) {
			fElements= new ArrayList<E>(elements);
			fBatchSize= batchSize;
		}

		@Override
		public boolean hasNext() {
			return !getNextChunk().isEmpty();
		}

		@Override
		public Collection<E> next() {
			Collection<E> nextChunk= getNextChunk();
			Collection<E> next= new ArrayList<E>(nextChunk);
			nextChunk.clear();
			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private Collection<E> getNextChunk() {
			return fElements.subList(0, Math.min(fElements.size(), fBatchSize));
		}

	}
}
