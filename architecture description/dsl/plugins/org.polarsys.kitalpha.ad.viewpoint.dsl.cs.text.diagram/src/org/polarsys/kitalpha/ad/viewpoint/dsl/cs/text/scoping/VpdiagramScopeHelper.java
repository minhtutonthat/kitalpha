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
package org.polarsys.kitalpha.ad.viewpoint.dsl.cs.text.scoping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.business.api.componentization.ViewpointRegistry;
import org.eclipse.sirius.viewpoint.description.Group;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.commondata.AbstractAssociation;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.commondata.AbstractClass;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.commondata.CommondataFactory;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.commondata.ExternalAssociation;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.commondata.ExternalClass;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.commondata.LocalAssociation;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.commondata.LocalClass;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractSuperClass;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.Class;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.ExternalSuperClass;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.LocalClassAssociation;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.LocalSuperClass;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdiagram.AbstractNode;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdiagram.BorderedNode;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdiagram.Container;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdiagram.ContainerChildren;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdiagram.Diagram;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdiagram.EdgeDomainAssociation;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdiagram.MappingSet;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdiagram.Node;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdiagram.NodeDomainElement;
import org.polarsys.kitalpha.ad.viewpoint.dsl.cs.text.registry.DataWorkspaceEPackage;


/**
 * TODO Review the code (clean, organization...)
 * @author Faycal Abka
 *
 */
public class VpdiagramScopeHelper {

	
	/*
	 * ***********************************
	 * 		External Associations
	 * ***********************************
	 */
	public static boolean selectExternalAssociation(EObject context,
			IEObjectDescription d) {		
		
		if (d.getEObjectOrProxy() instanceof EReference){
			if (context instanceof ExternalAssociation){
				ExternalAssociation externalAssociation = (ExternalAssociation)context;
				NodeDomainElement nde = getNodeDomainElementFrom(externalAssociation);
				
				
				if (nde == null && externalAssociation.eContainer() instanceof EdgeDomainAssociation){
					return d.getEObjectOrProxy() instanceof EReference;
				}
				
				EObject ndeContainer = nde.eContainer();
				if (ndeContainer instanceof Container || ndeContainer instanceof Node || ndeContainer instanceof BorderedNode){

					AbstractClass domain_class = getDomain_class(nde); 
					AbstractClass container_domain_class = getDomainContainerOfContainerOfElement(nde);

					if (domain_class == null){
						return d.getEObjectOrProxy() instanceof EReference;
					}

					if (container_domain_class == null){
						//With import
						EClass eClass = getDomainContainerOfContainerOfElementExternal(nde);
						
						if (domain_class instanceof ExternalClass){
							ExternalClass externalDomain_class = (ExternalClass)domain_class;
							return handleExternalExternalAssociation(d, externalDomain_class.getClass_(), eClass);
						}
						
						if (domain_class instanceof LocalClass){
							LocalClass localDomain_class = (LocalClass)domain_class;
							return handleExternalLocalAssociation(d, localDomain_class.getClass_(), eClass);
						}
						
						return d.getEObjectOrProxy() instanceof EReference;
					}

					if (container_domain_class instanceof ExternalClass){

						ExternalClass externalContainer_domain_class = (ExternalClass)container_domain_class;

						if (domain_class instanceof ExternalClass){
							ExternalClass externalDomain_class = (ExternalClass)domain_class;

							return handleExternalExternalAssociation(d, externalDomain_class, externalContainer_domain_class);
						}

						if (domain_class instanceof LocalClass){
							LocalClass localDomain_class = (LocalClass)domain_class;
							return handExternalLocalAssociation(d, localDomain_class, externalContainer_domain_class);
						}
					}
				}
			}
		}
		return d.getEObjectOrProxy() instanceof EReference;
	}
	
	/*
	 * Naming convention about handler:
	 * 	boolean handle[TypeOfContainerDomainClass][TypeOfCurrentDomainClass]Association(...)
	 * 
	 * TypeOfContainerDomainClass may be: External - Local
	 * TypeOfCurrentDomainClass may be  : External - Local
	 * 
	 */
	private static boolean handExternalLocalAssociation(IEObjectDescription d,
			LocalClass localDomain_class,
			ExternalClass externalContainer_domain_class) {
		
		return handleExternalLocalAssociation(d, localDomain_class.getClass_(), externalContainer_domain_class.getClass_());
	}


	private static boolean handleExternalLocalAssociation(
			IEObjectDescription d, Class localClass, EClass externalEClass) {
		Collection<EClass> SuperEClasses = getExternalSuperClassEClasses(localClass);
		
		boolean result = false;
		for (EClass eClass : SuperEClasses) {
			result |= handleExternalExternalAssociation(d, eClass, externalEClass);
		}
		
		return result;
	}


	private static Collection<EClass> getExternalSuperClassEClasses(
			Class localClass) {
		Collection<EClass> superEClasses = new UniqueEList<EClass>();
		
		EList<AbstractSuperClass> inheritences = localClass.getInheritences();
		
		
		for (AbstractSuperClass superClass : inheritences) {
			if (superClass instanceof ExternalSuperClass){
				ExternalSuperClass esc = (ExternalSuperClass)superClass;
				superEClasses.add(esc.getSuperClass());
			}
			if (superClass instanceof LocalSuperClass){
				LocalSuperClass lsc = (LocalSuperClass)superClass;
				superEClasses.addAll(getExternalSuperClassEClasses(lsc.getSuperClass()));
			}
		}
		
		EList<EClass> extendEClasses = localClass.getExtends();
		
		for (EClass eClass : extendEClasses) {
			EcoreUtil.resolveAll(eClass);
			superEClasses.add(eClass);
			superEClasses.addAll(eClass.getEAllSuperTypes());
		}
		
		return superEClasses;
	}

	private static NodeDomainElement getNodeDomainElementFrom(
			AbstractAssociation association) {
		
		if (association instanceof ExternalAssociation){
			ExternalAssociation externalAssociation = (ExternalAssociation)association;
			EObject eContainer = externalAssociation.eContainer();

			if (eContainer instanceof NodeDomainElement){
				return ((NodeDomainElement)eContainer);
			}
		}
		
		if (association instanceof LocalAssociation){
			LocalAssociation localAssociation = (LocalAssociation)association;
			EObject eContainer = localAssociation.eContainer();

			if (eContainer instanceof NodeDomainElement){
				return ((NodeDomainElement)eContainer);
			}
		}
		
		return null;
	}


	private static boolean handleExternalExternalAssociation(
			IEObjectDescription ieod, 
			ExternalClass externalDomain_class,
			ExternalClass externalContainer_domain_class) {
		
		return handleExternalExternalAssociation(ieod, externalDomain_class.getClass_(), externalContainer_domain_class.getClass_());
	}


	private static boolean handleExternalExternalAssociation(IEObjectDescription ieod, EClass domain_eClass,
			EClass container_eClass) {
		
		if (domain_eClass == null || container_eClass == null)
			return false; 
		
		
		EList<EReference> allReferencesContainerDomain = container_eClass.getEAllReferences();
		EList<EReference> filtredReferences = filterReferenceWithType(allReferencesContainerDomain, domain_eClass);
		EList<EClass> typeRefClasses = new UniqueEList<EClass>();
		EList<String> refTypeNames = new UniqueEList<String>();
		
		for (EReference eReference : filtredReferences) {
			typeRefClasses.add((EClass) eReference.getEType());
			refTypeNames.add(eReference.getEType().getName());
		}
		
		
		EObject r = ieod.getEObjectOrProxy();
		EReference ref = null;
		
		if (r instanceof EReference)
			ref = (EReference)r;
		;
		return (ref != null) && (refTypeNames.contains(ref.getEType().getName())); //(!typeRefClasses.isEmpty()) && (typeRefClasses.contains(ref.getEType()) || refTypeNames.contains(ref.getEType().getName()));// || //(ref != null) && (!filtredReferences.isEmpty()) && (filtredReferences.contains(ref));// &&
	}


	//TODO to change
	private static EList<EReference> filterReferenceWithType(
			EList<EReference> allReferencesContainerDomain, EClass type) {
		
		EList<EReference> filtredReferences = new UniqueEList<EReference>();
		EList<EClass> superTypesOfType = CollectAllSuperTypes(type); //type.getEAllSuperTypes();
		
		for (EReference eReference : allReferencesContainerDomain) {
			EClass eRefType = (EClass) eReference.getEType();
			
			if (type == eReference.getEType()){
				filtredReferences.add(eReference);
				continue;
			}
			EList<EClass> superTypesOfRefType = CollectAllSuperTypes(eRefType);
			
			if (superTypesOfType.contains(eRefType))
				filtredReferences.add(eReference);
			
			for (EClass eClass : superTypesOfRefType) {
				
				for (EClass eClass2 : superTypesOfType) {
					if (eClass.getName().equals(eClass2.getName())){
						filtredReferences.add(eReference);
					}
				}
//				if (superTypesOfType.contains(eClass) || type == eClass)
//					filtredReferences.add(eReference);
			}
			
		}
		
		return filtredReferences;
	}


	private static EList<EClass> CollectAllSuperTypes(EClass type) {
		EList<EClass> result = new UniqueEList<EClass>();
		
		EList<EClass> superTypes = type.getEAllSuperTypes();
		
		result.addAll(superTypes);
		
		for (EClass eClass : superTypes) {
			collectHierarchyTypes(eClass, result);
		}
		
		
		return result;
	}

	private static void collectHierarchyTypes(EClass eClass,
			EList<EClass> result) {
		
		EList<EClass> superTypes = eClass.getEAllSuperTypes();
		
		result.addAll(superTypes);
		for (EClass eClass2 : superTypes) {
			collectHierarchyTypes(eClass2, result);
		}
	}

	private static AbstractClass getDomain_class(NodeDomainElement nde){
		if (nde != null)
			return nde.getDomain_Class();
		return null;
	}
	
	
	//TODO Code need to be cleaned and organization :(
	private static EClass getDomainContainerOfContainerOfElementExternal(NodeDomainElement nde){
		if (nde == null)
			return null;
		
		EObject container = nde.eContainer().eContainer();
		
		if (container instanceof Diagram){
			Diagram diagram = (Diagram)container;
			AbstractClass abstClass = diagram.getThe_domain().getThe_domain();
			if (abstClass instanceof ExternalClass)
				return ((ExternalClass)abstClass).getClass_();
			
		}
		
		if (container instanceof AbstractNode){
			NodeDomainElement nde2 = ((AbstractNode)container).getThe_domain();
			AbstractClass abstClass = getDomain_class(nde2);
			if (abstClass instanceof ExternalClass)
				return ((ExternalClass)abstClass).getClass_();
		}
		
		if (container instanceof ContainerChildren){
			EObject childrenContainer = container.eContainer();
			if (childrenContainer instanceof Node){
				NodeDomainElement nde2 = ((Node)childrenContainer).getThe_domain();
				AbstractClass abstClass = getDomain_class(nde2);
				if (abstClass instanceof ExternalClass)
					return ((ExternalClass)abstClass).getClass_();
			}
			
			if (childrenContainer instanceof Container){
				NodeDomainElement nde2 = ((Container)childrenContainer).getThe_domain();
				if (nde2 == null){
					Container c = ((Container)childrenContainer);
					
					String containerImportDomain = c.getImports().getDomainClass();
					
					String tmp = null;
					if (containerImportDomain.contains(".")){
						tmp = containerImportDomain.substring(containerImportDomain.lastIndexOf(".") + 1);
					} else {
						tmp = containerImportDomain;
					}
					
//					RepresentationDescription rd = (RepresentationDescription) c.getImports().eContainer().eContainer();
//					EList<EPackage> mm = rd.getMetamodel();
					
					Collection<Object> ePackageRegistry = DataWorkspaceEPackage.INSTANCE.values();
					
					for (Object object : ePackageRegistry) {
						if (object instanceof EPackage){
							EPackage ePackage = (EPackage)object;
							
							EClass eClass = (EClass) ePackage.getEClassifier(tmp);
							//return the first class found
							if (eClass != null){
								return eClass;
							}
							
						}
					}					
				}
				
				AbstractClass abstClass = getDomain_class(nde2);
				if (abstClass instanceof ExternalClass)
					return ((ExternalClass)abstClass).getClass_();
			}
			
			if (childrenContainer instanceof BorderedNode){
				NodeDomainElement nde2 = ((BorderedNode)childrenContainer).getThe_domain();
				AbstractClass abstClass = getDomain_class(nde2);
				if (abstClass instanceof ExternalClass)
					return ((ExternalClass)abstClass).getClass_();
			}
		}
		
		if (container instanceof MappingSet){
			EObject mappingSetContainer = container.eContainer();
			if (mappingSetContainer instanceof Diagram){
				Diagram diagram = (Diagram)mappingSetContainer;
				AbstractClass abstClass = diagram.getThe_domain().getThe_domain();
				if (abstClass instanceof ExternalClass)
					return ((ExternalClass)abstClass).getClass_();
			}
		}
		return null;
	}
	
	private static AbstractClass getDomainContainerOfContainerOfElement(NodeDomainElement nde){
		if (nde == null)
			return null;
		
		EObject container = nde.eContainer().eContainer();
		
		if (container instanceof Diagram){
			Diagram diagram = (Diagram)container;
			return diagram.getThe_domain().getThe_domain();
		}
		
		if (container instanceof AbstractNode){
			NodeDomainElement nde2 = ((AbstractNode)container).getThe_domain();
			return getDomain_class(nde2);
		}
		
		if (container instanceof ContainerChildren){
			EObject childrenContainer = container.eContainer();
			if (childrenContainer instanceof Node){
				NodeDomainElement nde2 = ((Node)childrenContainer).getThe_domain();
				return getDomain_class(nde2);
			}
			
			if (childrenContainer instanceof Container){
				NodeDomainElement nde2 = ((Container)childrenContainer).getThe_domain();
//				if (nde2 == null){
//					Container c = ((Container)childrenContainer);
//					c.getImports().eContainer().eContainer();
//					
//					EClass eClass = ((RepresentationDescription) c.getImports().eContainer().eContainer()).getMetamodel().get(0).getEClassifier("HardwareComponent").eClass();
//				}
				
				return getDomain_class(nde2);
			}
			
			if (childrenContainer instanceof BorderedNode){
				NodeDomainElement nde2 = ((BorderedNode)childrenContainer).getThe_domain();
				return getDomain_class(nde2);
			}
		}
		
		if (container instanceof MappingSet){
			EObject mappingSetContainer = container.eContainer();
			if (mappingSetContainer instanceof Diagram){
				Diagram diagram = (Diagram)mappingSetContainer;
				return diagram.getThe_domain().getThe_domain();
			}
		}
		return null;
	}

	
	
	
	/*
	 * ***********************************
	 * 		Local Associations
	 * ***********************************
	 */
	
	public static boolean selectLocalAssociation(EObject context,
			IEObjectDescription d) {

		
		if (d.getEObjectOrProxy() instanceof LocalClassAssociation){
			if (context instanceof NodeDomainElement){
				NodeDomainElement nde =  (NodeDomainElement)context;

				
				EObject ndeContainer = nde.eContainer();
				if (ndeContainer instanceof Container || ndeContainer instanceof Node || ndeContainer instanceof BorderedNode){
					AbstractClass domain_class = getDomain_class(nde);
					AbstractClass container_domain_class = getDomainContainerOfContainerOfElement(nde);

					if (domain_class == null)
						return  d.getEObjectOrProxy() instanceof AbstractAssociation;

					if (container_domain_class == null){
						//With import
						EClass eClass = getDomainContainerOfContainerOfElementExternal(nde);
						
						if (domain_class instanceof ExternalClass){
							ExternalClass externalDomain_class = (ExternalClass)domain_class;
							return handleExternalExternalAssociation(d, externalDomain_class.getClass_(), eClass);
						}
						
						if (domain_class instanceof LocalClass){
							LocalClass localDomain_class = (LocalClass)domain_class;
							return handleExternalLocalAssociation(d, localDomain_class.getClass_(), eClass);
						}
						
						return d.getEObjectOrProxy() instanceof EReference;
					}

					if (container_domain_class instanceof LocalClass){
						LocalClass containerLocalDomain_class = (LocalClass)container_domain_class;

						if (domain_class instanceof LocalClass){
							LocalClass localDomain_class = (LocalClass)domain_class;

							return handleLocalLocalAssociation(d, localDomain_class, containerLocalDomain_class);
						}

						if (domain_class instanceof ExternalClass){
							ExternalClass externalDomainClass = (ExternalClass)domain_class;
							return d.getEObjectOrProxy() instanceof AbstractAssociation; //handleLocalExternalAssociations(d, externalDomainClass, containerLocalDomain_class);
						}
					}
				}
			}
		}

		return d.getEObjectOrProxy() instanceof LocalClassAssociation;
	}

	
	private static boolean handleLocalExternalAssociations(
			IEObjectDescription d, ExternalClass externalDomainClass,
			LocalClass containerLocalDomain_class) {
		
		Collection<EReference> allContainerReferences = getAllEReferencesOf(containerLocalDomain_class);
		filterEReferencesWithType(allContainerReferences, externalDomainClass.getClass_());
		return false;
	}
	
	private static void filterEReferencesWithType(
			Collection<EReference> allContainerReferences, EClass eClazz) {
		
		EList<EClass> superTypes = eClazz.getEAllSuperTypes();
		
		Iterator<EReference> it = allContainerReferences.iterator();
		
		while (it.hasNext()){
			EReference ref = it.next();
			if (!superTypes.contains(ref.getEType())){
				it.remove();
			}
		}
	}

	private static Collection<EReference> getAllEReferencesOf(
			LocalClass containerLocalDomain_class) {
		
		Collection<EReference> localEReferences = getLocalEReferences(containerLocalDomain_class);
		return localEReferences;
	}
	
	private static Collection<EReference> getLocalEReferences(
			LocalClass containerLocalDomain_class) {
		
		Collection<EReference> currentEReferences = getCurrentEReferences(containerLocalDomain_class.getClass_());
		
		EList<AbstractSuperClass> superClasses = containerLocalDomain_class.getClass_().getInheritences();
		
		for (AbstractSuperClass abstractSuperClass : superClasses) {
			if (abstractSuperClass instanceof ExternalSuperClass){
				ExternalSuperClass ec = (ExternalSuperClass)abstractSuperClass;
				currentEReferences.addAll(ec.getSuperClass().getEAllReferences());
			}
			
			if (abstractSuperClass instanceof LocalSuperClass){
				LocalSuperClass lsc = (LocalSuperClass)abstractSuperClass;
				collectEReferences(currentEReferences, lsc);
			}
		}
		
		
		
		return currentEReferences;
	}
	
	
	
	

	private static void collectEReferences(
			Collection<EReference> currentEReferences, LocalSuperClass lsc) {
		
		Class clazz = lsc.getSuperClass();
		
		currentEReferences.addAll(getCurrentEReferences(clazz));
		
		EList<AbstractSuperClass> superTypes = clazz.getInheritences();
		
		if (superTypes.isEmpty())
			return;
		
		for (AbstractSuperClass abstractSuperClass : superTypes) {
			if (abstractSuperClass instanceof ExternalSuperClass){
				ExternalSuperClass esc = (ExternalSuperClass)abstractSuperClass;
				currentEReferences.addAll(esc.getSuperClass().getEAllReferences());
			}
			
			if (abstractSuperClass instanceof LocalSuperClass){
				LocalSuperClass lsc2 = (LocalSuperClass)abstractSuperClass;
				collectEReferences(currentEReferences, lsc2);
			}
		}
	}

	
	private static Collection<EReference> getCurrentEReferences(
			Class vpdslClass) {
		
		Collection<EReference> ref = new HashSet<EReference>();
		
		EList<org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation> associations = 
				vpdslClass.getVP_Classes_Associations();
		
		for (org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation abstractAssociation : associations) {
			if (abstractAssociation instanceof ExternalAssociation){
				ExternalAssociation ea = (ExternalAssociation)abstractAssociation;
				ref.add(ea.getReference());
			}
		}
		
		return ref;
	}
	
	
	/*
	 * Local local domain classes
	 */
	
	

	private static boolean handleLocalLocalAssociation(IEObjectDescription d,
			LocalClass localDomain_class, LocalClass containerLocalDomain_class) {
		
		return  handleLocalLocalAssociation(d, localDomain_class.getClass_(), containerLocalDomain_class.getClass_());
	}

	private static boolean handleLocalLocalAssociation(IEObjectDescription d,
		Class localVpdescClass, Class containerVpdescClass) {
		
		if (localVpdescClass == null || containerVpdescClass == null)
			return false;
			
		Collection<org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation> availableAssociations = getAllAssociations(containerVpdescClass);
		filterWithType(availableAssociations, localVpdescClass);
		LocalClassAssociation lca = (LocalClassAssociation)d.getEObjectOrProxy();
		Class target  = lca.getLocalTarget();
		
		return (areEqualWithHerarchy(target, localVpdescClass)) && (availableAssociations.contains(lca));
	}
	
	
	private static Collection<org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation> getAllAssociations(Class containerDomain){
		Collection<org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation> associations =
				new HashSet<org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation>();

		
		associations.addAll(containerDomain.getVP_Classes_Associations());
		EList<AbstractSuperClass> superTypes = containerDomain.getInheritences();

		
		for (AbstractSuperClass abstractSuperClass : superTypes) {
			if (abstractSuperClass instanceof LocalSuperClass){
				LocalSuperClass lsc = (LocalSuperClass)abstractSuperClass;
				Class clazz = lsc.getSuperClass();
				associations.addAll(clazz.getVP_Classes_Associations());
				getAllAssociations_rec(associations, clazz);
			}
		}
		
		return associations;
	}
	
	
	static Collection<AbstractSuperClass> visited = new HashSet<AbstractSuperClass>();
	private static void getAllAssociations_rec(
			Collection<org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation> associations,
			Class clazz) {
		
		if (clazz == null) return;
		
		EList<AbstractSuperClass> superTypes = clazz.getInheritences();
		
		if (superTypes.isEmpty()) return;

		for (AbstractSuperClass abstractSuperClass : superTypes) {
			if (!visited.contains(abstractSuperClass)){
				if (abstractSuperClass instanceof LocalSuperClass){
					LocalSuperClass lsc = (LocalSuperClass)abstractSuperClass;
					Class clazz2 = lsc.getSuperClass();
					associations.addAll(clazz2.getVP_Classes_Associations());
					visited.add(abstractSuperClass);
					getAllAssociations_rec(associations, clazz);
				}
			}
		}
	}
	
	private static void filterWithType(Collection<org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation> associations,
			Class type){
		Collection<Class> hierarchyTypes = getHierarchyEClassifiersOf(type);
		
		Iterator<org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation> it = associations.iterator();
		
		while(it.hasNext()){
			org.polarsys.kitalpha.ad.viewpoint.dsl.as.model.vpdesc.AbstractAssociation a = it.next();
			if (a instanceof LocalClassAssociation){
				LocalClassAssociation tmp = (LocalClassAssociation)a;
				if (!hierarchyTypes.contains(tmp.getLocalTarget())){
					it.remove();
				}
			}
		}
	}
	
	
	private static Collection<Class> getHierarchyEClassifiersOf(Class type){
		Collection<Class> eClasses = new HashSet<Class>();
		
		eClasses.add(type);
		
		EList<AbstractSuperClass> superClasses = type.getInheritences();
		
		for (AbstractSuperClass abstractSuperClass : superClasses) {
			if (abstractSuperClass instanceof LocalSuperClass){
				LocalSuperClass lsc = (LocalSuperClass)abstractSuperClass;
				Class clazz = lsc.getSuperClass();
				eClasses.add(clazz);
				getHierarchyEClassifiersOf_rec(eClasses, clazz);
			}
		}
		
		return eClasses;
	}
	
	private static void getHierarchyEClassifiersOf_rec(Collection<Class> eClasses,
			Class clazz) {
		EList<AbstractSuperClass> superClasses = clazz.getInheritences();
		
		if (superClasses.isEmpty())
			return;
		
		for (AbstractSuperClass abstractSuperClass : superClasses) {
			if (abstractSuperClass instanceof LocalSuperClass){
				LocalSuperClass lsc = (LocalSuperClass)abstractSuperClass;
				Class clazz2 = lsc.getSuperClass();
				eClasses.add(clazz2);
				getHierarchyEClassifiersOf_rec(eClasses, clazz2);
			}
		}
		
	}
	
	private static boolean areEqualWithHerarchy(Class target,
			Class localVpdescClass) {
		
		if (localVpdescClass == null) return false;
		if (target.eClass() == localVpdescClass.eClass()) return true;
		
		boolean areEqual = false;
		
		
		
		EList<AbstractSuperClass> superClasses = localVpdescClass.getInheritences();
		
		if (superClasses.isEmpty())
			return false;
		
		for (AbstractSuperClass abstractSuperClass : superClasses) {
			if (abstractSuperClass instanceof LocalSuperClass){
				Class clazz = ((LocalSuperClass)abstractSuperClass).getSuperClass();
				
				areEqual |= (clazz.eClass() == target.eClass());
				
				if (areEqual) return areEqual;
				
				return areEqualWithHerarchy(target, clazz);
			}
		}
		
		return false;
	}
}
