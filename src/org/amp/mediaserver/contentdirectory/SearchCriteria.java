package org.amp.mediaserver.contentdirectory;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.amp.mediaserver.contentdirectory.operator.LogicalAND;
import org.amp.mediaserver.contentdirectory.operator.LogicalNOT;
import org.amp.mediaserver.contentdirectory.operator.LogicalOR;
import org.amp.mediaserver.contentdirectory.operator.LogicalTruth;
import org.amp.mediaserver.contentdirectory.operator.Predicate;
import org.amp.mediaserver.contentdirectory.properties.PropertyContains;
import org.amp.mediaserver.contentdirectory.properties.PropertyDerivedFrom;
import org.amp.mediaserver.contentdirectory.properties.PropertyDoesNotContain;
import org.amp.mediaserver.contentdirectory.properties.PropertyEquality;
import org.amp.mediaserver.contentdirectory.properties.PropertyExists;
import org.amp.mediaserver.contentdirectory.properties.PropertyRetriever;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;

/*
 *  upnp:class = '..audioItem'
		becomes [propertyRetriever] [operation] '..audioItem'

*/

/*
 * String:	this is a test and so is this
 * Pattern:	(.*)\s?(and|or)\s?(.*)
 * 
 *	start() = 0, end() = 29
 *	group(0) = "this is a test and so is this"
 *	group(1) = "this is a test "
 *	group(2) = "and"
 *	group(3) = "so is this"
 *
 */
/**
 *  Factory class for creating a predicate from a search string.
 *  It's ugly, but it works.
 */
public abstract class SearchCriteria implements Predicate {

	final static Pattern propertyPattern = Pattern.compile("((?:(.*?):)?(.*?)(?:@(.*?))?)" + "\\s(.*)");
	// final static Pattern stringOpPattern = Pattern.compile("(?i)(derivedFrom|contains|doesNotContain)\\s?\\\"?(.*?)\\\"?");
	final static Pattern stringOpPattern = Pattern.compile("(.*)\\s\\\"(.*)\\\"");
	
	final private static Logger log = Logger.getLogger(SearchCriteria.class.getName());
	
	public static Predicate parse(String str) throws Exception {
		if(str == null) 
			throw new NullPointerException();

		// if(str.equals(""))
		//	return new LogicalTruth();
		
		str = str.trim();		
		log.fine("Input string: " + str);
		
		/////////////////////////////////////////////////////
		// Multiple behaviors.
		/////////////////////////////////////////////////////		
		if( str.contains(" or ") ||  str.contains(" OR ") ) {			
			String [] result = str.split("(?i) OR ", 2);		
			return new LogicalOR(
					SearchCriteria.parse(result[0]),
					SearchCriteria.parse(result[1])
				);
		}
		
		if( str.contains(" and ") || str.contains(" AND ")) {			
			String [] result = str.split("(?i) and ", 2);
			return new LogicalAND(
					SearchCriteria.parse(result[0]),
					SearchCriteria.parse(result[1])
				);
		}
				
			
		/////////////////////////////////////////////////////
		// Find the search string (UPNP Spec 2.5.5.1)
		//  This removes parentheses.
		/////////////////////////////////////////////////////
		str = str.replaceAll("[\\(\\)]", "");

		
		/////////////////////////////////////////////////////
		// Match everything, always return true.
		/////////////////////////////////////////////////////
		if( str.matches("^\\s?\\*\\s?$") )
			return new LogicalTruth();
		

		/////////////////////////////////////////////////////
		// Parse property tag.
		/////////////////////////////////////////////////////		
				
		Matcher matcher = propertyPattern.matcher(str);
		
		if( matcher.matches() == false ) {
			log.fine("Unable to match property tag: " + str);
			// System.err.println("Unable to match property tag: " + str);
			// return null;
			throw new IllegalStateException("Unable to parse search string remainder: [" + str + "]");
		}
		
		
		String remainder = matcher.group(5);

		/////////////////////////////////////////////////////
		// Property existence.
		/////////////////////////////////////////////////////
		if( remainder.matches("(?i)\\s?exists false") ) {
			return new LogicalNOT( new PropertyExists(matcher.group(1)) );
			// return new PropertySearch.HasProperty(matcher.group(1), false);
		}
		
		if( remainder.matches("(?i)\\s?exists true") ) {
			return new PropertyExists(matcher.group(1));
		}
		
		
		
		/////////////////////////////////////////////////////
		// Property derivation.
		/////////////////////////////////////////////////////
		Matcher stringOpMatcher = null;
		try {
			stringOpMatcher = stringOpPattern.matcher(remainder);
						
			stringOpMatcher.matches();
			
			String op = stringOpMatcher.group(1);
			String value = stringOpMatcher.group(2);
			
			if( stringOpMatcher.group(1).equalsIgnoreCase("contains") ) {
				//return new PropertySearch.Contains(matcher.group(1), stringOpMatcher.group(2));
				return new PropertyContains( PropertyRetriever.find(matcher.group(1)),
											 stringOpMatcher.group(2) );
				
			} else if( stringOpMatcher.group(1).equalsIgnoreCase("doesNotContain") ) {
				// return new PropertySearch.DoesNotContain(matcher.group(1), stringOpMatcher.group(2));
				return new PropertyDoesNotContain( PropertyRetriever.find(matcher.group(1)),
						 						   stringOpMatcher.group(2) );
				
			} else if( stringOpMatcher.group(1).equalsIgnoreCase("derivedFrom") ) {
				// return new PropertySearch.DerivedFrom(matcher.group(1), stringOpMatcher.group(2));
				return new PropertyDerivedFrom( 
												PropertyRetriever.find(matcher.group(1)),
												stringOpMatcher.group(2) );
			}
			
			// // ‘=’ | ‘!=’ | ‘<’ | ‘<=’ | ‘>’ | ‘>=’
			if(op.equals("!=")) return new LogicalNOT( new PropertyEquality(PropertyRetriever.find(matcher.group(1)), value) );
			if(op.equals("=")) return new PropertyEquality(PropertyRetriever.find(matcher.group(1)), value);
			if(op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=") ) {
				throw new ContentDirectoryException( ContentDirectoryService.UNSUPPORTED_SEARCH_CRITERIA,
						 "Unsupported or invalid search criteria."); 
			}
			
			/*
			switch( op.toLowerCase() ) {
				case "!=":
					return new LogicalNOT( new PropertyEquality(PropertyRetriever.find(matcher.group(1)), value) );
				case "=":
					return new PropertyEquality(PropertyRetriever.find(matcher.group(1)), value);
				case "<":
				case "<=":
				case ">":
				case ">=":
					// We can't handle this right now.
					throw new ContentDirectoryException( ContentDirectoryService.UNSUPPORTED_SEARCH_CRITERIA,
														 "Unsupported or invalid search criteria."); 
			}	*/	
			
		} catch(ContentDirectoryException c) {
			throw c;
		} catch(IllegalStateException e) {
			// System.err.println("Matcher error on: [" + remainder + "]");
		} catch(Exception e) {
			// System.err.println("Matcher error 2 on: [" + remainder + "]");
			// System.err.println("SearchCriteria:parse: " + e.getMessage() + " - Remainder: " + remainder);
			e.printStackTrace();
		}
				
		/*
		System.out.println("Property: " + matcher.group(1) );
		System.out.println("Namespace: " + ((matcher.group(2)==null)?"none":matcher.group(2))  );
		System.out.println("Element: " + ((matcher.group(3)==null)?"none":matcher.group(3)));
		System.out.println("Attribute: " + ((matcher.group(4)==null)?"none":matcher.group(4)));
		System.out.println("Remainder: " + ((matcher.group(5)==null)?"none":matcher.group(5)));
				*/

		// System.out.println("No results for " + remainder);
		return null;		
	}
	
}
