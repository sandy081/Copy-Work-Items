/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.internal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ConfigurationPage extends WizardPage {

	private final EvaluationContext fContext;

	public ConfigurationPage(EvaluationContext context) {
		super("Configuration", "Configuration", null);
		fContext= context;
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite container= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		layout.numColumns= 2;
		container.setLayout(layout);

		Label label= new Label(container, SWT.NONE);
		label.setText("Replacement Text:");
		final Text text= new Text(container, SWT.BORDER); //$NON-NLS-1$
		GridData data= new GridData(SWT.FILL, SWT.TOP, true, false);
		text.setLayoutData(data);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fContext.configuration.replacementText= text.getText();
			}
		});

		label= new Label(container, SWT.NONE);
		label.setText("Default Category:");
		final Text categoryText= new Text(container, SWT.BORDER); //$NON-NLS-1$
		data= new GridData(SWT.FILL, SWT.TOP, true, false);
		categoryText.setLayoutData(data);
		categoryText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fContext.configuration.categoryPath= categoryText.getText();
			}
		});

		label= new Label(container, SWT.NONE);
		label.setText("Tags:");
		final Text tagsText= new Text(container, SWT.BORDER); //$NON-NLS-1$
		data= new GridData(SWT.FILL, SWT.TOP, true, false);
		tagsText.setLayoutData(data);
		tagsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fContext.configuration.tags= tagsText.getText();
			}
		});

		label= new Label(container, SWT.NONE);
		label.setText("Copy Attachments:");
		final Button copyAttachmentsButton= new Button(container, SWT.CHECK); //$NON-NLS-1$
		copyAttachmentsButton.setSelection(fContext.configuration.copyAttachments);
		data= new GridData(SWT.FILL, SWT.TOP, true, false);
		copyAttachmentsButton.setLayoutData(data);
		copyAttachmentsButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fContext.configuration.copyAttachments= copyAttachmentsButton.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		label= new Label(container, SWT.NONE);
		label.setText("Copy Links:");
		final Button copyLinksButton= new Button(container, SWT.CHECK); //$NON-NLS-1$
		copyLinksButton.setSelection(fContext.configuration.copyLinks);
		data= new GridData(SWT.FILL, SWT.TOP, true, false);
		copyLinksButton.setLayoutData(data);
		copyLinksButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fContext.configuration.copyLinks= copyLinksButton.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		label= new Label(container, SWT.NONE);
		label.setText("Copy Rank:");
		final Button copyRankButton= new Button(container, SWT.CHECK); //$NON-NLS-1$
		copyRankButton.setSelection(fContext.configuration.copyRankingValue);
		data= new GridData(SWT.FILL, SWT.TOP, true, false);
		copyRankButton.setLayoutData(data);
		copyRankButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fContext.configuration.copyRankingValue= copyRankButton.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		setControl(container);
		Dialog.applyDialogFont(container);
	}
}
