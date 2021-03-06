/*******************************************************************************
 * Copyright (c) 2014 Thales Global Services S.A.S.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *   Thales Global Services S.A.S - initial API and implementation
 ******************************************************************************/

package org.polarsys.kitalpha.ad.viewpoint.dsl.services.preference.helper;

/**
 * @author Boubekeur Zendagui
 */

public abstract class ViewpointDslPreferenceConstant {
	
	/** This preference allows to set a default target application to use in
	 * viewpoint creation and reverse wizards */
	public static final String VP_TA__DEFAULT = "DefaultViewpointTA";
	
	/** This is the default target application */
	public static final String VP_TA__DEFAULT__DEFAULT_VALUE = "EMF";
	
	/** This preference allows the user to choose between the value of the target 
	 * application root project name or value he provided **/
	public static final String VP_ROOT_PROJECT_NAME__USE_TA = "UseTAViewpointRootProjectName";
	
	/** The customized value of root project name **/
	public static final String VP_ROOT_PROJECT_NAME__VALUE = "ViewpointRootProjectName";
	
	/** The default value of root project name preference **/
	public static final String VP_ROOT_PROJECT_NAME__DEFAULT_VALUE = "org.polarsys.kitalpha.viewpoint";
}
