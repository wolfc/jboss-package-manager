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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.entity.InstalledFile;
import org.jboss.ejb3.packagemanager.entity.InstalledPackage;
import org.jboss.ejb3.packagemanager.entity.PackageDependency;
import org.jboss.ejb3.packagemanager.entity.PackageManagerEntity;
import org.jboss.ejb3.packagemanager.exception.PackageNotInstalledException;
import org.jboss.ejb3.packagemanager.metadata.InstallFileType;
import org.jboss.logging.Logger;

/**
 * DefaultDatabaseManager
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DefaultDatabaseManager implements PackageDatabaseManager
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

      // TODO: Set it in a better way
      //      String jbossServerDataDir = pkgMgrCtx.getJBossServerHome() + "/server/default/data";
      //      System.setProperty("derby.system.home", "/home/jpai/pm");
      System.out.println("Derby system home is " + System.getProperty("derby.system.home"));
      this.entityMgrFactory = Persistence.createEntityManagerFactory("default");

   }

   private PackageManagerEntity getOrCreatePackageManagerEntity(PackageManagerContext pkgMgrCtx)
   {
      EntityManager em = this.getEntityManager();
      Query query = em.createQuery("from PackageManagerEntity pm where pm.jbossHome='" + pkgMgrCtx.getJBossServerHome()
            + "'");
      List<PackageManagerEntity> packageManagers = query.getResultList();
      if (packageManagers == null || packageManagers.isEmpty())
      {
         PackageManagerEntity pm = new PackageManagerEntity(pkgMgrCtx);
         EntityTransaction tx = em.getTransaction();
         tx.begin();
         em.persist(pm);
         tx.commit();
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
   public InstalledPackage installPackage(PackageContext pkgCtx)
   {
      EntityManager em = this.getEntityManager();
      EntityTransaction tx = em.getTransaction();
      tx.begin();
      PackageManagerEntity packageManager = this.getOrCreatePackageManagerEntity(this.packageManagerCtx);

      InstalledPackage newlyInstalledPackage = this.createPackage(packageManager, pkgCtx);

      try
      {
         em.persist(newlyInstalledPackage);
         tx.commit();
         logger.info("Recorded installation of package " + pkgCtx + " to database");
         return newlyInstalledPackage;

      }
      catch (Exception e)
      {
         tx.rollback();
         this.removeEntityManager(em);
         throw new RuntimeException(e);
      }

   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#getInstalledPackage(java.lang.String)
    */
   @Override
   public InstalledPackage getInstalledPackage(String name)
   {
      EntityManager em = this.getEntityManager();
      PackageManagerEntity packageManager = this.getOrCreatePackageManagerEntity(this.packageManagerCtx);
      Query query = em.createQuery("from " + InstalledPackage.class.getSimpleName() + " p where p.name='" + name
            + "' and p.packageManager.id=" + packageManager.getId());
      return (InstalledPackage) query.getSingleResult();
   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#isPackageInstalled(java.lang.String)
    */
   @Override
   public boolean isPackageInstalled(String name)
   {
      EntityManager em = this.getEntityManager();
      PackageManagerEntity packageManager = this.getOrCreatePackageManagerEntity(this.packageManagerCtx);
      Query query = em.createQuery("from " + InstalledPackage.class.getSimpleName() + " p where p.name='" + name
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
   public Set<InstalledPackage> getDependentPackages(String name)
   {
      InstalledPackage installedPackage = this.getInstalledPackage(name);
      EntityManager em = this.getEntityManager();
      Query query = em.createQuery("select pd.dependentPackage from " + PackageDependency.class.getSimpleName()
            + " pd " + "join pd.dependeePackage p " + " where p.id=" + installedPackage.getId());

      List<InstalledPackage> result = query.getResultList();
      if (result == null || result.isEmpty())
      {
         return Collections.EMPTY_SET;
      }
      Set<InstalledPackage> dependentPackages = new HashSet<InstalledPackage>(result);
      return dependentPackages;
   }

   /**
    * 
    * @param pkgMgrEntity
    * @param pkgCtx
    * @return
    */
   private InstalledPackage createPackage(PackageManagerEntity pkgMgrEntity, PackageContext pkgCtx)
   {
      InstalledPackage newPackage = new InstalledPackage(pkgMgrEntity, pkgCtx.getPackageName(), pkgCtx
            .getPackageVersion());

      List<InstallFileType> files = pkgCtx.getInstallationFiles();
      if (files != null)
      {
         Set<InstalledFile> installationFilesForNewPackage = new HashSet<InstalledFile>(files.size());
         newPackage.setInstallationFiles(installationFilesForNewPackage);

         for (InstallFileType file : files)
         {
            InstalledFile installationFile = new InstalledFile(file.getName(), file.getDestPath());
            installationFile.setPkg(newPackage);
            if (file.getType() != null)
            {
               installationFile.setFileType(file.getType().toString());
            }
            installationFilesForNewPackage.add(installationFile);
         }
      }

      Set<PackageContext> dependencyPackages = pkgCtx.getDependencyPackages();
      if (dependencyPackages != null)
      {
         Set<PackageDependency> dependencyPackagesForNewPackage = new HashSet<PackageDependency>(dependencyPackages
               .size());
         newPackage.setDependencies(dependencyPackagesForNewPackage);

         for (PackageContext dependencyPkgCtx : dependencyPackages)
         {
            PackageDependency dependency = new PackageDependency();
            dependency.setDependentPackage(newPackage);
            InstalledPackage dependencyPackage = this.getInstalledPackage(dependencyPkgCtx.getPackageName());
            dependency.setDependeePackage(dependencyPackage);

            dependencyPackagesForNewPackage.add(dependency);
         }
      }
      return newPackage;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#upgradePackage(org.jboss.ejb3.packagemanager.entity.InstalledPackage, org.jboss.ejb3.packagemanager.entity.InstalledPackage)
    */
   @Override
   public InstalledPackage upgradePackage(PackageContext packageToUpgrade)
   {
      // get all packages which were dependent on the previous version of the package
      return null;
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
      InstalledPackage installedPackage = this.getInstalledPackage(name);
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
    * TODO: Revisit this
    * @param em
    */
   private void removeEntityManager(EntityManager em)
   {
      em.close();
      currentEntityManager.set(null);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.db.PackageDatabaseManager#removePackage(org.jboss.ejb3.packagemanager.entity.InstalledPackage)
    */
   @Override
   public void removePackage(InstalledPackage installedPackage)
   {
      EntityManager em = this.getEntityManager();
      EntityTransaction tx = em.getTransaction();
      tx.begin();
      try
      {
         installedPackage = em.merge(installedPackage);
         em.remove(installedPackage);
         tx.commit();
         logger.info("Deleted installed package = " + installedPackage.getId());
      }
      catch (Exception e)
      {
         // tx.rollback();
         this.removeEntityManager(em);
         throw new RuntimeException(e);
      }

   }

}
