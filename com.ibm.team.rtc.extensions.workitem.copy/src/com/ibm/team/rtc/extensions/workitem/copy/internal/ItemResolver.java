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
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.IItemHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.common.UUID;

@SuppressWarnings("unchecked")
public class ItemResolver {

	public interface ICallback<T> {
		public void with(T result, IProgressMonitor monitor) throws TeamRepositoryException;
	}

	private final HashMap<UUID, IItem> fResolved= new HashMap<UUID, IItem>();
	private final List<Deferred<IItem>> fDeferreds= new ArrayList<ItemResolver.Deferred<IItem>>();
	private final List<IItemHandle> fItemsToResolve= new ArrayList<IItemHandle>();

	private final IItemManager fManager;

	public ItemResolver(ITeamRepository repository) {
		fManager= repository.itemManager();
	}

	public <H extends IItemHandle, V extends IItem> Deferred<V> resolve(H handle, IProgressMonitor monitor) throws TeamRepositoryException {
		Deferred<V> deferred= new Deferred<V>(monitor);
		V item= (V)fResolved.get(handle.getItemId());
		if (item != null) {
			return deferred.onResolved(item, monitor);
		}

		fDeferreds.add((Deferred<IItem>)deferred);
		fItemsToResolve.add(handle);
		return deferred;
	}

	public void execute(IProgressMonitor monitor) throws TeamRepositoryException {
		if (fItemsToResolve.isEmpty())
			return;

		try {
			addToResolvedList(fManager.fetchCompleteItems(fItemsToResolve, IItemManager.DEFAULT, SubMonitor.convert(monitor, fItemsToResolve.size())));
			for (int i= 0; i < fItemsToResolve.size(); i++) {
				IItemHandle handle= fItemsToResolve.get(i);
				Deferred<IItem> deferred= fDeferreds.get(i);
				IItem item= fResolved.get(handle.getItemId());
				deferred.onResolved(item, monitor);
			}
		} finally {
			fDeferreds.clear();
			fItemsToResolve.clear();
			fResolved.clear();
		}
	}

	private void addToResolvedList(List<IItem> items) {
		for (IItem item : items) {
			fResolved.put(item.getItemId(), item);
		}
	}

	public static class Deferred<T extends IItem> {

		private final IProgressMonitor fMonitor;
		private ICallback<T> fSuccess;
		private T fResult;

		public Deferred(IProgressMonitor monitor) {
			fMonitor= monitor;
		}

		public Deferred<T> success(ICallback<T> success) throws TeamRepositoryException {
			fSuccess= success;
			if (fResult != null) {
				onResolved(fResult, fMonitor);
			}
			return this;
		}

		private Deferred<T> onResolved(IItem result, IProgressMonitor monitor) throws TeamRepositoryException {
			fResult= (T)result;
			if (fSuccess != null) {
				fSuccess.with(fResult, monitor);
			}
			return this;
		}
	}

}
