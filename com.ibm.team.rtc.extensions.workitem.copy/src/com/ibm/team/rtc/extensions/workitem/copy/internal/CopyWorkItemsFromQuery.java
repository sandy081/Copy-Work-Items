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
