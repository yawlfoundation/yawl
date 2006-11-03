/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.editor.editors.schedule;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.nexusbpm.editor.WorkflowEditor;
import com.nexusbpm.editor.desktop.CapselaInternalFrame;
import com.nexusbpm.editor.editors.schedule.ScheduledContentProvider.ScheduledContent;
import com.nexusbpm.editor.worker.GlobalEventQueue;
import com.nexusbpm.editor.worker.Worker;
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
    
    private Scheduler scheduler;
    
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
    	
    	this.scheduler = scheduler;
    	
    	calendar = new JCalendar( null, null, true, false, new ScheduledContentProvider( scheduler ), this );
    	
    	getContentPane().removeAll();
    	getContentPane().add( calendar );
    }
    
	public void mouseClicked( MouseEvent event, Object[] selected, Date date ) {
		if( SwingUtilities.isRightMouseButton( event ) ) {
			JPopupMenu p = new JPopupMenu();
			
			JMenuItem create = new JMenuItem( new CreateNewScheduleAction() );
			create.setText( "Create new Workflow Schedule" );
			
			p.add( create );
			for( int i = 0; i < selected.length; i++ ) {
				p.add( new JMenuItem( new EditExistingScheduleAction( (ScheduledContent) selected[i] ) ) );
			}
			p.show( (Component) event.getSource(), event.getX(), event.getY() );
		}
	}
	
	private void createNewSchedule() {
		ScheduleInformation i = SchedulerDialog.showSchedulerDialog( WorkflowEditor.getInstance(), null );
		if( i != null ) {
			try {
				CronTriggerEx t = new CronTriggerEx( i.getScheduleName(), "DEFAULT" );
				t.setCronExpression( i.getCronExpression() );
				t.setEndTime( i.getEndDate() );
				t.setStartTime( i.getStartDate() );
				
				JobDataMap data = new JobDataMap();
				data.put( "specID", i.getUri() );
				t.setJobDataMap( data );
				
				scheduleJob( new JobDetail("new", "group", AbstractJob.class), t );
				
				// force the calendar (the UI) to update
				calendar.setCalendar( calendar.getCalendar() );
			}
			catch( SchedulerException e ) {
				LOG.error( "Error scheduling trigger for specification " + i.getUri(), e );
			}
			catch( ParseException e ) {
				LOG.error( "Scheduler Dialog created an invalid cron expression!", e );
			}
			// TODO remove once finished debugging/testing
			LOG.info( i.toString() );
		}
	}
	
	private void editExistingSchedule( CronTriggerEx trigger ) {
		ScheduleInformation i = new ScheduleInformation(
				trigger.getName(),
				trigger.getJobDataMap().getString( "specID" ),
				trigger.getCronExpression(),
				trigger.getStartTime(),
				trigger.getEndTime() );
		String oldName = trigger.getName();
		i = SchedulerDialog.showSchedulerDialog( WorkflowEditor.getInstance(), i );
		if( i != null ) {
			try {
				CronTriggerEx t = new CronTriggerEx( i.getScheduleName(), "DEFAULT" );
				t.setCronExpression( i.getCronExpression() );
				t.setEndTime( i.getEndDate() );
				t.setStartTime( i.getStartDate() );
				t.setJobName( trigger.getJobName() );
				t.setGroup( trigger.getGroup() );
				
				JobDataMap data = new JobDataMap();
				data.put( "specID", i.getUri() );
				t.setJobDataMap( data );
				
				scheduler.rescheduleJob( oldName, "DEFAULT", t );
				
				// force the calendar (the UI) to update
				calendar.setCalendar( calendar.getCalendar() );
			}
			catch( SchedulerException e ) {
				LOG.error( "TODO: error msg", e );
			}
			catch( ParseException e ) {
				LOG.error( "Scheduler Dialog created an invalid cron expression!", e );
			}
		}
	}
	
	private class CreateNewScheduleAction extends AbstractAction {
		public void actionPerformed( ActionEvent e ) {
			Worker worker = new Worker() {
				public String getName() {
					return "Create new Workflow Schedule";
				}
				public void execute() {
					createNewSchedule();
				}
			};
			GlobalEventQueue.add( worker );
		}
	}
	
	private class EditExistingScheduleAction extends AbstractAction {
		private ScheduledContent content;
		public EditExistingScheduleAction( ScheduledContent content ) {
			this.content = content;
		}
		public void actionPerformed( ActionEvent e ) {
			Worker worker = new Worker() {
				public String getName() {
					return "Edit existing Workflow Schedule";
				}
				public void execute() {
					if( content.getTrigger() instanceof CronTriggerEx ) {
						editExistingSchedule( (CronTriggerEx) content.getTrigger() );
					}
					else {
						LOG.warn( "Invalid Workflow Schedule trigger type!" );
					}
				}
			};
			GlobalEventQueue.add( worker );
		}
	}
	
	public void scheduleJob( JobDetail job, Trigger trigger ) throws SchedulerException {
		trigger.setJobGroup( job.getGroup() );
		trigger.setJobName( job.getName() );
		
		scheduler.addJob( job, true );
		scheduler.scheduleJob( trigger );
	}
	
	@Override
	protected void setInputElementsEnabled( Container c, Component[] exempt, boolean enabled ) {
		// do not disable any input elements for the calendar
	}
}
