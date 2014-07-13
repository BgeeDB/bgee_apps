package org.bgee.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parameters {
	// The next serial number to be assigned
	protected static int nextSerialNum = 0;
	private static ThreadLocal<?> serialNum = new ThreadLocal<Object>() {
		@Override
		protected synchronized Object initialValue() {
			return new Integer(nextSerialNum++);
		}
	};

	private static HashMap<Integer, Parameters> parametersPool = new HashMap<Integer, Parameters>();

	final private String bgeeYAMLpath = "bgee" + File.separator + "META-INF"
			+ File.separator + "Parameters.yaml";

	// Server section
	protected String serverRoot;
	protected String homePage;
	protected String bgeeRoot;
	protected String downloadRootDirectory;
	protected String cssFilesRootDirectory;
	protected String javascriptFilesRootDirectory;
	protected String imagesRootDirectory;
	protected String tomcatLogs;

	// TopOBO section
	/**
	 * Path of RScript Executable file which is used by <code>RCaller</code> to
	 * execute the R code.
	 * 
	 * @see model.TopOBO.TopOBOAnalyses
	 */
	private String topOBORScriptExecutable;

	/**
	 * Current working directory of <code>R</code>, where all the other files
	 * required for the processing of <code>model.TopOBO.TopOBOAnalyses</code>
	 * are kept.
	 * <p>
	 * This directory should only be used with <code>RCaller</code>, to set the
	 * working directory of the <code>R</code>. If you need to use the directory
	 * to access a result file or get the directory to write TopOBO result
	 * files, use <code>#topOBOResultsDirectory</code>
	 * <p>
	 * If you want to link to such a file using a URL, you must use
	 * <code>#topOBOResultsUrlDirectory</code>.
	 * 
	 * @see #topOBOResultsWritingDirectory
	 * @see #topOBOResultsUrlDirectory
	 * @see model.TopOBO.TopOBOAnalyses
	 */
	private String topOBORCallerWorkingDirectory;

	/**
	 * File which contains the additional modified topGO R functions used by
	 * topOBO to perform the analyses.
	 * 
	 * @see model.TopOBO.TopOBOAnalyses
	 */
	private String topOBORFunctionsFile;

	/**
	 * Directory to store outputs of the TopOBO analyses that should be kept to
	 * be retrieved in case the same TopOBO query is performed again.
	 * <p>
	 * This directory has to be used when writing files. If you want to link to
	 * such a file using a URL, you must use
	 * <code>#topOBOResultsUrlDirectory</code>.
	 * <p>
	 * If you want to set the working directory for <code>R</code>, use
	 * <code>#topOBORCallerWorkingDirectory</code>
	 * 
	 * @see #topOBOResultsUrlDirectory
	 * @see #topOBORCallerWorkingDirectory
	 * @see model.TopOBO.TopOBOAnalyses
	 */
	private String topOBOResultsWritingDirectory;
	/**
	 * The path to be used in URL to link to a file stored in the TopOBO result
	 * directory (see <code>#topOBOResultsDirectory</code>).
	 * <p>
	 * This path has to be used to link to a TopOBO result file. If you want to
	 * get the directory to write TopOBO result files, use
	 * <code>#topOBOResultsDirectory</code>
	 * <p>
	 * If you want to set the working directory for <code>R</code>, use
	 * <code>#topOBORCallerWorkingDirectory</code>
	 * 
	 * @see #topOBOResultsWritingDirectory
	 * @see #topOBORCallerWorkingDirectory
	 * @see model.TopOBO.TopOBOAnalyses
	 */
	private String topOBOResultsUrlDirectory;

	/**
	 * Directory where the R scripts used by the TopOBOAnalyses class are stored
	 * 
	 * @see model.TopOBO.TopOBOAnalyses
	 */
	private String topOBORScriptsDirectory;

	// Email section
	protected String mailingList;
	protected String fromEmail;
	protected String protocolEmail;
	protected String hostEmail;
	protected String userEmail;
	protected String passwordEmail;

	// Database section
	protected String dblogin;
	protected String dbpassword;
	protected String dbase;
	protected int dbport;
	protected String dbhost;
	protected int debugLevel;

	/**
	 * <code>String</code> indicating the directory where are stored serialized
	 * <code>RequestParameters</code> objects.
	 * 
	 * @see controller.RequestParameters
	 */
	private String requestParametersStorageDirectory;

	private static synchronized int getIdentifier() {
		return ((Integer) (serialNum.get())).intValue();
	}

	public static synchronized Parameters removeFromParametersPool() {
		return Parameters.parametersPool.remove(new Integer(Parameters
				.getIdentifier()));
	}

	private static void putIntoParametersPool(Parameters parameters) {
		Parameters.parametersPool.put(new Integer(Parameters.getIdentifier()),
				parameters);
	}

	public static synchronized Parameters getParameters() {
		int identifier = Parameters.getIdentifier();
		Parameters parameters = Parameters.parametersPool.get(new Integer(
				identifier));

		if (parameters == null) {
			parameters = new Parameters();
			Parameters.putIntoParametersPool(parameters);
		}

		return parameters;
	}

	private Parameters() {
		// Default if YAML file is missing or unreachable!
		this.passwordEmail = "";
		this.userEmail = "";
		this.hostEmail = "smtp.unil.ch";
		this.protocolEmail = "smtp";
		this.fromEmail = "Bgee@isb-sib.ch";
		this.mailingList = ""; // Need non-spam headers

		this.serverRoot = "http://bgee.unil.ch/";
		this.bgeeRoot = "bgee/bgee";
		this.downloadRootDirectory = "ftp://lausanne.isb-sib.ch/pub/databases/Bgee/";
		this.cssFilesRootDirectory = "bgee/css/bgee_v12_1/";
		this.javascriptFilesRootDirectory = "bgee/javascript/bgee_v12_1/";
		this.imagesRootDirectory = "bgee/images/";
		this.homePage = "bgee/bgee";
		this.tomcatLogs = "logs" + File.separator;
		// this.tomcatLogs = "";

		this.setTopOBORScriptExecutable("/usr/bin/Rscript");
		this.setTopOBORCallerWorkingDirectory("/home/bgee/webapps/TopOBOFiles/results");
		this.setTopOBORFunctionsFile("/home/bgee/webapps/TopOBOFiles/R_scripts/topOBO_functions.R");
		this.setTopOBORScriptsDirectory("webapps/bgee/TopOBOFiles/R_scripts/");
		this.setTopOBOResultsWritingDirectory("webapps/bgee/TopOBOFiles/results/");
		this.setTopOBOResultsUrlDirectory("bgee/TopOBOFiles/results/");

		this.dblogin = "bgee";
		this.dbpassword = "bgee";
		this.dbase = "bgee_v12";
		this.dbport = 3306;
		this.dbhost = "127.0.0.1";
		this.debugLevel = 1;

		this.setRequestParametersStorageDirectory("webapps/bgee/bgee_request_parameters/");

		// From YAML parameter file
		try {
			Scanner openYAML = new Scanner(new File("webapps" + File.separator
					+ this.bgeeYAMLpath)); // For Tomcat6
			this.readParameterFile(openYAML);
		} catch (FileNotFoundException e) {
			try {
				Scanner openYAML = new Scanner(new File(this.bgeeYAMLpath)); // For
																				// Tomcat5.5
				this.readParameterFile(openYAML);
			} catch (FileNotFoundException f) {
				// System.err.println("webapps" + File.separator +
				// this.bgeeYAMLpath + " or
				// " + this.bgeeYAMLpath + " canNOT be reached");
				// e.printStackTrace();
				// f.printStackTrace();
				// these are too large in log output file catalina.out
			}
		}
	}

	public String getServerRoot() {
		return this.serverRoot;
	}

	public String getHomePage() {
		return this.getServerRoot() + this.homePage;
	}

	public String getBgeeRoot() {
		return this.getServerRoot() + this.bgeeRoot;
	}

	public String getDownloadRootDirectory() {
		return this.downloadRootDirectory;
	}

	public String getCssFilesRootDirectory() {
		return this.getServerRoot() + this.cssFilesRootDirectory;
	}

	public String getJavascriptFilesRootDirectory() {
		return this.getServerRoot() + this.javascriptFilesRootDirectory;
	}

	public String getImagesRootDirectory() {
		return this.getServerRoot() + this.imagesRootDirectory;
	}

	public String getTomcatLogs() {
		return this.tomcatLogs;
	}

	public String getMailingList() {
		return this.mailingList;
	}

	public String getFromEmail() {
		return this.fromEmail;
	}

	public String getProtocolEmail() {
		return this.protocolEmail;
	}

	public String getHostEmail() {
		return this.hostEmail;
	}

	public String getUserEmail() {
		return this.userEmail;
	}

	public String getPasswordEmail() {
		return this.passwordEmail;
	}

	public String getDBlogin() {
		return this.dblogin;
	}

	public String getDBpassword() {
		return this.dbpassword;
	}

	public String getDBase() {
		return this.dbase;
	}

	public int getDBport() {
		return this.dbport;
	}

	public String getDBhost() {
		return this.dbhost;
	}

	public int getDebugLevel() {
		return this.debugLevel;
	}

	/**
	 * @return the requestParametersStorageDirectory
	 * @see #requestParametersStorageDirectory
	 */
	public String getRequestParametersStorageDirectory() {
		return this.requestParametersStorageDirectory;
	}

	/**
	 * @param requestParametersStorageDirectory
	 *            the requestParametersStorageDirectory to set
	 * @see #requestParametersStorageDirectory
	 */
	public void setRequestParametersStorageDirectory(
			String requestParametersStorageDirectory) {
		this.requestParametersStorageDirectory = requestParametersStorageDirectory;
	}

	private void readParameterFile(Scanner openYAML) {
		Pattern paramLine = Pattern.compile("(\\w+)\\s*:\\s*(.*)");
		while (openYAML.hasNextLine()) {
			Matcher paramArg = paramLine.matcher(openYAML.nextLine());
			if (paramArg.matches()) {
				if (paramArg.group(1).trim().equals("fromEmail")) {
					this.fromEmail = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim().equals("passwordEmail")) {
					this.passwordEmail = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim().equals("userEmail")) {
					this.userEmail = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim().equals("hostEmail")) {
					this.hostEmail = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim().equals("protocolEmail")) {
					this.protocolEmail = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim().equals("mailingList")) {
					this.mailingList = paramArg.group(2).replaceAll("\"", "")
							.trim();
				}

				else if (paramArg.group(1).trim().equals("serverRoot")) {
					this.serverRoot = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim().equals("bgeeRoot")) {
					this.bgeeRoot = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim()
						.equals("downloadRootDirectory")) {
					this.downloadRootDirectory = paramArg.group(2)
							.replaceAll("\"", "").trim();
				} else if (paramArg.group(1).trim()
						.equals("cssFilesRootDirectory")) {
					this.cssFilesRootDirectory = paramArg.group(2)
							.replaceAll("\"", "").trim();
				} else if (paramArg.group(1).trim()
						.equals("javascriptFilesRootDirectory")) {
					this.javascriptFilesRootDirectory = paramArg.group(2)
							.replaceAll("\"", "").trim();
				} else if (paramArg.group(1).trim()
						.equals("imagesRootDirectory")) {
					this.imagesRootDirectory = paramArg.group(2)
							.replaceAll("\"", "").trim();
				} else if (paramArg.group(1).trim().equals("homePage")) {
					this.homePage = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim().equals("tomcatLogs")) {
					this.tomcatLogs = System.getProperty("catalina.base")
							+ File.separator
							+ paramArg.group(2).replaceAll("\"", "").trim()
							+ File.separator;
				} else if (paramArg.group(1).trim()
						.equals("topOBOTempDirectory")) {
					this.setTopOBORFunctionsFile(paramArg.group(2)
							.replaceAll("\"", "").trim());
				} else if (paramArg.group(1).trim()
						.equals("topOBORScriptsDirectory")) {
					this.setTopOBORScriptsDirectory(paramArg.group(2)
							.replaceAll("\"", "").trim());
				} else if (paramArg.group(1).trim()
						.equals("topOBORResultsDirectory")) {
					this.setTopOBOResultsWritingDirectory(paramArg.group(2)
							.replaceAll("\"", "").trim());
				} else if (paramArg.group(1).trim()
						.equals("topOBOResultsUrlDirectory")) {
					this.setTopOBOResultsUrlDirectory(paramArg.group(2)
							.replaceAll("\"", "").trim());
				}

				else if (paramArg.group(1).trim().equals("dblogin")) {
					this.dblogin = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim().equals("dbpassword")) {
					this.dbpassword = paramArg.group(2).replaceAll("\"", "")
							.trim();
				} else if (paramArg.group(1).trim().equals("dbase")) {
					this.dbase = paramArg.group(2).replaceAll("\"", "").trim();
				} else if (paramArg.group(1).trim().equals("dbport")) {
					this.dbport = Integer.parseInt(paramArg.group(2)
							.replaceAll("\"", "").trim());
				} else if (paramArg.group(1).trim().equals("dbhost")) {
					this.dbhost = paramArg.group(2).replaceAll("\"", "").trim();
				} else if (paramArg.group(1).trim().equals("debugLevel")) {
					this.debugLevel = Integer.parseInt(paramArg.group(2)
							.replaceAll("\"", "").trim());
				}

				else if (paramArg.group(1).trim()
						.equals("requestParametersStorageDirectory")) {
					this.setRequestParametersStorageDirectory(paramArg.group(2)
							.replaceAll("\"", "").trim());
				}
			}
		}

		openYAML.close();
	}

	/**
	 * Get the path of the RScript Executable file used by <code>RCaller</code>
	 * 
	 * @return the rScriptExecutable
	 * @see #topOBORScriptExecutable
	 */
	public String getTopOBORScriptExecutable() {
		return this.topOBORScriptExecutable;
	}

	/**
	 * Set the path of the RScript Executable file used by <code>RCaller</code>
	 * 
	 * @param rScriptExecutable
	 *            the rScriptExecutable to set
	 * @see #topOBORScriptExecutable
	 */
	public void setTopOBORScriptExecutable(String topOBORScriptExecutable) {
		this.topOBORScriptExecutable = topOBORScriptExecutable;
	}

	/**
	 * Get the path of the current working directory of <code>RCaller</code>
	 * 
	 * @return the topOBORCallerWorkingDirectory
	 * @see #topOBORCallerWorkingDirectory
	 */
	public String getTopOBORCallerWorkingDirectory() {
		return this.topOBORCallerWorkingDirectory;
	}

	/**
	 * Set the path of the current working directory of <code>RCaller</code>
	 * 
	 * @param topOBORCallerWorkingDirectory
	 *            the topOBORCallerWorkingDirectory to set
	 * @see #topOBORCallerWorkingDirectory
	 */
	public void setTopOBORCallerWorkingDirectory(
			String topOBORCallerWorkingDirectory) {
		this.topOBORCallerWorkingDirectory = topOBORCallerWorkingDirectory;
	}

	/**
	 * @return the topOBORFunctionsFile
	 * @see #topOBORFunctionsFile
	 */
	public String getTopOBORFunctionsFile() {
		return this.topOBORFunctionsFile;
	}

	/**
	 * @param topOBORFunctionsFile
	 *            the topOBORFunctionsFile to set
	 * @see #topOBORFunctionsFile
	 */
	public void setTopOBORFunctionsFile(String topOBORFunctionsFile) {
		this.topOBORFunctionsFile = topOBORFunctionsFile;
	}

	/**
	 * @return the topOBORScriptsDirectory
	 * @see #topOBORScriptsDirectory
	 */
	public String getTopOBORScriptsDirectory() {
		return this.topOBORScriptsDirectory;
	}

	/**
	 * @param topOBORScriptsDirectory
	 *            the topOBORScriptsDirectory to set
	 * @see #topOBORScriptsDirectory
	 */
	public void setTopOBORScriptsDirectory(String topOBORScriptsDirectory) {
		this.topOBORScriptsDirectory = topOBORScriptsDirectory;
	}

	/**
	 * @return the topOBOResultsDirectory
	 * @see #topOBOResultsWritingDirectory
	 */
	public String getTopOBOResultsWritingDirectory() {
		return this.topOBOResultsWritingDirectory;
	}

	/**
	 * @param topOBOResultsDirectory
	 *            the topOBOResultsDirectory to set
	 * @see #topOBOResultsWritingDirectory
	 */
	public void setTopOBOResultsWritingDirectory(String topOBOResultsDirectory) {
		this.topOBOResultsWritingDirectory = topOBOResultsDirectory;
	}

	/**
	 * Get the path to be used in URL to link to a file stored in the TopOBO
	 * result directory.
	 * 
	 * @return the topOBOResultsStorageDirectory
	 * @see #topOBOResultsUrlDirectory
	 */
	public String getTopOBOResultsUrlDirectory() {
		return this.topOBOResultsUrlDirectory;
	}

	/**
	 * Set the path to be used in URL to link to a file stored in the TopOBO
	 * result directory.
	 * 
	 * @param topOBOResultsStorageDirectory
	 *            the topOBOResultsStorageDirectory to set
	 * @see #topOBOResultsUrlDirectory
	 */
	public void setTopOBOResultsUrlDirectory(
			String topOBOResultsStorageDirectory) {
		this.topOBOResultsUrlDirectory = topOBOResultsStorageDirectory;
	}

}
