/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
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
	private HashMap<UUID, IWorkItemHandle> fWorkItemPairs= new HashMap<UUID, IWorkItemHandle>();

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

	public void addPair(IWorkItemHandle current, IWorkItemHandle remote) {
		fWorkItemPairs.put(current.getItemId(), remote);
	}

	public IWorkItemHandle getPair(IWorkItemHandle current) {
		return fWorkItemPairs.get(current.getItemId());
	}

}
