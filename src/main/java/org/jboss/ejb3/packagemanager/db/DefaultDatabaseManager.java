/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.ejb3.packagemanager.db;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.transaction.Synchronization;

import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.entity.PersistentDependency;
import org.jboss.ejb3.packagemanager.entity.PersistentFile;
import org.jboss.ejb3.packagemanager.entity.PersistentPackage;
import org.jboss.ejb3.packagemanager.entity.PersistentPackageManager;
import org.jboss.ejb3.packagemanager.entity.PersistentPreUnInstallScript;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.exception.PackageNotInstalledException;
import org.jboss.ejb3.packagemanager.metadata.InstallFileType;
import org.jboss.ejb3.packagemanager.metadata.ScriptType;
import org.jboss.logging.Logger;

/**
 * DefaultDatabaseManager
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DefaultDatabaseManager implements PackageDatabaseManager, Synchronization
{
   /**
    * Logger 
    */
   private static Logger logger = Logger.getLogger(DefaultDatabaseManager.class);

   /**
    * Entity manager factory
    */
   private EntityManagerFactory entityMgrFactory;

   /**
    * TODO: Revisit this
    */
   private ThreadLocal<EntityManager> currentEntityManager = new ThreadLocal<EntityManager>();

   private PackageManagerContext packageManagerCtx;

   /**
    * Constructor
    * @param pkgMgrCtx
    */
   public DefaultDatabaseManager(PackageManagerContext pkgMgrCtx)
   {
      this.packageManagerCtx = pkgMgrCtx;
      PackageManagerEnvironment environment = pkgMgrCtx.getPackageManagerEnvironment();
      // we use derby (filesystem) based DB
      File dbHome = environment.getDataDir();
      if (!dbHome.exists())
      {
         dbHome.mkdirs();
      }
      // set the Derby system home property to point to the package manager db
      System.setProperty("derby.system.home", dbHome.getAbsolutePath());

      logger.info("Package manager DB home set to " + System.getProperty("derby.system.home"));
      this.entityMgrFactory = Persistence.createEntityManagerFactory("default");

   }

   private PersistentPackageManager getOrCreatePackageManagerEntity(PackageManagerContext pkgMgrCtx)
   {
      EntityManager em = this.getEntityManager();
      Query query = em.createQuery("from " + PersistentPackageManager.class.getSimpleName()
            + " pm where pm.jbossHome='" + pkgMgrCtx.getJBossServerHome() + "'");
      List<PersistentPackageManager> packageManagers = query.getResultList();
      if (packageManagers == null || packageManagers.isEmpty())
      {
         PersistentPackageManager pm = new PersistentPackageManager(pkgMgrCtx);
         em.persist(pm);

         return pm;
      }
      else if (packageManagers.size() > 1)
      {
         throw new RuntimeException("More than one package manager found for JBOSS_HOME "
               + pkgMgrCtx.getJBossServerHome());
      }
      else
      {
         return packageManagers.get(0);
      }
   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#installPackage(PackageContext)
    */
   @Override
   public PersistentPackage installPackage(PackageContext pkgCtx)
   {
      EntityManager em = this.getEntityManager();
      //      EntityTransaction tx = em.getTransaction();
      //      tx.begin();
      PersistentPackageManager packageManager = this.getOrCreatePackageManagerEntity(this.packageManagerCtx);

      PersistentPackage newlyInstalledPackage;
      try
      {
         newlyInstalledPackage = this.createPackage(packageManager, pkgCtx);
      }
      catch (PackageManagerException pme)
      {
         throw new RuntimeException(pme);
      }

      em.persist(newlyInstalledPackage);
      logger.info("Recorded installation of package " + pkgCtx + " to database");
      return newlyInstalledPackage;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#getInstalledPackage(java.lang.String)
    */
   @Override
   public PersistentPackage getInstalledPackage(String name) throws PackageNotInstalledException
   {
      EntityManager em = this.getEntityManager();
      PersistentPackageManager packageManager = this.getOrCreatePackageManagerEntity(this.packageManagerCtx);
      Query query = em.createQuery("from " + PersistentPackage.class.getSimpleName() + " p where p.name='" + name
            + "' and p.packageManager.id=" + packageManager.getId());
      return (PersistentPackage) query.getSingleResult();
   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#isPackageInstalled(java.lang.String)
    */
   @Override
   public boolean isPackageInstalled(String name)
   {
      EntityManager em = this.getEntityManager();
      PersistentPackageManager packageManager = this.getOrCreatePackageManagerEntity(this.packageManagerCtx);
      Query query = em.createQuery("from " + PersistentPackage.class.getSimpleName() + " p where p.name='" + name
            + "' and p.packageManager.id=" + packageManager.getId());
      List<Object> result = query.getResultList();

      if (result == null || result.isEmpty())
      {
         return false;
      }
      return true;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#getDependentPackages(java.lang.String)
    */
   @Override
   public Set<PersistentPackage> getDependentPackages(String name) throws PackageNotInstalledException
   {
      PersistentPackage installedPackage = this.getInstalledPackage(name);
      EntityManager em = this.getEntityManager();
      Query query = em.createQuery("select pd.dependentPackage from " + PersistentDependency.class.getSimpleName()
            + " pd " + "join pd.dependeePackage p " + " where p.name='" + installedPackage.getPackageName() + "'");

      List<PersistentPackage> result = query.getResultList();
      if (result == null || result.isEmpty())
      {
         return Collections.EMPTY_SET;
      }
      Set<PersistentPackage> dependentPackages = new HashSet<PersistentPackage>(result);
      return dependentPackages;
   }

   /**
    * 
    * @param pkgMgrEntity
    * @param pkgCtx
    * @return
    * @throws PackageManagerException 
    */
   private PersistentPackage createPackage(PersistentPackageManager pkgMgrEntity, PackageContext pkgCtx)
         throws PackageManagerException
   {
      PersistentPackage newPackage = new PersistentPackage(pkgMgrEntity, pkgCtx.getPackageName(), pkgCtx
            .getPackageVersion());

      List<InstallFileType> files = pkgCtx.getInstallationFiles();
      if (files != null)
      {
         for (InstallFileType file : files)
         {
            PersistentFile installationFile = new PersistentFile(file.getName(), file.getDestPath());
            if (file.getType() != null)
            {
               installationFile.setFileType(file.getType().toString());
            }
            newPackage.addInstallationFile(installationFile);
            installationFile.setPkg(newPackage);

         }
      }
      String relativePathToScriptStore = this.packageManagerCtx.getScriptStoreLocation(pkgCtx);
      List<ScriptType> preUnInstallScripts = pkgCtx.getPreUnInstallScripts();
      if (preUnInstallScripts != null)
      {
         for (ScriptType script : preUnInstallScripts)
         {
            String scriptName = script.getName();
            PersistentPreUnInstallScript preUnInstallScript = new PersistentPreUnInstallScript(newPackage, scriptName,
                  relativePathToScriptStore);
            newPackage.addPreUnInstallScript(preUnInstallScript);
         }
      }

      List<ScriptType> postUnInstallScripts = pkgCtx.getPostUnInstallScripts();
      if (postUnInstallScripts != null)
      {
         for (ScriptType script : postUnInstallScripts)
         {
            String scriptName = script.getName();
            org.jboss.ejb3.packagemanager.entity.PersistentPostUnInstallScript postUnInstallScript = new org.jboss.ejb3.packagemanager.entity.PersistentPostUnInstallScript(
                  newPackage, scriptName, relativePathToScriptStore);
            newPackage.addPostUnInstallScript(postUnInstallScript);
         }
      }

      Set<PackageContext> dependencyPackages = pkgCtx.getDependencyPackages();
      if (dependencyPackages != null)
      {
         Set<PersistentDependency> dependencyPackagesForNewPackage = new HashSet<PersistentDependency>(
               dependencyPackages.size());
         newPackage.setDependencies(dependencyPackagesForNewPackage);

         for (PackageContext dependencyPkgCtx : dependencyPackages)
         {
            PersistentDependency dependency = new PersistentDependency();
            dependency.setDependentPackage(newPackage);
            PersistentPackage dependencyPackage = this.getInstalledPackage(dependencyPkgCtx.getPackageName());
            dependency.setDependeePackage(dependencyPackage);

            dependencyPackagesForNewPackage.add(dependency);
         }
      }
      return newPackage;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#removePackage(java.lang.String)
    */
   @Override
   public void removePackage(String name) throws PackageNotInstalledException
   {
      if (!this.isPackageInstalled(name))
      {
         throw new PackageNotInstalledException(name);
      }
      PersistentPackage installedPackage = this.getInstalledPackage(name);
      this.removePackage(installedPackage);

   }

   /**
    * TODO: Revisit this
    * @return
    */
   private EntityManager getEntityManager()
   {
      EntityManager em = currentEntityManager.get();
      if (em == null)
      {
         em = this.entityMgrFactory.createEntityManager();
         currentEntityManager.set(em);
      }

      return em;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#removePackage(org.jboss.ejb3.packagemanager.entity.PersistentPackage)
    */
   @Override
   public void removePackage(PersistentPackage installedPackage)
   {
      EntityManager em = this.getEntityManager();
      //EntityTransaction tx = em.getTransaction();
      //tx.begin();
      installedPackage = em.merge(installedPackage);
      em.remove(installedPackage);
      //  tx.commit();
      logger.info("Deleted installed package = " + installedPackage.getPackageName());

   }

   /**
    * @see javax.transaction.Synchronization#afterCompletion(int)
    */
   @Override
   public void afterCompletion(int status)
   {
      currentEntityManager.set(null);
   }

   /**
    * @see javax.transaction.Synchronization#beforeCompletion()
    */
   @Override
   public void beforeCompletion()
   {
      // TODO Auto-generated method stub

   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<String> getAllInstalledPackages()
   {
      Set<String> installedPackageNames = new HashSet<String>();
      EntityManager em = this.getEntityManager();
      PersistentPackageManager packageManager = this.getOrCreatePackageManagerEntity(this.packageManagerCtx);
      Query query = em.createQuery("select pkg.name from " + PersistentPackage.class.getSimpleName()
            + " pkg where pkg.packageManager.id=" + packageManager.getId());
      List<String> result = query.getResultList();
      if (result != null)
      {
         installedPackageNames.addAll(result);
      }
      return installedPackageNames;

   }

}
