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
package org.jboss.ejb3.packagemanager.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.db.DefaultDatabaseManager;
import org.jboss.ejb3.packagemanager.db.PackageDatabaseManager;
import org.jboss.ejb3.packagemanager.entity.InstalledFile;
import org.jboss.ejb3.packagemanager.entity.InstalledPackage;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.exception.PackageNotInstalledException;
import org.jboss.ejb3.packagemanager.installer.DefaultInstaller;
import org.jboss.ejb3.packagemanager.installer.Installer;
import org.jboss.ejb3.packagemanager.installer.MergingInstaller;
import org.jboss.ejb3.packagemanager.metadata.FileType;
import org.jboss.ejb3.packagemanager.metadata.InstallFileType;
import org.jboss.ejb3.packagemanager.metadata.PackagedDependency;
import org.jboss.ejb3.packagemanager.metadata.ScriptType;
import org.jboss.ejb3.packagemanager.metadata.UnProcessedDependenciesType;
import org.jboss.ejb3.packagemanager.script.ScriptProcessor;
import org.jboss.ejb3.packagemanager.script.impl.AntScriptProcessor;
import org.jboss.logging.Logger;

/**
 * DefaultPackageManagerImpl
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DefaultPackageManagerImpl implements PackageManager
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(DefaultPackageManagerImpl.class);

   /**
    * The server against which this package manager will carry out the installation/uninstallation
    * 
    */
   private String installationServerHome;

   /**
    * Package manager environment
    */
   private PackageManagerEnvironment environment;

   /**
    * Package manager context
    */
   private PackageManagerContext pkgMgrCtx;

   /**
    * Package database manager
    */
   private PackageDatabaseManager pkgDatabaseManager;

   /**
    * Creates the default package manager for a server 
    * 
    * @param environment The package manager environment
    * @param jbossHome The JBoss AS server home
    */
   public DefaultPackageManagerImpl(PackageManagerEnvironment environment, String jbossHome)
   {
      this.environment = environment;
      this.installationServerHome = jbossHome;
      this.pkgMgrCtx = new DefaultPackageManagerContext(this);
      
      this.pkgDatabaseManager = new DefaultDatabaseManager(this.pkgMgrCtx);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManager#getPackageManagerEnvironment()
    */
   public PackageManagerEnvironment getPackageManagerEnvironment()
   {
      return this.environment;
   }

   /**
    * Returns the server home against which this package manager operates
    */
   public String getServerHome()
   {
      return this.installationServerHome;
   }

   /**
    * Installs a package
    * 
    * @param pkgPath The URL to the package that is to be installed
    * @throws PackageManagerException If any exceptions occur during installation of the package
    * @see org.jboss.ejb3.packagemanager.PackageManager#installPackage(java.lang.String)
    */
   @Override
   public void installPackage(String pkgPath) throws PackageManagerException
   {
      if (pkgPath == null)
      {
         throw new PackageManagerException("Package path is null");
      }
      URL packageURL = null;
      try
      {
         packageURL = this.getPackageURL(pkgPath);
      }
      catch (MalformedURLException mue)
      {
         throw new PackageManagerException("Cannot parse path " + pkgPath, mue);
      }
      this.installPackage(packageURL);
   }

   /**
    * Installs the package from the {@code packageURL}
    * 
    * @param packageURL The URL to the package that is to be installed
    * @throws PackageManagerException If any exceptions occur during installation of the package
    * @see org.jboss.ejb3.packagemanager.PackageManager#installPackage(URL)
    * 
    */
   @Override
   public void installPackage(URL packageURL) throws PackageManagerException
   {
      if (packageURL == null)
      {
         throw new PackageManagerException("Package URL is null");
      }

      // create a package context
      PackageContext pkgCtx = new DefaultPackageContext(this.pkgMgrCtx, packageURL);
      this.installPackage(pkgCtx);

   }

   /**
    * 
    * @param pkgContext
    * @throws PackageManagerException
    */
   public void installPackage(PackageContext pkgContext) throws PackageManagerException
   {
      if (pkgContext == null)
      {
         throw new PackageManagerException("Package context is null");
      }
      // check if package is already installed
      boolean packageAlreadyInstalled = this.pkgDatabaseManager.isPackageInstalled(pkgContext.getPackageName());
      if (packageAlreadyInstalled)
      {
         throw new PackageManagerException("Package " + pkgContext + " is already installed");
      }
      logger.debug("New package " + pkgContext + " being installed");
      // proceed with installation of the package

      if (pkgContext.getInstallationFiles() == null)
      {
         throw new PackageManagerException("There are no files to install for package: " + pkgContext);
      }
      // work on dependencies first (because if deps are not satisfied then no point
      // running the pre-install step.
      // BUT, think about this (should we first run pre-install and then deps?). 
      // What would be a ideal behaviour? 
      this.installDependencies(pkgContext);
      // pre-installation step
      this.preInstallPackage(pkgContext);
      // install files in this package
      for (InstallFileType fileToInstall : pkgContext.getInstallationFiles())
      {
         Installer installer = getInstaller(fileToInstall);
         installer.install(pkgContext, fileToInstall);
      }
      // post-installation step
      this.postInstallPackage(pkgContext);
      this.pkgDatabaseManager.installPackage(pkgContext);
      logger.info("Installed " + pkgContext);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManager#removePackage(java.lang.String)
    */
   @Override
   public void removePackage(String packageName) throws PackageNotInstalledException, PackageManagerException
   {
      // get the installed package
      boolean isPackageInstalled = this.pkgDatabaseManager.isPackageInstalled(packageName);
      if (!isPackageInstalled)
      {
         throw new PackageNotInstalledException("Package " + packageName + " is not installed - so cannot be removed!");
      }
      InstalledPackage installedPackage = this.pkgDatabaseManager.getInstalledPackage(packageName);
      this.removePackage(installedPackage, false);
      
   }

   protected void removePackage(InstalledPackage installedPackage, boolean forceRemove)
         throws PackageNotInstalledException, PackageManagerException
   {
      String packageName = installedPackage.getPackageName();
      if (!forceRemove)
      {
         // check if other packages are dependent on this package
         // If yes, then do NOT remove this package. Else remove this package
         Set<InstalledPackage> dependentPackages = this.pkgDatabaseManager.getDependentPackages(packageName);
         if (dependentPackages != null && !dependentPackages.isEmpty())
         {
            throw new PackageManagerException("Other packages are dependent on package " + packageName
                  + " - cannot remove this package!");
         }
      }
      // TODO : Revisit this installer creation
      Installer installer = new DefaultInstaller(this.pkgMgrCtx);
      // install files in this package
      for (InstalledFile fileToUninstall : installedPackage.getInstallationFiles())
      {
         installer.uninstall(installedPackage, fileToUninstall);
      }
      this.pkgDatabaseManager.removePackage(installedPackage);
      logger.info("Uninstalled " + packageName);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManager#updatePackage(java.lang.String)
    */
   @Override
   public void updatePackage(String packageFilePath) throws PackageManagerException
   {
      if (packageFilePath == null)
      {
         throw new PackageManagerException("Package path is null");
      }
      URL packageURL = null;
      try
      {
         packageURL = this.getPackageURL(packageFilePath);
      }
      catch (MalformedURLException mue)
      {
         throw new PackageManagerException("Cannot parse path " + packageFilePath, mue);
      }
      this.updatePackage(packageURL);
   }

   @Override
   public void updatePackage(URL packageURL) throws PackageManagerException
   {
      if (packageURL == null)
      {
         throw new PackageManagerException("Package URL is null");
      }

      // create a package context
      PackageContext pkgCtx = new DefaultPackageContext(this.pkgMgrCtx, packageURL);
      this.updatePackage(pkgCtx);
   }

   public void updatePackage(PackageContext pkgContext) throws PackageManagerException
   {
      String packageName = pkgContext.getPackageName();
      boolean isPackageInstalled = this.pkgDatabaseManager.isPackageInstalled(packageName);
      if (isPackageInstalled)
      {
         InstalledPackage installedPackage = this.pkgDatabaseManager.getInstalledPackage(packageName);
         logger.info("Removing existing package " + packageName + " version " + installedPackage.getPackageVersion()
               + " for upgrading to " + pkgContext);
         removePackage(installedPackage, true);
      }

      // now install new version
      this.installPackage(pkgContext);
      
   }

   /**
    * Parses the {@code pkgPath} string and returns an appropriate URL.
    * 
    * @param pkgPath 
    * @return 
    * 
    * @throws MalformedURLException If the URL cannot be parsed
    */
   private URL getPackageURL(String pkgPath) throws MalformedURLException
   {
      URL url = null;
      try
      {
         url = new URL(pkgPath);
      }
      catch (MalformedURLException e)
      {
         logger.debug("Cannot handle " + pkgPath + " - will try using file: URL");
         // fall back to file based URL
         File file = new File(pkgPath);
         url = file.toURI().toURL();
      }
      return url;
   }

   /**
    * Returns the appropriate {@link Installer} for the {@link InstallFileType}
    */
   private Installer getInstaller(InstallFileType file)
   {
      if (file == null)
      {
         return null;
      }
      FileType fileType = file.getType();
      if (fileType == FileType.CONFIG)
      {
         return new MergingInstaller(this.pkgMgrCtx);
      }
      return new DefaultInstaller(this.pkgMgrCtx);
   }

   /**
    * The pre-installation step for packages. Each package can have multiple 
    * pre-install scripts to be run. This method runs those pre-install scripts
    * 
    * @param pkgCtx The package source
    * @throws PackageManagerException If any exception occurs during pre-installation of the 
    * package
    */
   protected void preInstallPackage(PackageContext pkgCtx) throws PackageManagerException
   {
      // find any pre-install scripts
      List<ScriptType> preInstallScripts = pkgCtx.getPreInstallScripts();
      if (preInstallScripts == null || preInstallScripts.isEmpty())
      {
         logger.debug("There are no pre-install scripts for " + pkgCtx);
         return;
      }
      for (ScriptType script : preInstallScripts)
      {
         // TODO: Can we just have one instance of the script processor to process
         // all scripts? Stateful/stateless?
         ScriptProcessor scriptProcessor = new AntScriptProcessor();
         scriptProcessor.processScript(this.pkgMgrCtx, pkgCtx, script);
      }

   }

   /**
    * The post-installation step for packages. Each package can have multiple 
    * post-install scripts to be run. This method runs those post-install scripts
    * 
    * @param pkgCtx The package source
    * @throws PackageManagerException If any exception occurs during post-installation of the 
    * package
    */
   protected void postInstallPackage(PackageContext pkgCtx) throws PackageManagerException
   {
      // find any post-install scripts
      List<ScriptType> postInstallScripts = pkgCtx.getPostInstallScripts();
      if (postInstallScripts == null || postInstallScripts.isEmpty())
      {
         logger.debug("There are no post-install scripts for " + pkgCtx);
         return;
      }
      for (ScriptType script : postInstallScripts)
      {
         ScriptProcessor scriptProcessor = new AntScriptProcessor();
         // TODO: Can we just have one instance of the script processor to process
         // all scripts? Stateful/stateless?
         scriptProcessor.processScript(this.pkgMgrCtx, pkgCtx, script);

      }
   }

   /**
    * Process any dependencies listed for the package. Dependency processing
    * will include resolving and retrieving the appropriate dependency packages
    * and install those packages. Dependency packages, either in the form of {@link PackagedDependency}
    * or {@link UnProcessedDependenciesType} will be installed during this process.
    * 
    * First the packaged dependencies are installed and then the unprocessed dependencies
    * 
    * @param pkgContext
    * @throws PackageManagerException
    */
   protected void installDependencies(PackageContext pkgContext) throws PackageManagerException
   {
      Set<PackageContext> dependencies = pkgContext.getDependencyPackages();
      for (PackageContext dependencyPackage : dependencies)
      {
         logger.info("Installing dependency package : " + dependencyPackage + " for dependent package: " + pkgContext);
         this.updatePackage(dependencyPackage);
      }
   }

}
