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

import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstall;

@SuppressWarnings("restriction")
public class DockerVMType extends StandardVMType {

	@Override
	public String getName() {
		return "Docker Remote JRE";
	}

	@Override
	protected IVMInstall doCreateVMInstall(String id) {
		return new DockerVM(this, id);
	}

	@Override
	public String getVMVersion(File javaHome, File javaExecutable) {
		return super.getVMVersion(javaHome, javaExecutable);
	}

}
