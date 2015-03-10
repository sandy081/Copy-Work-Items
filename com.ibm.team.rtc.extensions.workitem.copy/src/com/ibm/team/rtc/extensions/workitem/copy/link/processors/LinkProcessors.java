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
