/*******************************************************************************
 * Copyright (c) 2016 Thales Global Services S.A.S.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *  Thales Global Services S.A.S - initial API and implementation
 ******************************************************************************/
package org.polarsys.kitalpha.model.common.scrutiny.contrib.viewpoints.scrutinizes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.polarsys.kitalpha.ad.common.AD_Log;
import org.polarsys.kitalpha.ad.common.utils.URIHelper;
import org.polarsys.kitalpha.ad.integration.sirius.model.SiriusRepresentation;
import org.polarsys.kitalpha.ad.metadata.metadata.Metadata;
import org.polarsys.kitalpha.ad.metadata.metadata.ViewpointUsage;
import org.polarsys.kitalpha.ad.services.manager.ViewpointManager;
import org.polarsys.kitalpha.ad.viewpoint.coredomain.viewpoint.model.RepresentationElement;
import org.polarsys.kitalpha.ad.viewpoint.coredomain.viewpoint.model.Viewpoint;
import org.polarsys.kitalpha.model.common.scrutiny.analyzer.ModelScrutinyException;
import org.polarsys.kitalpha.model.common.scrutiny.analyzer.Scrutineer;
import org.polarsys.kitalpha.model.common.scrutiny.contrib.modelresources.scrutinize.ModelResourcesScrutinizer;
import org.polarsys.kitalpha.model.common.scrutiny.interfaces.IScrutinize;
import org.polarsys.kitalpha.model.common.scrutiny.registry.ModelScrutinyRegistry.RegistryElement;
import org.polarsys.kitalpha.model.common.share.ui.utilities.vp.tree.ViewpointTreeContainer;
import org.polarsys.kitalpha.model.common.share.ui.utilities.vp.tree.helpers.FinderAFViewpointHelper;
import org.polarsys.kitalpha.model.common.share.ui.utilities.vp.tree.helpers.ViewpointsSearcherHelper;

/**
 * @author Faycal Abka
 */
public class UsedAFViewpoints implements IScrutinize<ViewpointTreeContainer, Object> {
	
	private Set<String> usedNsURI = new HashSet<String>();
	private Set<String> usedViewpoints = new HashSet<String>();
	private ViewpointTreeContainer container;
	

	public UsedAFViewpoints() {
	}

	@Override
	public void findIn(EObject eObject) {
		String ePackageURI = FinderAFViewpointHelper.getEPackageNsURI_of(eObject);
		
		if (!usedNsURI.contains(ePackageURI)){
			usedNsURI.add(ePackageURI);
		}
	}

	@Override
	public void findIn(Resource resource) {
		if (!resource.getContents().isEmpty() && resource.getContents().get(0) instanceof Metadata) {
			Metadata root = (Metadata) resource.getContents().get(0);
			for (ViewpointUsage uv : root.getViewpointUsages()) {
				usedViewpoints.add(uv.getVpId());
			}
		}
	}

	@Override
	public ViewpointTreeContainer getAnalysisResult() {
		if (container != null)
			return container;
		
		//initialize container
		computeUsedViewpointsHierarchy();
		return container;
	}
	
	private void computeUsedViewpointsHierarchy(){
		
		org.polarsys.kitalpha.resourcereuse.model.Resource[] allVpResources = ViewpointsSearcherHelper.getAllViewpoints();
		
		Map<String, Collection<String>> viewpointsURIDependencies = ViewpointRelationshipHelper.getUsedRelationship(allVpResources);
		
		usedNsURI = FinderAFViewpointHelper.filterURISet(usedNsURI, viewpointsURIDependencies);
		Map<String, Collection<String>> filtredVpDependencies = FinderAFViewpointHelper.filterURIMap(viewpointsURIDependencies, usedNsURI);
		
		ViewpointTreeBuilder vpTreeBuilder = new ViewpointTreeBuilder();
		
		//Initialize container
		container = vpTreeBuilder.getViewpointTreeContainer(filtredVpDependencies, usedViewpoints);
	}

	@Override
	public Object getFeedbackAnalysisMessages() {
		// Not need to feedback the user. ViewpointTreeContainer is used for this aim
		return null;
	}
	
	public void dispose(){
		container.dispose();
		container = null;
	}

	public Collection<String> getUsedNsURIs() {
		return new ArrayList<String>(usedNsURI);
	}
	
	public static Set<org.polarsys.kitalpha.resourcereuse.model.Resource> lookUp(Resource resource) {
		Set<org.polarsys.kitalpha.resourcereuse.model.Resource> result = new HashSet<org.polarsys.kitalpha.resourcereuse.model.Resource>();
		Scrutineer.startScrutiny(resource);

		Set<String> nsUris = new HashSet<String>();
		Set<String> odesigns = new HashSet<String>();
		
		collectData(nsUris, odesigns);
		
		ResourceSet set = new ResourceSetImpl();
		try {
			for (org.polarsys.kitalpha.resourcereuse.model.Resource res : ViewpointManager.getAvailableViewpoints()) {
				try {
					URI uri = URIHelper.createURI(res);
					Viewpoint vp = (Viewpoint) set.getEObject(uri, true);
					if (vp.getMetamodel() != null) {
						for (EPackage pack : vp.getMetamodel().getModels()) {
							if (pack.getNsURI() != null && nsUris.contains(pack.getNsURI())) 
								result.add(res);
							
						}
					}
					if (vp.getRepresentation() != null) {
						for (RepresentationElement representation : vp.getRepresentation().getRepresentations()) {
							if (representation instanceof SiriusRepresentation) {
								SiriusRepresentation sr = (SiriusRepresentation)representation;
								URI uri2 = EcoreUtil.getURI(sr.getOdesign());
								if (odesigns.contains(uri2.lastSegment()))
									result.add(res);
							}
						}
					}
				} catch (Exception e) {
					ViewpointManager.pinError(res, e);

				}
			}
		} finally {
			for (org.eclipse.emf.ecore.resource.Resource r : set.getResources()) {
				r.unload();
			}
			set.getResources().clear();
		}

		return result;
	}

	private static void collectData(Set<String> nsUris, Set<String> odesigns) {
		try {
			// look for odesign 
			RegistryElement element = Scrutineer.getRegistryElement("org.polarsys.kitalpha.model.common.scrutiny.contrib.unknownresources.scrutiny");
			for (IScrutinize iFinder : element.getFinders()) {
				if (iFinder instanceof ModelResourcesScrutinizer)
				{
					ModelResourcesScrutinizer resources = (ModelResourcesScrutinizer)iFinder;
					for (URI uri :resources.getAnalysisResult().getAllModelResourceURI()) {
						if ("odesign".equals(uri.fileExtension()))
							odesigns.add(uri.lastSegment());
					}
				}
			}
			RegistryElement vpElement = Scrutineer.getRegistryElement("org.polarsys.kitalpha.model.common.scrutiny.contrib.scrutiny.viewpoints");
			for (IScrutinize iFinder : vpElement.getFinders()) {
				if (iFinder instanceof UsedAFViewpoints)
				{
					UsedAFViewpoints afFinder = (UsedAFViewpoints)iFinder;
					nsUris.addAll(afFinder.getUsedNsURIs());
				}
			}

			
		} catch (ModelScrutinyException e) {
			AD_Log.getDefault().logWarning(e);
		}
	}

}
