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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.links.common.IReference;
import com.ibm.team.links.common.registry.IEndPointDescriptor;
import com.ibm.team.repository.common.Location;
import com.ibm.team.repository.common.TeamRepositoryException;
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

	public List<IWorkItemHandle> run(IProgressMonitor monitor) throws TeamRepositoryException {
		SubMonitor progress= SubMonitor.convert(monitor);
		List<IWorkItemHandle> allSources= new ArrayList<IWorkItemHandle>();
		List<IWorkItemHandle> allTargets= new ArrayList<IWorkItemHandle>();
		try {
			Collection<IAttribute> sourceAttributes= new SourceAttributes(fContext).get(progress.newChild(1));
			TargetAttributes targetAttributes= new TargetAttributes(fContext);

			WorkItemsResolver workItemsResolver= new WorkItemsResolver(fContext);
			IWorkItems workItems= workItemsResolver.resolve(progress.newChild(10));

			int totalResultsSize= workItemsResolver.getTotalResultsSize();
			progress.beginTask("Copy Work Items", (totalResultsSize * 5));

			int batchNumber= 0;
			while (workItems.hasNext()) {
				Collection<IWorkItem> batch= workItems.next();
				List<WorkItemWorkingCopy> targets= createTargets(batch, sourceAttributes, targetAttributes, batchNumber, totalResultsSize, progress);
				for (WorkItemWorkingCopy target : targets) {
					allTargets.add(target.getWorkItem());
				}

				batchNumber++;
				allSources.addAll(batch);
			}

			if (fContext.configuration.copyLinks) {
				createLinks(allSources, Arrays.asList(WorkItemEndPoints.ATTACHMENT), progress);
			}

		} finally {
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
		return allTargets;
	}

	private List<WorkItemWorkingCopy> createTargets(Collection<IWorkItem> sources, Collection<IAttribute> sourceAttributes, TargetAttributes targetAttributes, int batchNumber, int totalResultsSize, SubMonitor progress) throws TeamRepositoryException {
		List<WorkItemWorkingCopy> workingCopies= new ArrayList<WorkItemWorkingCopy>(sources.size());
		int counter= 1;
		SubMonitor preparingMonitor= progress.newChild(sources.size() * 2);
		for (IWorkItem source : sources) {

			SubMonitor singleMonitor= preparingMonitor.newChild(1);
			String preparingMessage= "Preparing copies " + (fContext.configuration.copyAttachments ? "with attachments " : "");
			singleMonitor.setTaskName(preparingMessage + "(" + ((batchNumber * WorkItemsResolver.BATCH_SIZE) + counter) + " of " + totalResultsSize + ")");

			String targetType= new WorkItemTypeProcessor().getMapping(null, targetAttributes.findAttribute(IWorkItem.TYPE_PROPERTY, singleMonitor), source.getWorkItemType(), fContext, preparingMonitor);
			WorkItemWorkingCopy target= newTarget(targetType, singleMonitor);

			for (IAttribute sourceAttribute : sourceAttributes) {
				IValueProcessor<Object> processor= (IValueProcessor<Object>)ValueProcessors.getProcessor(sourceAttribute);
				IAttribute targetAttribute= targetAttributes.findAttribute(sourceAttribute.getIdentifier(), singleMonitor);
				if (source.hasAttribute(sourceAttribute)) {
					processor.prepareTargetValue(target.getWorkItem(), targetAttribute, sourceAttribute, source.getValue(sourceAttribute), fContext, singleMonitor);
				}
			}

			if (fContext.configuration.copyAttachments) {
				IWorkItemReferences sourceReferences= fContext.sourceContext.workingCopyManager.getWorkingCopy(source).getReferences();
				updateEndPoint(sourceReferences, target.getReferences(), WorkItemEndPoints.ATTACHMENT, singleMonitor);
			}

			workingCopies.add(target);
			fContext.sourceContext.addPair(source, target.getWorkItem());
			fContext.targetContext.addPair(target.getWorkItem(), source);
			singleMonitor.done();
			counter++;
		}
		fContext.sourceContext.itemResolver.execute(preparingMonitor);
		fContext.targetContext.itemResolver.execute(preparingMonitor);
		preparingMonitor.done();

		for (WorkItemWorkingCopy target : workingCopies) {
			XMLString copiedFromCommentText= XMLString.createFromXMLText("Copied from " + createTextLink((IWorkItem)fContext.targetContext.getPair(target.getWorkItem())));
			IComment comment= target.getWorkItem().getComments().createComment(fContext.targetContext.auditableClient.getUser(), copiedFromCommentText);
			target.getWorkItem().getComments().append(comment);
		}

		SubMonitor saveMonitor= progress.newChild(sources.size());
		saveMonitor.setTaskName("Copying " + ((batchNumber * 100) + sources.size()) + " Work Items (" + totalResultsSize + ")");
		fContext.targetContext.workingCopyManager.save(workingCopies.toArray(new WorkItemWorkingCopy[workingCopies.size()]), saveMonitor);
		saveMonitor.done();
		return workingCopies;
	}

	private WorkItemWorkingCopy newTarget(String type, IProgressMonitor monitor) throws TeamRepositoryException {
		IWorkItemType workitemType= fContext.targetContext.workItemClient.findWorkItemType(fContext.targetContext.projectArea, type, monitor);
		if (workitemType == null) {
			throw new TeamRepositoryException("Mapping not found for work item type: " + type);
		}
		IWorkItemHandle workItemHandle= fContext.targetContext.workingCopyManager.connectNew(workitemType, monitor);
		return fContext.targetContext.workingCopyManager.getWorkingCopy(workItemHandle);
	}

	private void createLinks(List<IWorkItemHandle> sources, List<IEndPointDescriptor> exclude, SubMonitor progress) throws TeamRepositoryException {
		sources= new ArrayList<IWorkItemHandle>(sources);
		int batchNumber= 0;
		int totalResultsSize= sources.size();
		while (!sources.isEmpty()) {

			int len= Math.min(sources.size(), WorkItemsResolver.BATCH_SIZE);
			List<IWorkItemHandle> sourcesChunk= sources.subList(0, len);
			List<WorkItemWorkingCopy> targets= new ArrayList<WorkItemWorkingCopy>();

			SubMonitor linksMonitor= progress.newChild(sourcesChunk.size());
			linksMonitor.setTaskName("Copying Links for " + ((batchNumber * WorkItemsResolver.BATCH_SIZE) + sourcesChunk.size()) + " Work Items (" + totalResultsSize + ")...");

			for (IWorkItemHandle source : sourcesChunk) {
				IWorkItemReferences sourceReferences= fContext.sourceContext.workingCopyManager.getWorkingCopy(source).getReferences();
				WorkItemWorkingCopy target= fContext.targetContext.workingCopyManager.getWorkingCopy(fContext.sourceContext.getPair(source));
				IWorkItemReferences targetReferences= target.getReferences();
				for (IEndPointDescriptor endPoint : sourceReferences.getTypes()) {
					if (exclude == null || !exclude.contains(endPoint)) {
						updateEndPoint(sourceReferences, targetReferences, endPoint, linksMonitor);
					}
				}
				targets.add(target);
			}

			fContext.sourceContext.itemResolver.execute(linksMonitor);
			fContext.targetContext.itemResolver.execute(linksMonitor);
			List<WorkItemWorkingCopy> updated= new ArrayList<WorkItemWorkingCopy>();
			for (WorkItemWorkingCopy workingCopy : targets) {
				if (!workingCopy.getReferences().getChangedReferenceTypes().isEmpty()) {
					updated.add(workingCopy);
				}
			}
			if (!updated.isEmpty()) {
				fContext.targetContext.workingCopyManager.save(updated.toArray(new WorkItemWorkingCopy[updated.size()]), linksMonitor);
			}
			batchNumber++;
			linksMonitor.done();
			sourcesChunk.clear();
		}
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
}
