/*******************************************************************************
 * Copyright (c) 2014 Thales Global Services S.A.S.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *  Thales Global Services S.A.S - initial API and implementation
 ******************************************************************************/
package org.polarsys.kitalpha.model.common.scrutiny.contrib.unknownreferences.scrutinizes;

import java.util.Set;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xml.type.AnyType;

/**
 * @author Faycal Abka
 */
public class MissingEPackageResultWrapper {
	
	private Set<EPackage> ePackages;
	private Set<XMLResource> xmlResources;
	private Set<AnyType> anyTypes;
	
	
	public MissingEPackageResultWrapper(Set<EPackage> ePackages,
			Set<XMLResource> xmlResources, Set<AnyType> anyTypes) {
		super();
		this.ePackages = ePackages;
		this.xmlResources = xmlResources;
		this.anyTypes = anyTypes;
	}


	public Set<EPackage> getePackages() {
		return ePackages;
	}


	public Set<XMLResource> getXmlResources() {
		return xmlResources;
	}


	public Set<AnyType> getAnyTypes() {
		return anyTypes;
	}
}
