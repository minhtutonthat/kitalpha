/**
 * Copyright (c) 2014, 2017 Thales Global Services S.A.S.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *  Thales Global Services S.A.S - initial API and implementation
 */
package org.polarsys.kitalpha.ad.viewpoint.dsl.cs.text.generator;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.polarsys.kitalpha.ad.viewpoint.dsl.as.activityexplorer.model.viewpointActivityExplorer.ViewpointActivityExplorer;
import org.polarsys.kitalpha.ad.viewpoint.dsl.cs.text.generator.CommonGenerator;

/**
 * @author Faycal Abka
 */
@SuppressWarnings("all")
public class ActivityexplorerGenerator extends CommonGenerator {
  @Override
  public boolean checkInput(final List<EObject> objects) {
    final Function1<EObject, Boolean> _function = (EObject c) -> {
      return Boolean.valueOf((c instanceof ViewpointActivityExplorer));
    };
    EObject activityExplorer = IterableExtensions.<EObject>findFirst(objects, _function);
    boolean _equals = Objects.equal(activityExplorer, null);
    if (_equals) {
      return false;
    }
    return true;
  }
}
