package com.nexusbpm.editor.editors.schedule;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.nexusbpm.editor.desktop.CapselaInternalFrame;
import com.toedter.calendar.CalendarSelectionListener;
import com.toedter.calendar.JCalendar;

/**
 * The calendar for scheduling workflow specifications.
 * 
 * @author Nathan Rose
 */
public class SchedulerCalendar extends CapselaInternalFrame implements CalendarSelectionListener {
	private static final Log LOG = LogFactory.getLog( SchedulerCalendar.class );
    
    private JCalendar calendar;
    
    public static SchedulerCalendar createCalendar() throws SchedulerException {
    	LOG.trace( "creating calendar" );
    	
    	System.setProperty( "org.quartz.properties", "quartz.client.properties" );
    	
    	return new SchedulerCalendar( StdSchedulerFactory.getDefaultScheduler() );
    }
    
    public static SchedulerCalendar createTestCalendar() {
    	LOG.trace( "creating test calendar" );
    	try {
    		System.getProperties().remove( "org.quartz.properties" );
    		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
    		
    		return new SchedulerCalendar( scheduler );
    	}
    	catch( SchedulerException e ) {
    		throw new RuntimeException( e );
    	}
    }
    
    private SchedulerCalendar( Scheduler scheduler ) {
    	removeLoadingLabel();
    	
    	calendar = new JCalendar( null, null, true, false, new ScheduledContentProvider( scheduler ), this );
    	
    	getContentPane().removeAll();
    	getContentPane().add( calendar );
    }
    
	public void mouseClicked( MouseEvent event, Object[] selected, Date date ) {
		if( SwingUtilities.isRightMouseButton( event ) ) {
			JPopupMenu p = new JPopupMenu();
			p.add( new JMenuItem("item" + selected.length) );
			for( int i = 0; i < selected.length; i++ ) {
				p.add( new JMenuItem("" + selected[i]) );
			}
			p.show( (Component) event.getSource(), event.getX(), event.getY() );
		}
//		System.out.println( event );
//		System.out.println( ( selected == null ) ? "null" : selected.length );
//		System.out.println( date );
	}
	
	@Override
	protected void setInputElementsEnabled( Container c, Component[] exempt, boolean enabled ) {
		// do not disable any input elements for the calendar
	}
}
