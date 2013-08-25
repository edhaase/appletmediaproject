package org.amp.mediaserver.contentdirectory.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.amp.mediaserver.contentdirectory.properties.InvalidPropertyException;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;

/*
 * Comparator for sorting returned data based on request sort criterion.
 *   Used to implement java sorting operations.
 */
public class SortCriterionComparator implements Comparator<DIDLObject> {

	// SortCriterion[] order;
	
	final private static Logger log = Logger.getLogger(SortCriterionComparator.class.getName());
	
	final protected Comparator<DIDLObject> comparator;
	
	
	public SortCriterionComparator(SortCriterion[] orderby) {				
		if(orderby == null) { 
			log.info("Nonsorting-comparator only");
			comparator = new NonsortingComparator();
			return;
		}
		
		List<Comparator<DIDLObject>> list = new ArrayList<Comparator<DIDLObject>>();
		for(SortCriterion sorter : orderby) {

			try {
				
				if(sorter.isAscending())
					list.add( new PropertyComparator(sorter.getPropertyName()) );
				else
					list.add( new OrderInvertingComparator(new PropertyComparator(sorter.getPropertyName())) );
				
			} catch (InvalidPropertyException e) {
				log.warning("Unable to sort by " + sorter.toString() + " - " + e.getMessage());
				e.printStackTrace();
			}
			log.info("Adding property comparator " + sorter.getPropertyName());
		}
		
		// For our current uses with the treeset, this solves a problem.
		list.add(new NonsortingComparator());
		
		comparator = new ComparatorChain<DIDLObject>(list);		
	}
	
	public SortCriterionComparator(final String criteria) {
		this(SortCriterion.valueOf(criteria));
	}
	
	
	@Override
	public int compare(DIDLObject o1, DIDLObject o2) {		
		if(comparator == null)
			return 1;				
			
		return comparator.compare(o1, o2);
	}

}
