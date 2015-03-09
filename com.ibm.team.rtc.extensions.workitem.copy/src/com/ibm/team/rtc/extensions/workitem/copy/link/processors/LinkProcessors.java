/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.link.processors;

import com.ibm.team.links.common.registry.IEndPointDescriptor;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.WorkItemEndPoints;

public class LinkProcessors {

	private static final AttachmentProcessor ATTACHMENT_PROCESSOR= new AttachmentProcessor();
	private static final ILinkProcessor WORKITEM_LINK_PROCESSOR= new WorkItemLinkProcessor();
	private static final ILinkProcessor URI_LINK_PROCESSOR= new URILinkProcessor();

	public static ILinkProcessor getProcessor(IEndPointDescriptor endPoint) {
		if (WorkItemEndPoints.ATTACHMENT.equals(endPoint)) {
			return ATTACHMENT_PROCESSOR;
		}

		if (endPoint.isItemReference() && endPoint.getReferencedItemType().equals(IWorkItem.ITEM_TYPE)) {
			return WORKITEM_LINK_PROCESSOR;
		}

		if (WorkItemEndPoints.RELATED_ARTIFACT.equals(endPoint)) {
			return URI_LINK_PROCESSOR;
		}

		return null;
	}

}
