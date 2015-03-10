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

import java.util.HashMap;

import com.ibm.team.apt.internal.client.IIterationPlanClient;
import com.ibm.team.apt.internal.common.process.AuditablePlanningItemStore;
import com.ibm.team.apt.internal.common.rcp.IPlanningItemStore;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.client.IContentManager;
import com.ibm.team.repository.client.IContributorManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.UUID;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.client.IQueryClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.IWorkItemWorkingCopyManager;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;

@SuppressWarnings("restriction")
public class RepositoryContext {

	public final ITeamRepository teamRepository;
	public final IWorkItemClient workItemClient;
	public final IWorkItemWorkingCopyManager workingCopyManager;
	public final IAuditableClient auditableClient;
	public final IQueryClient queryClient;

	public final IIterationPlanClient iterationPlanClient;
	public final IPlanningItemStore planItemStore;

	public final IContentManager contentManager;
	public final IContributorManager contributorManager;
	public final ItemResolver itemResolver;

	public IProjectAreaHandle projectArea;
	private HashMap<UUID, IWorkItem> fWorkItemPairs= new HashMap<UUID, IWorkItem>();

	public boolean isAdmin= false;

	public RepositoryContext(boolean writeable, ITeamRepository teamRepository) {
		this.teamRepository= teamRepository;

		workItemClient= (IWorkItemClient)teamRepository.getClientLibrary(IWorkItemClient.class);
		workingCopyManager= workItemClient.createWorkingCopyManager(teamRepository.getName(), writeable);
		auditableClient= (IAuditableClient)teamRepository.getClientLibrary(IAuditableClient.class);
		queryClient= (IQueryClient)teamRepository.getClientLibrary(IQueryClient.class);

		iterationPlanClient= (IIterationPlanClient)teamRepository.getClientLibrary(IIterationPlanClient.class);
		planItemStore= new AuditablePlanningItemStore(auditableClient);

		contentManager= teamRepository.contentManager();
		contributorManager= teamRepository.contributorManager();
		itemResolver= new ItemResolver(teamRepository);
	}

	public void addPair(IWorkItemHandle current, IWorkItem remote) {
		fWorkItemPairs.put(current.getItemId(), remote);
	}

	public IWorkItem getPair(IWorkItemHandle current) {
		return fWorkItemPairs.get(current.getItemId());
	}

}
