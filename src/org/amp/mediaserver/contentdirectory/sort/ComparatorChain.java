package org.amp.mediaserver.contentdirectory.sort;

import java.util.Collection;
import java.util.Comparator;

/**
 * Simple comparator chain. 
 */
public class ComparatorChain<T> implements Comparator<T> {

	protected Collection<Comparator<T>> chain;
		
	public ComparatorChain(Collection<Comparator<T>> c) {
		this.chain = c;		
	}
	
	@Override
	public int compare(T o1, T o2) {
		int result = 0;
		for(Comparator<T> ci : chain) {
			result = ci.compare(o1, o2);
			if(result != 0) {
				return result;
			} 
		}		
		return 0;
	}
	
}
