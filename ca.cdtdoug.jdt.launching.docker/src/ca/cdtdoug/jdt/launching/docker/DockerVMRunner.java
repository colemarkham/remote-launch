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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.launching.StandardVMRunner;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

@SuppressWarnings("restriction")
public class DockerVMRunner extends StandardVMRunner {

	public DockerVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}

	@Override
	protected String constructProgramString(VMRunnerConfiguration config) throws CoreException {
		return "docker";
	}

	@Override
	protected String[] prependJREPath(String[] env) {
		// Super does some crazy stuff if you're on Mac
		return env;
	}
	
	@Override
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		DockerVM vm = (DockerVM) fVMInstance;
		return super.exec(vm.fixCmdLine(cmdLine), workingDirectory);
	}
	
	@Override
	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		DockerVM vm = (DockerVM) fVMInstance;
		return super.exec(vm.fixCmdLine(cmdLine), workingDirectory, envp);
	}

}
