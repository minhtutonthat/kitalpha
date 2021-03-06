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

package org.polarsys.kitalpha.ad.viewpoint.dsl.cs.text.ui.builder;

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.builder.builderState.MarkerUpdaterImpl;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.ui.validation.IResourceUIValidatorExtension;
import org.polarsys.kitalpha.ad.viewpoint.dsl.cs.text.resources.ResourceHelper;

import com.google.inject.Inject;

/**
 * 
 * @author Amine Lajmi
 *
 */
@SuppressWarnings("restriction")
public class PeriodicMarkerUpdater extends MarkerUpdaterImpl {

	@Inject
	private IResourceServiceProvider.Registry resourceServiceProviderRegistry;
	
	@Override
	protected IResourceUIValidatorExtension getResourceUIValidatorExtension(URI uri) {
		IResourceServiceProvider provider = null;
		//handle periodic file extensions
		if (ResourceHelper.hasPeriodicFileExtension(uri)) {
			Map<String, Object> extensionToFactoryMap = resourceServiceProviderRegistry.getExtensionToFactoryMap();
			provider = ResourceHelper.getServiceProvider(uri, extensionToFactoryMap, ResourceHelper.getFileExtension(uri));
		} else {
			provider =  resourceServiceProviderRegistry.getResourceServiceProvider(uri);
		}		
		if (provider != null) {
			return provider.get(IResourceUIValidatorExtension.class);
		}
		return null;
	}
}
