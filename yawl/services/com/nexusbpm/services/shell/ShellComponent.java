package com.nexusbpm.services.shell;

import java.io.BufferedOutputStream;
import java.io.File;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.util.StreamGobbler;

/**
 * <b>Use case:</b><br>
 * <p>Runs a shell script provided by the user on the operating system that the
 * engine is running on.  The output and error streams are saved as variables
 * of this component.
 *
 * @author             Dean Mao
 * @author             Daniel Gredler
 * @author 			   Matthew Sandoz
 * @created            February 19, 2003
 * @hibernate.subclass discriminator-value="26"
 * @javabean.class     name="ShellComponent"
 *                     displayName="Shell Component"
 */
public class ShellComponent {

	private static final long serialVersionUID = 6210057157938753532L;

	public ShellComponent() {
		super();
	} //ShellComponent()

	public ShellComponent(NexusServiceData data) {
		setCode(data.getPlain("code"));
	} //ShellComponent()

	/**
	 * The code to be executed in a shell.
	 */
	private String code;

	/**
	 * Gets the code to be executed in a shell.
	 * @return the code to be executed in a shell.
	 * @hibernate.property  column="CODE" length="10000000"
     * @javabean.property     displayName="Code"
     *                        hidden="false"
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code to be executed in a shell.
	 * @param code the code to be executed in a shell.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * The output resulting from running the shell code.
	 */
	private String outputResult;

	/**
	 * Gets the output resulting from running the shell code.
	 * @return the output resulting from running the shell code.
	 * @hibernate.property column="OUTPUT_RESULT" length="10000000"
     * @javabean.property     displayName="Output Result"
     *                        hidden="false"
	 */
	public String getOutputResult() {
		return outputResult;
	}

	/**
	 * Sets the output result of running the shell code.
	 * @param outputResult the output result of running the shell code.
	 */
	public void setOutputResult(String outputResult) {
		this.outputResult = outputResult;
	}

	/**
	 * The error output resulting from running the shell code.
	 */
	private String errorResult;

	/**
	 * Gets the error output resulting from running the shell code.
	 * @return the error output resulting from running the shell code.
	 * @hibernate.property column="ERROR_RESULT" length="10000000"
     * @javabean.property     displayName="Error Result"
     *                        hidden="false"
	 */
	public String getErrorResult() {
		return errorResult;
	}

	/**
	 * Sets the error output result of running the shell code.
	 * @param errorResult the error output result of running the shell code.
	 */
	public void setErrorResult(String errorResult) {
		this.errorResult = errorResult;
	}
	
	/**
	 * Executes the shell component.
	 * 
	 * The default working directory in which the shell code gets executed
	 * is the system temp directory.
	 *
	 * @throws Exception if there was any error output or if there is any
	 *                          error executing the shell code.
	 */
	public NexusServiceData run() throws Exception
	{
		String code = this.getCode();
		if(code == null)
		{
			return null;
		}
		
		try
		{
			// Initialize the variables that depend on whether we are in Unix or Windows.
			boolean isUnix = (File.separatorChar == '/');
			String cmd = (isUnix ? "bash" : "cmd");
			String exit = (isUnix ? "exit" : "exit");
			String newlineW = "\r\n";
			String newlineU = "\n";
			
			// Start the process in the temp directory.
			Runtime rt = Runtime.getRuntime();
			String tempDirPath = System.getProperty("java.io.tmpdir");
			File tempDir = new File(tempDirPath);
			Process process = rt.exec(cmd, null, tempDir);
			
			// Start reading from the output and error streams immediately so we don't deadlock.
			StreamGobbler errGobbler = new StreamGobbler(process.getErrorStream(), false);
			StreamGobbler outGobbler = new StreamGobbler(process.getInputStream(), false);
			errGobbler.start();
			outGobbler.start();
			
			// Modify the code according to whether we are in Unix or Windows.
			if(isUnix)
			{
				code = code.trim().replaceAll("\r\n", "\n") + newlineU + exit + newlineU;
			}
			else
			{
				code = code.trim().replaceAll("([^\r])\n", "$1\r\n") + newlineW + exit + newlineW;
			}
			
			// Execute the shell script line by line.
			BufferedOutputStream os = null;

			try {
				os = new BufferedOutputStream(process.getOutputStream());
				String[] lines = code.split(newlineW);
				for(int i = 0; i < lines.length; i++)
				{
					String line = lines[i];
					os.write(line.getBytes());
					os.write(newlineW.getBytes());
					os.flush();
				}
			}
			finally {
//				Closer.close(os);
			}
			
			// Wait for the shell script and stream consumers to finish.
			process.waitFor();
			errGobbler.join();
			outGobbler.join();
			
			// Retrieve the output stream contents.
			this.setErrorResult(errGobbler.getStreamContents());
			this.setOutputResult(outGobbler.getStreamContents());
			
			// If an error occurred, throw an exception (so our status gets set to ERROR).
			String errorResult = getErrorResult();
			if(null != errorResult && errorResult.length() > 0)
			{
				throw new Exception("ShellComponent.run - Error: " + errorResult);
			}
		}
		catch(Throwable thrown)
		{
			throw new Exception(thrown);
		}
		finally {
			NexusServiceData data = new NexusServiceData();
			data.setPlain("code", getCode());
			data.setPlain("error", getErrorResult());
			data.setPlain("output", getOutputResult());
			return data;
		}
	}
}
