/*******************************************************************************
 * Copyright (c) 2018  Thales Global Services S.A.S.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Thales Global Services S.A.S - initial API and implementation
 *******************************************************************************/
package org.polarsys.kitalpha.massactions.edit;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.polarsys.kitalpha.massactions.activator.MAActivator;
import org.polarsys.kitalpha.massactions.core.table.IMATable;
import org.polarsys.kitalpha.massactions.edit.table.METable;
import org.polarsys.kitalpha.massactions.shared.view.MAView;

/**
 * A Mass Editing view extension of the default {@link MAView}.
 * 
 * @author Sandu Postaru 
 *
 */
public class MEView extends MAView {

	@Override
	public IMATable createTable(Composite parent) {
		return new METable(parent);
	}

	@Override
	protected ImageDescriptor getNewViewIconDescriptor() {
	  return MAActivator.getDefault().getImageRegistry().getDescriptor(MAActivator.IMAGE_ME_NEW_VIEW);
	}
}
