/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.mavenizor.maven;

import java.util.Collections;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.slf4j.Logger;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sourcepit.common.utils.lang.Exceptions;

@SuppressWarnings("deprecation")
public final class DeploymentHandler extends AbstractDistributionHandler
{
   private final RemoteRepositoryManager remoteRepositoryManager;
   private final RepositorySystemSession repositorySession;
   private final ArtifactDeployer deployer;
   private final ArtifactRepository localRepository;
   private final ArtifactRepository snapshotRepository;
   private final ArtifactRepository releaseRepository;

   public DeploymentHandler(Logger log, RemoteRepositoryManager remoteRepositoryManager,
      RepositorySystemSession repositorySession, ArtifactDeployer deployer, ArtifactRepository localRepository,
      ArtifactRepository snapshotRepository, ArtifactRepository releaseRepository)
   {
      super(log);
      this.remoteRepositoryManager = remoteRepositoryManager;
      this.repositorySession = repositorySession;
      this.deployer = deployer;
      this.localRepository = localRepository;
      this.snapshotRepository = snapshotRepository;
      this.releaseRepository = releaseRepository;
   }

   @Override
   protected void doDistribute(Artifact artifact)
   {
      final ArtifactRepository deploymentRepository;
      if (ArtifactUtils.isSnapshot(artifact.getVersion()))
      {
         deploymentRepository = snapshotRepository;
      }
      else
      {
         deploymentRepository = releaseRepository;
      }
      try
      {
         deployer.deploy(artifact.getFile(), artifact, deploymentRepository, localRepository);
      }
      catch (ArtifactDeploymentException e)
      {
         throw Exceptions.pipe(e);
      }
   }

   @Override
   protected boolean existsInTarget(Artifact artifact)
   {
      RemoteRepository remoteRepo = RepositoryUtils.toRepo(snapshotRepository);
      /*
       * NOTE: This provides backward-compat with maven-deploy-plugin:2.4 which bypasses the repository factory
       * when using an alternative deployment location.
       */
      if (snapshotRepository instanceof DefaultArtifactRepository && snapshotRepository.getAuthentication() == null)
      {
         remoteRepo.setAuthentication(repositorySession.getAuthenticationSelector().getAuthentication(remoteRepo));
         remoteRepo.setProxy(repositorySession.getProxySelector().getProxy(remoteRepo));
      }

      RepositoryConnector connector = null;
      try
      {
         connector = remoteRepositoryManager.getRepositoryConnector(repositorySession, remoteRepo);

         final ArtifactDownload download = new ArtifactDownload();
         download.setArtifact(RepositoryUtils.toArtifact(artifact));
         download.setExistenceCheck(true);

         connector.get(Collections.singletonList(download), null);

         return download.getException() == null;
      }
      catch (Exception e)
      {
         throw Exceptions.pipe(e);
      }
      finally
      {
         if (connector != null)
         {
            connector.close();
         }
      }
   }
}