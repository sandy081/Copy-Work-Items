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

import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.query.IQueryDescriptor;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EvaluationContext {

	public IQueryDescriptor queryDescriptor;
	public String message;
	public IStatus status;
	public List<IWorkItemHandle> result;

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
