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
package com.ibm.team.rtc.extensions.workitem.copy.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.ItemProfile;
import com.ibm.team.workitem.common.query.IQueryDescriptor;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResult;

public class WorkItemsResolver {
	
	public interface IWorkItems {

		public boolean hasNext() throws TeamRepositoryException;

		public Collection<IWorkItem> next() throws TeamRepositoryException;
	}

	public static final int BATCH_SIZE= 100;

	private final IQueryDescriptor fQuery;
	private final RepositoryContext fContext;
	private final ItemProfile<IWorkItem> fProfile;

	private int fTotalSize;
	
	public WorkItemsResolver(EvaluationContext evaluationContext) {
		fQuery= evaluationContext.queryDescriptor;
		fContext= evaluationContext.sourceContext;
		fProfile= evaluationContext.configuration.copyAttachments || evaluationContext.configuration.copyLinks ? IWorkItem.FULL_PROFILE : ItemProfile.<IWorkItem> createFullProfile(IWorkItem.ITEM_TYPE);
	}
	
	public IWorkItems resolve(IProgressMonitor monitor) throws TeamRepositoryException {
		IQueryResult<IResult> results= fContext.queryClient.getQueryResults(fQuery);
		results.setLimit(Integer.MAX_VALUE);
		results.setPageSize(BATCH_SIZE);
		fTotalSize= results.getResultSize(monitor).getTotal();
		return new WorkItemsIterator(results, monitor);
	}

	public int getTotalResultsSize() {
		return fTotalSize;
	}

	private class WorkItemsIterator implements IWorkItems {

		private final IQueryResult<IResult> fResults;
		private final IProgressMonitor fMonitor;

		private List<IWorkItem> fNext;

		private WorkItemsIterator(IQueryResult<IResult> results, IProgressMonitor monitor) throws TeamRepositoryException {
			fResults= results;
			fMonitor= monitor;
			fMonitor.beginTask("Fetching Work Items", 10);
			fetchNextPage();
		}

		public boolean hasNext() throws TeamRepositoryException {
			return fNext != null;
		}

		public Collection<IWorkItem> next() throws TeamRepositoryException {
			Collection<IWorkItem> result= fNext;
			fetchNextPage();
			return result;
		}

		private void fetchNextPage() throws TeamRepositoryException {
			if (!fResults.hasNext(fMonitor)) {
				fMonitor.done();
				fNext= null;
				return;
			}

			List<IResult> page= fResults.nextPage(fMonitor);
			List<IWorkItemHandle> handles= new ArrayList<IWorkItemHandle>();
			for (IResult result : page) {
				handles.add((IWorkItemHandle)result.getItem());
			}
			fNext= fContext.auditableClient.resolveAuditables(handles, IWorkItem.FULL_PROFILE, fMonitor);
			fContext.workingCopyManager.connect(fNext, fProfile, fMonitor);
		}

	}
}
