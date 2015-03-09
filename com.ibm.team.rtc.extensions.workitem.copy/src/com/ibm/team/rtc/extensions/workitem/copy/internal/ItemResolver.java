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
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.IItemHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.common.UUID;

@SuppressWarnings("unchecked")
public class ItemResolver {

	public interface ICallback<T> {
		public void with(T result) throws TeamRepositoryException;
	}

	private final IItemManager fManager;
	private final HashMap<UUID, IItem> fResolved= new HashMap<UUID, IItem>();

	private final List<Deferred<IItem>> fDeferreds= new ArrayList<ItemResolver.Deferred<IItem>>();
	private final List<IItemHandle> fItemsToResolve= new ArrayList<IItemHandle>();

	public ItemResolver(ITeamRepository repository) {
		fManager= repository.itemManager();
	}

	public <H extends IItemHandle, V extends IItem> Deferred<V> resolve(H handle) throws TeamRepositoryException {
		Deferred<V> deferred= new Deferred<V>();
		V item= (V)fResolved.get(handle.getItemId());
		if (item != null) {
			return deferred.onResolved(item);
		}

		fDeferreds.add((Deferred<IItem>)deferred);
		fItemsToResolve.add(handle);
		return deferred;
	}

	public void execute(IProgressMonitor monitor) throws TeamRepositoryException {
		if (fItemsToResolve.isEmpty())
			return;

		try {
			addToResolvedList(fManager.fetchCompleteItems(fItemsToResolve, IItemManager.DEFAULT, monitor));
			for (int i= 0; i < fItemsToResolve.size(); i++) {
				IItemHandle handle= fItemsToResolve.get(i);
				Deferred<IItem> deferred= fDeferreds.get(i);
				IItem item= fResolved.get(handle.getItemId());
				deferred.onResolved(item);
			}
		} finally {
			fDeferreds.clear();
			fItemsToResolve.clear();
		}
	}

	private void addToResolvedList(List<IItem> items) {
		for (IItem item : items) {
			fResolved.put(item.getItemId(), item);
		}
	}

	public static class Deferred<T extends IItem> {

		private ICallback<T> fSuccess;
		private T fResult;

		public Deferred<T> success(ICallback<T> success) throws TeamRepositoryException {
			fSuccess= success;
			if (fResult != null) {
				onResolved(fResult);
			}
			return this;
		}

		private Deferred<T> onResolved(IItem result) throws TeamRepositoryException {
			fResult= (T)result;
			if (fSuccess != null) {
				fSuccess.with(fResult);
			}
			return this;
		}
	}

}
