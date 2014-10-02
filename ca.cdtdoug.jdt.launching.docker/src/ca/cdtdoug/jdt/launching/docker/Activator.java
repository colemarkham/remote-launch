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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	private static BundleContext context;
	private static Plugin plugin;

	static BundleContext getContext() {
		return context;
	}

	static Plugin getPlugin() {
		return plugin;
	}

	static String getId() {
		return context.getBundle().getSymbolicName();
	}

	static void log(Exception e) {
		plugin.getLog().log(new Status(IStatus.ERROR, getId(), e.getLocalizedMessage(), e));
	}

	static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		plugin = this;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		plugin = null;
	}

}
