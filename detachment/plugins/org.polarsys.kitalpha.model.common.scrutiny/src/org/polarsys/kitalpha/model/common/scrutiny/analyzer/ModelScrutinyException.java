/*******************************************************************************
 * Copyright (c) 2014, 2017 Thales Global Services S.A.S.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *  Thales Global Services S.A.S - initial API and implementation
 ******************************************************************************/
package org.polarsys.kitalpha.model.common.scrutiny.analyzer;

/**
 * @author Faycal Abka
 */
public class ModelScrutinyException extends Exception {

	private static final long serialVersionUID = 7014705563026180357L;
	
	public ModelScrutinyException(String message){
		super(message);
	}
}
