/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.team.workitem.common.query.IQueryDescriptor;
import com.ibm.team.workitem.common.query.IQueryDescriptorHandle;

public class CopyWorkItemsFromQuery implements IObjectActionDelegate {

	private IWorkbenchPart fPart;
	private IStructuredSelection fSelection;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fPart= targetPart;
	}

	public void run(IAction action) {
		IQueryDescriptorHandle handle= null;
		if (fSelection.getFirstElement() instanceof IQueryDescriptorHandle) {
			handle= (IQueryDescriptorHandle)fSelection.getFirstElement();
		}
		if (handle == null) {
			return;
		}


		CopyWorkItemsFromQueryWizard wizard= new CopyWorkItemsFromQueryWizard();
		wizard.init(fPart.getSite().getWorkbenchWindow().getWorkbench(), new StructuredSelection(handle));
		WizardDialog dialog= new WizardDialog(fPart.getSite().getShell(), wizard);
		dialog.create();
		dialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection)selection;
			if (fSelection.getFirstElement() instanceof IQueryDescriptor) {
				IQueryDescriptor desc= (IQueryDescriptor)fSelection.getFirstElement();
				if (desc.isPropertySet(IQueryDescriptor.HAS_PARAMETERS_PROPERTY) && desc.hasParameterVariables()) {
					action.setEnabled(false);
					return;
				}
			}
		}
		action.setEnabled(true);
	}
}
