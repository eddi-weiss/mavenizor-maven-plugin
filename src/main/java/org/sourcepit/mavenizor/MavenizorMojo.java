/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.mavenizor;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.tycho.core.TargetEnvironment;
import org.eclipse.tycho.core.TargetPlatformConfiguration;
import org.eclipse.tycho.core.utils.TychoProjectUtils;
import org.slf4j.Logger;
import org.sourcepit.common.utils.lang.PipedException;

/**
 * @requiresDependencyResolution compile
 * @goal mavenize
 * @phase package
 * @author Bernd Vogt <bernd.vogt@sourcepit.org>
 */
public class MavenizorMojo extends AbstractMavenizorMojo
{
   private final class MultiProperty extends HashSet<String>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean equals(Object o)
      {
         return isEmpty() || contains(o);
      }
   }

   @Inject
   private Logger logger;

   /** @parameter expression="${session}" */
   private MavenSession session;

   @Inject
   private BundleResolver bundleResolver;
   
   @Inject
   private Mavenizor mavenizor;

   @Override
   protected void doExecute() throws PipedException
   {
      logger.info("Hello :-)");

      final OsgiStateBuilder stateBuilder = new OsgiStateBuilder();
      addPlatformProperties(session, stateBuilder);
      addBundles(stateBuilder);

      final State state = stateBuilder.getState();
      state.resolve(false);
      
      mavenizor.mavenize(state);

      logger.info("awesome!");
   }

   private void addPlatformProperties(final MavenSession session, final OsgiStateBuilder stateBuilder)
   {
      final MavenProject project = session.getCurrentProject();

      final TargetPlatformConfiguration configuration = TychoProjectUtils.getTargetPlatformConfiguration(project);

      final String eeName = configuration.getExecutionEnvironment();
      if (eeName == null)
      {
         stateBuilder.addSystemExecutionEnvironmentProperties();
      }
      else
      {
         stateBuilder.addExecutionEnvironmentProperties(eeName);
      }

      final Set<String> os = new MultiProperty();
      final Set<String> ws = new MultiProperty();
      final Set<String> arch = new MultiProperty();
      for (TargetEnvironment targetEnvironment : configuration.getEnvironments())
      {
         os.add(targetEnvironment.getOs());
         ws.add(targetEnvironment.getWs());
         arch.add(targetEnvironment.getArch());
      }

      final Map<String, Object> targetMap = new HashMap<String, Object>();
      targetMap.put(OsgiStateBuilder.OSGI_OS, os);
      targetMap.put(OsgiStateBuilder.OSGI_WS, ws);
      targetMap.put(OsgiStateBuilder.OSGI_ARCH, arch);
      stateBuilder.addPlatformProperties(targetMap);
   }

   private void addBundles(final OsgiStateBuilder stateBuilder)
   {
      bundleResolver.resolve(session, new BundleResolver.Handler()
      {
         public void resolved(File bundleLocation)
         {
            stateBuilder.addBundle(bundleLocation);
         }
      });
   }
}
