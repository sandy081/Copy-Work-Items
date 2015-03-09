/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.value.processors;

import com.ibm.team.rtc.extensions.workitem.copy.internal.EvaluationContext;

public abstract class AbstractReplacementValueProcessor<T> extends AbstractValueProcessor<T> {

	protected String replace(String targetValue, EvaluationContext context) {
		String replacementText= context.configuration.replacementText;
		if (replacementText == null || replacementText.isEmpty()) {
			return targetValue;
		}

		String[] pairs= replacementText.split(",");
		for (String pair : pairs) {
			String key= pair.substring(0, pair.indexOf("=")).trim();
			String value= pair.substring(pair.indexOf("=") + 1).trim();
			targetValue= targetValue.replaceAll("(?i)" + key, value);
		}

		return targetValue;
	}

}
