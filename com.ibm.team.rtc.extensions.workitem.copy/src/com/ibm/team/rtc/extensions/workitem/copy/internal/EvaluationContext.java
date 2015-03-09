/*******************************************************************************
 * Licensed Materials - Property of IBM
 * © Copyright IBM Corporation 2015. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package com.ibm.team.rtc.extensions.workitem.copy.internal;

import java.util.LinkedHashMap;

import com.ibm.team.workitem.common.query.IQueryDescriptor;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EvaluationContext {

	public IQueryDescriptor queryDescriptor;
	public String message;

	public RepositoryContext sourceContext;
	public RepositoryContext targetContext;
	public final Configuration configuration= new Configuration();

	private final LinkedHashMap<Key<?>, Object> fStore= new LinkedHashMap<Key<?>, Object>(10, 0.9f, true);

	public <V> void set(Key<V> key, V value) {
		fStore.put(key, value);
	}

	public <V> V get(Key<V> key) {
		return (V)fStore.get(key);
	}

	public static class Key<V> {
		final String fKey;

		public Key(String key) {
			super();
			fKey= key;
		}

		@Override
		public int hashCode() {
			final int prime= 31;
			int result= 1;
			result= prime * result + ((fKey == null) ? 0 : fKey.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other= (Key)obj;
			if (fKey == null) {
				if (other.fKey != null)
					return false;
			} else if (!fKey.equals(other.fKey))
				return false;
			return true;
		}

	}
}
