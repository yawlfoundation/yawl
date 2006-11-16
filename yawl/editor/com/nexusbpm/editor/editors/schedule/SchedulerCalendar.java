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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
import com.nexusbpm.editor.worker.GlobalEventQueue;
import com.nexusbpm.editor.worker.Worker;
import com.nexusbpm.scheduler.CronTriggerEx;
import com.nexusbpm.scheduler.QuartzEvent;
import com.nexusbpm.scheduler.StartYawlCaseJob;
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
    
    private static final DateFormat format = new SimpleDateFormat( "hh:mm a" ); 
    
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
    	
    	calendar = new JCalendar( null, null, true, false,
    			new HistoricContentProvider( scheduler ), this, new SchedulerListCellRenderer() );
    	
    	getContentPane().removeAll();
    	getContentPane().add( calendar );
    }
    
	public void mouseClicked( MouseEvent event, Object[] selected, Date date ) {
		if( SwingUtilities.isLeftMouseButton( event ) &&
				event.getClickCount() == 2 &&
				selected != null &&
				selected.length == 1 ) {
			ScheduledContent content = (ScheduledContent) selected[0];
			
			if( content.getEvent() != null ) {
				new ViewInstanceStatusAction( content.getEvent() ).actionPerformed( null );
			}
			else if( content.getTrigger() != null ) {
				new EditExistingScheduleAction( content ).actionPerformed( null );
			}
		}
		else if( SwingUtilities.isRightMouseButton( event ) ) {
			JPopupMenu p = new JPopupMenu();
			
			JMenuItem create = new JMenuItem( new CreateNewScheduleAction() );
			create.setText( "Create new Workflow Schedule" );
			
			p.add( create );
			
			Set<Trigger> items = new HashSet<Trigger>();
			
			for( int i = 0; i < selected.length; i++ ) {
				ScheduledContent content = (ScheduledContent) selected[i];
				
				if( content.getTrigger() != null ) {
					if( items.contains( content.getTrigger() ) ) {
						continue;
					}
					items.add( content.getTrigger() );
					
					p.addSeparator();
					
					JMenuItem item = new JMenuItem( new EditExistingScheduleAction( (ScheduledContent) selected[i] ) );
					item.setText( "Edit schedule '" + ((ScheduledContent) selected[i] ).getTrigger().getName() + "'" );
					p.add( item );
					
					item = new JMenuItem( new DeleteExistingScheduleAction( (ScheduledContent) selected[i] ) );
					item.setText( "Delete schedule '" + ((ScheduledContent) selected[i] ).getTrigger().getName() + "'" );
					p.add( item );
				}
			}
			
			for( int i = 0; i < selected.length; i++ ) {
				ScheduledContent content = (ScheduledContent) selected[i];
				
				if( content.getEvent() != null ) {
					p.addSeparator();
					
					JMenuItem item = new JMenuItem( new ViewInstanceStatusAction( content.getEvent() ) );
					item.setText( "View status for '" + content.getEvent().getTriggerName() + "' at " +
							format.format( content.getScheduledFireTime() ) );
					p.add( item );
					
					item = new JMenuItem( new ViewInstanceAction( content.getEvent() ) );
					item.setText( "View instance for '" + content.getEvent().getTriggerName() + "' at " +
							format.format( content.getScheduledFireTime() ) );
					p.add( item );
				}
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
				data.put( StartYawlCaseJob.MAP_KEY_SPEC_ID, i.getUri() );
				t.setJobDataMap( data );
				
				scheduleJob( new JobDetail("LaunchCase", "DEFAULT", StartYawlCaseJob.class), t );
				
				scheduler.getTriggerState( t.getName(), t.getGroup() );
				
				LOG.info( "Workflow Schedule '" + t.getName() + "' created." );
				
				// force the calendar (the UI) to update
				refresh();
			}
			catch( SchedulerException e ) {
				LOG.error( "Error scheduling trigger for specification " + i.getUri(), e );
			}
			catch( ParseException e ) {
				LOG.error( "Scheduler Dialog created an invalid cron expression!", e );
			}
			LOG.debug( i.toString() );
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
				t.setJobGroup( trigger.getJobGroup() );
				
				JobDataMap data = new JobDataMap();
				data.put( "specID", i.getUri() );
				t.setJobDataMap( data );
				
				scheduler.rescheduleJob( oldName, "DEFAULT", t );
				
				scheduler.getTriggerState( t.getName(), t.getGroup() );
				
				Trigger t2 = scheduler.getTrigger( t.getName(), t.getGroup() );
				
				LOG.debug( t.toString() );
				LOG.debug( t2 == null ? "null" : t2.toString() );
				
				String msg = "Workflow Schedule '" + oldName + "' updated";
				if( ! oldName.equals( t.getName() ) ) {
					msg += " and renamed to '" + t.getName() + "'";
				}
				LOG.info( msg );
				
				// force the calendar (the UI) to update
				refresh();
			}
			catch( SchedulerException e ) {
				LOG.error( "Error modifying schedule '" + oldName
						+ "' for specification " + i.getUri(), e );
			}
			catch( ParseException e ) {
				LOG.error( "Scheduler Dialog created an invalid cron expression!", e );
			}
		}
	}
	
	private void deleteExistingSchedule( Trigger trigger ) {
		int selection = JOptionPane.showConfirmDialog(
				WorkflowEditor.getInstance(),
				"Do you really want to delete the schedule '" + trigger.getName() + "'?",
				"Delete Workflow Schedule?",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE );
		if( selection == JOptionPane.YES_OPTION ) {
			try {
				scheduler.unscheduleJob( trigger.getName(), trigger.getGroup() );
				
				LOG.info( "Workflow Schedule '" + trigger.getName() + "' deleted." );
				
				// force the calendar (the UI) to update
				refresh();
			}
			catch( SchedulerException e ) {
				LOG.error( "Error unscheduling Workflow!", e );
			}
		}
	}
	
	private void viewInstanceStatus( QuartzEvent event ) {
		// TODO open a dialog showing instance status
	}
	
	private void viewInstance( QuartzEvent event ) {
		// TODO open up the instance
	}
	
	/**
	 * Forces the calendar (the UI) to update.
	 */
	private void refresh() {
		Runnable r = new Runnable() {
			public void run() {
				calendar.setCalendar( calendar.getCalendar() );
			}
		};
		if( SwingUtilities.isEventDispatchThread() ) {
			r.run();
		}
		else {
			SwingUtilities.invokeLater( r );
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
	
	private class DeleteExistingScheduleAction extends AbstractAction {
		private ScheduledContent content;
		public DeleteExistingScheduleAction( ScheduledContent content ) {
			this.content = content;
		}
		public void actionPerformed( ActionEvent e ) {
			Worker worker = new Worker() {
				public String getName() {
					return "Delete existing Workflow Schedule";
				}
				public void execute() {
					if( content.getTrigger() instanceof Trigger ) {
						deleteExistingSchedule( (Trigger) content.getTrigger() );
					}
					else {
						LOG.warn( "Invalid Workflow Schedule trigger type!" );
					}
				}
			};
			GlobalEventQueue.add( worker );
		}
	}
	
	private class ViewInstanceStatusAction extends AbstractAction {
		private QuartzEvent event;
		public ViewInstanceStatusAction( QuartzEvent event ) {
			this.event = event;
		}
		public void actionPerformed( ActionEvent e ) {
			Worker worker = new Worker() {
				public String getName() {
					return "View Instance Status";
				}
				public void execute() {
					viewInstanceStatus( event );
				}
			};
			GlobalEventQueue.add( worker );
		}
	}
	
	private class ViewInstanceAction extends AbstractAction {
		private QuartzEvent event;
		public ViewInstanceAction( QuartzEvent event ) {
			this.event = event;
		}
		public void actionPerformed( ActionEvent e ) {
			Worker worker = new Worker() {
				public String getName() {
					return "View Instance";
				}
				public void execute() {
					viewInstance( event );
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
