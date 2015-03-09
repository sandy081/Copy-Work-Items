/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.ibm.team.foundation.common.util.FoundationLog;

public class WorkItemsCopyPlugIn extends AbstractUIPlugin {

	private static BundleContext context;
	public static WorkItemsCopyPlugIn INSTANCE;

	public static final String ID= "com.ibm.team.rtc.extensions.workitem.copy";

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		INSTANCE= this;
		WorkItemsCopyPlugIn.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		WorkItemsCopyPlugIn.context = null;
		INSTANCE= null;
	}

	public void logUnexpected(IStatus status) {
		FoundationLog.logUnexpected(status);
	}

}
