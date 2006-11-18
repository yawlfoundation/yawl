/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.scheduler;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CachedQuartzEventDataSource implements QuartzEventDataSource {
	private static final int CACHE_SIZE = 12;
	
	private TreeSet<HistoryRange> cache;
	private List<HistoryRange> queue;
	private QuartzEventDataSource dataSource;
	
	public CachedQuartzEventDataSource( QuartzEventDataSource dataSource ) {
		this.cache = new TreeSet<HistoryRange>();
		this.queue = new LinkedList<HistoryRange>();
		this.dataSource = dataSource;
	}
	
	public List<QuartzEvent> getEventsBetween( Date startDate, Date endDate ) throws RemoteException {
		// never get history for the future, so modify the end date as needed
		Date now = new Date();
		if( endDate.after( now ) ) {
			endDate = now;
		}
		
		// if we don't already have the whole range cached, cache a large range
		if( !isCached( startDate, endDate ) ) {
			Calendar start = Calendar.getInstance();
			start.setTime( startDate );
			start.add( Calendar.MONTH, -1 );
			Calendar end = Calendar.getInstance();
			end.setTime( startDate );
			end.add( Calendar.MONTH, 2 );
			if( end.getTime().before( endDate ) ) {
				end.setTime( endDate );
			}
			if( end.getTime().after( now ) ) {
				end.setTime( now );
			}
			
			cache( start.getTime(), end.getTime() );
		}
		
		// return the proper range from the cache
		return retrieve( startDate, endDate );
	}
	
	private boolean isCached( Date startDate, Date endDate ) {
		long start = startDate.getTime();
		long end = endDate.getTime();
		
		for( HistoryRange range : cache ) {
			if( range.end.getTime() < start ) {
				continue;
			}
			else if( range.start.getTime() > start ) {
				return false;
			}
			else {
				start = range.end.getTime() + 1;
				if( start > end ) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private List<QuartzEvent> retrieve( Date startDate, Date endDate ) {
		List<QuartzEvent> events = new ArrayList<QuartzEvent>();
		
		for( HistoryRange range : cache ) {
			if( startDate.before( range.end ) && endDate.after( range.start ) ) {
				for( QuartzEvent event : range.events ) {
					long scheduledFireTime = event.getScheduledFireTime().getTime();
					if( scheduledFireTime >= startDate.getTime() &&
							scheduledFireTime <= endDate.getTime() ) {
						events.add( event );
					}
				}
			}
		}
		
		return events;
	}
	
	private void cache( Date startDate, Date endDate ) throws RemoteException {
		// We can't resize the cache after retrieving because we could remove needed data.
		// This will mean the cache size doesn't have a hard cap, but it avoids certain problems
		while( cache.size() > CACHE_SIZE && queue.size() > 0 ) {
			cache.remove( queue.remove( 0 ) );
		}
		
		long start = startDate.getTime();
		long end = endDate.getTime();
		
		Set<HistoryRange> additions = new HashSet<HistoryRange>();
		
		for( HistoryRange range : cache ) {
			if( range.end.getTime() < start ) {
				continue;
			}
			else if( range.start.getTime() > start ) {
				// retrieve from start to min(range.start - 1, end)
				long rangeEnd = ( end < range.start.getTime() ) ? end : range.start.getTime() - 1;
				
				additions.add( new HistoryRange( new Date( start ), new Date( rangeEnd ),
						dataSource.getEventsBetween( new Date( start ), new Date( rangeEnd ) ) ) );
				
				// increment start
				start = rangeEnd + 1;
			}
			else {
				start = range.end.getTime() + 1;
			}
			if( start > end ) {
				break;
			}
		}
		
		if( start < end ) {
			additions.add( new HistoryRange( new Date( start ), new Date( end ),
					dataSource.getEventsBetween( new Date( start ), new Date( end ) ) ) );
		}
		
		for( HistoryRange range : additions ) {
			cache.add( range );
			queue.add( range );
		}
	}
	
	private static class HistoryRange implements Comparable {
		Date start;
		Date end;
		TreeSet<QuartzEvent> events;
		
		HistoryRange( Date start, Date end, List<QuartzEvent> events ) {
			this.start = start;
			this.end = end;
			this.events = new TreeSet<QuartzEvent>(new Comparator() {
				public int compare( Object o1, Object o2 ) {
					if( o1 instanceof QuartzEvent && o2 instanceof QuartzEvent ) {
						QuartzEvent e1 = (QuartzEvent) o1;
						QuartzEvent e2 = (QuartzEvent) o2;
						int compare = e1.getScheduledFireTime().compareTo( e2.getScheduledFireTime() );
						if( compare == 0 ) {
							compare = e1.getTriggerName().compareTo( e2.getTriggerName() );
						}
						if( compare == 0 ) {
							compare = e1.hashCode() - e2.hashCode();
						}
						return compare;
					}
					else if( o1 != null && o2 != null ) {
						return o1.hashCode() - o2.hashCode();
					}
					else if( o1 != null ) {
						return -1;
					}
					else if( o2 != null ) {
						return 1;
					}
					else {
						return 0;
					}
				}
			});
			for( QuartzEvent event : events ) {
				this.events.add( event );
			}
		}

		public int compareTo( Object o ) {
			if( o instanceof HistoryRange ) {
				return start.compareTo( ((HistoryRange) o ).start );
			}
			else {
				return 0;
			}
		}
	}
}
