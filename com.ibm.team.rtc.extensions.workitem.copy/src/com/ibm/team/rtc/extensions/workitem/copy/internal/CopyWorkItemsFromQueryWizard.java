/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2005, 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:  Use,
 * duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.team.foundation.rcp.ui.util.FoundationUIJob;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.rtc.extensions.workitem.copy.WorkItemsCopyPlugIn;
import com.ibm.team.workitem.common.internal.util.Utils;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.query.IQueryDescriptor;
import com.ibm.team.workitem.common.query.IQueryDescriptorHandle;
import com.ibm.team.workitem.rcp.ui.WorkItemUI;

/**
 * @since 0.6
 */
@SuppressWarnings("restriction")
public class CopyWorkItemsFromQueryWizard extends Wizard implements IImportWizard {

	private EvaluationContext fContext;

	public CopyWorkItemsFromQueryWizard() {
		setWindowTitle("Copy Work Items");
		setNeedsProgressMonitor(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (!(selection.getFirstElement() instanceof IQueryDescriptorHandle)) {
			return;
		}
		
		final IQueryDescriptorHandle handle= (IQueryDescriptorHandle)selection.getFirstElement();
		fContext= new EvaluationContext();
		fContext.sourceContext= new RepositoryContext(false, (ITeamRepository)handle.getOrigin());

		try {
			workbench.getProgressService().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						fContext.queryDescriptor= fContext.sourceContext.auditableClient.fetchCurrentAuditable(handle, IQueryDescriptor.FULL_PROFILE, monitor);
						fContext.sourceContext.projectArea= fContext.queryDescriptor.getProjectArea();
					} catch (TeamRepositoryException x) {
						throw new InterruptedException(x.getMessage());
					}
				}
			});
		} catch (InvocationTargetException e) {
			// handle error
		} catch (InterruptedException e) {
			// handle error
		}

		addPage(new ProjectAreaSelectionPage(fContext));
		addPage(new ConfigurationPage(fContext));
	}

	@Override
	public boolean canFinish() {
		return fContext.targetContext != null 
				&& fContext.targetContext.projectArea != null 
				&& fContext.targetContext.isAdmin;
	}

	@Override
	public boolean performFinish() {
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					Utils.runImport(new Runnable() {
						@Override
						public void run() {
							new CopyWorkItemsJob(fContext).run(monitor);
						}
					});
				}
			});
		} catch (InvocationTargetException e) {
			Throwable cause= e.getCause() != null ? e.getCause() : e;
			fContext.status= new Status(IStatus.ERROR, WorkItemsCopyPlugIn.ID, IStatus.ERROR, cause.getMessage(), cause);
		} catch (InterruptedException e) {
			Throwable cause= e.getCause() != null ? e.getCause() : e;
			fContext.status= new Status(IStatus.ERROR, WorkItemsCopyPlugIn.ID, IStatus.ERROR, cause.getMessage(), cause);
		}
		return postCopy();
	}

	private boolean postCopy() {
		if (!fContext.status.isOK()) {
			ErrorDialog.openError(getContainer().getShell(), "Copy Work Items", fContext.status.getMessage(), fContext.status);
			WorkItemsCopyPlugIn.INSTANCE.logUnexpected(fContext.status);
			return true;
		}

		new FoundationUIJob("") { //$NON-NLS-1$
			@Override
			protected IStatus runProtectedInUI(IProgressMonitor monitor) throws Exception {
				IWorkbenchWindow w= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (w != null) {
					List<IWorkItemHandle> toShow= fContext.result.size() > 1000 ? fContext.result.subList(0, 999) : fContext.result;
					WorkItemUI.showWorkItems(w, fContext.targetContext.projectArea, "Copied Work Items. Showing (" + toShow.size() + " of " + fContext.result.size() + ").", toShow.toArray(new IWorkItemHandle[toShow.size()]));
				}
				return Status.OK_STATUS;
			}
		}.schedule();
		return true;
	}

}
