package com.nexusbpm.editor.worker;

import com.nexusbpm.editor.icon.ProgressIcon;

/**
 * An implementation of the Worker interface that gives the user visual feedback
 * whenever a job is being executed.
 * 
 * @author     Daniel Gredler
 * @created    05/03/2004
 */
public abstract class CapselaWorker implements Worker
{
	private String _name;

	/**
	 * Constructor which uses a name to make it easier to debug
	 * and profile the workers.
	 * @param name the name of the worker.
	 */
	public CapselaWorker(String name) {
		_name = "Capsela Worker: " + name;
	}

	/**
	 * @return the name of this worker.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @see com.ichg.capsela.client.thread.Worker#execute()
	 */
	public void execute() throws Throwable
	{
		try {
			ProgressIcon.getInstance().start();
			this.run();
		} finally {
			ProgressIcon.getInstance().stop();
		}
	}

	/**
	 * The actual implementation of whatever work this worker does.
	 * Implemented by each subclass.
	 * @throws Throwable if the worker encounters an error.
	 */
	protected abstract void run() throws Throwable;
}
