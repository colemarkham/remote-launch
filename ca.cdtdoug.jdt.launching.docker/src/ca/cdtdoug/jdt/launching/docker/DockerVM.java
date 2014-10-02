/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Doug Schaefer (QNX) - initial implementation
 *******************************************************************************/
package ca.cdtdoug.jdt.launching.docker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;

@SuppressWarnings("restriction")
public class DockerVM extends AbstractVMInstall {

	private String version;

	public DockerVM(IVMInstallType type, String id) {
		super(type, id);
	}

	@Override
	public String getJavaVersion() {
		if (version == null) {
			// default to 1.7 if we don't know for sure
			version = "1.7";
			File releaseFile = new File(getInstallLocation(), "release");
			if (releaseFile.exists()) {
				Properties releaseProps = new Properties();
				try {
					releaseProps.load(new FileInputStream(releaseFile));
					String releaseVersion = releaseProps.getProperty("JAVA_VERSION");
					if (releaseVersion != null) {
						// strip the quotes off this thing
						version = releaseVersion.substring(1, releaseVersion.length() - 1);
					}
				} catch (IOException e) {
					Activator.log(e);
				}
			}
		}
		return version;
	}

	/**
	 * Returns the java executable for this VM or <code>null</code> if cannot be found
	 * 
	 * @return executable for this VM or <code>null</code> if none
	 */
	File getJavaExecutable() {
		File installLocation = getInstallLocation();
		if (installLocation != null) {
			return StandardVMType.findJavaExecutable(installLocation);
		}
		return null;
	}    

	@Override
	public IVMRunner getVMRunner(String mode) {
		if (ILaunchManager.RUN_MODE.equals(mode)) {
			return new DockerVMRunner(this);
		} else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
			return new DockerVMDebugger(this);
		}
		return null;
	}

	String[] fixCmdLine(String[] cmdLine) throws CoreException {
		// Options:
		// -t hostpath:dockerpath - translate host path to path inside docker container
		// -h ip - IP address back to us for the debugger to call home
		// -j java - an alternative name for java exe
		
		// Usage:
		// docker [options] -- [docker command] -- [java command]

		// The standard VM may have inserted the debug options in with our options
		// Need to move them to after the java command.
		List<String> newCmdLine = new ArrayList<>(cmdLine.length);
		List<String> debugOptions = new ArrayList<>();
		List<String> hostPaths = new ArrayList<>();
		List<String> dockerPaths = new ArrayList<>();
		String ipHome = null;
		String java = "java";
		
		// Pick off our options until the first --
		int i;
		for (i = 1; i < cmdLine.length; ++i) {
			String arg = cmdLine[i];
			if (arg.equals("-t")) {
				if (++i < cmdLine.length) {
					String trans = cmdLine[i];
					int n = trans.lastIndexOf(':');
					if (n > 0 && n < trans.length() - 1) {
						hostPaths.add(trans.substring(0, n));
						dockerPaths.add(trans.substring(n + 1));
					}
				}
			} else if (arg.equals("-h")) {
				if (++i < cmdLine.length) {
					ipHome = cmdLine[i];
				}
			} else if (arg.equals("--")) {
				break;
			} else {
				debugOptions.add(arg);
			}
		}

		// need to know the ip address to call home
		if (ipHome == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "No ip address specified in docker launch"));
		}

		// Now start adding the docker commands until the next --
		newCmdLine.add(cmdLine[0]);
		for (++i; i < cmdLine.length; ++i) {
			String arg = cmdLine[i];
			if (arg.equals("--")) {
				break;
			} else {
				newCmdLine.add(arg);
			}
		}

		// Finally the java command
		newCmdLine.add(java);
		for (String arg : debugOptions) {
			if (arg.startsWith("-agentlib:")) {
				// replace localhost with iphome
				newCmdLine.add(arg.replace("localhost", ipHome));
			} else {
				newCmdLine.add(arg);
			}
		}
		
		for (++i; i < cmdLine.length; ++i) {
			String arg = cmdLine[i];
			if (arg.equals("-classpath")) {
				newCmdLine.add(arg);
				if (++i < cmdLine.length) {
					String[] paths = cmdLine[i].split(File.pathSeparator);
					for (int j = 0; j < paths.length; ++j) {
						String path = paths[j];
						for (int k = 0; k < hostPaths.size(); ++k) {
							String hostPath = hostPaths.get(k);
							if (path.startsWith(hostPath)) {
								paths[j] = dockerPaths.get(k) + path.substring(hostPath.length());
							}
						}
					}
					newCmdLine.add(String.join(File.pathSeparator, paths));
				}
			} else {
				newCmdLine.add(arg);
			}
		}

		Activator.log(new Status(IStatus.INFO, Activator.getId(), String.join(" ", newCmdLine)));

		return newCmdLine.toArray(new String[newCmdLine.size()]);
	}

}
