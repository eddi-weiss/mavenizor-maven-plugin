/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.mavenizor.maven;

import org.apache.maven.execution.MavenSession;
import org.sourcepit.mavenizor.maven.tycho.TychoProjectBundleResolver.Handler;


public interface BundleResolver
{
   void resolve(final MavenSession session, final Handler handler);
}