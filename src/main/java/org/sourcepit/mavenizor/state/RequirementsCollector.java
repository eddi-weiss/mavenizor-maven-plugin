/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.mavenizor.state;

import java.util.Collection;

import org.eclipse.osgi.service.resolver.BundleDescription;


public interface RequirementsCollector
{
   Collection<Requirement> collectRequirements(BundleDescription bundle);
}