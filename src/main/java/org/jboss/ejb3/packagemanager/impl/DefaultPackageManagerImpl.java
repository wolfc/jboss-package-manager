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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.annotation.TransactionAttribute;
import org.jboss.ejb3.packagemanager.annotation.TransactionAttributeType;
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
import org.jboss.ejb3.packagemanager.metadata.impl.PostInstallScript;
import org.jboss.ejb3.packagemanager.metadata.impl.PostUnInstallScript;
import org.jboss.ejb3.packagemanager.metadata.impl.PreInstallScript;
import org.jboss.ejb3.packagemanager.metadata.impl.PreUninstallScript;
import org.jboss.ejb3.packagemanager.script.ScriptProcessor;
import org.jboss.ejb3.packagemanager.script.impl.AntScriptProcessor;
import org.jboss.ejb3.packagemanager.tx.TransactionManagerImpl;
import org.jboss.ejb3.packagemanager.util.IOUtil;
import org.jboss.logging.Logger;

/**
 * DefaultPackageManagerImpl
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DefaultPackageManagerImpl implements PackageManager, Synchronization
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
    * Transaction manager
    */
   private TransactionManager transactionManager;

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
      this.transactionManager = TransactionManagerImpl.getInstance();
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
   @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
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
   @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
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
   @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
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
      // store the pre/post uninstall scripts (if any) at a particular location
      // so that they can be used during uninstallation of this package
      this.storeScripts(pkgContext);
      // now record the installation into DB
      this.pkgDatabaseManager.installPackage(pkgContext);
      logger.info("Installed " + pkgContext);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManager#removePackage(java.lang.String)
    */
   @Override
   @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
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
      // pre-uninstall step
      this.preUnInstallPackage(installedPackage);
      // TODO : Revisit this installer creation
      Installer installer = new DefaultInstaller(this.pkgMgrCtx);
      // install files in this package
      for (InstalledFile fileToUninstall : installedPackage.getInstallationFiles())
      {
         installer.uninstall(installedPackage, fileToUninstall);
      }
      // post-uninstall step
      this.postUnInstallPackage(installedPackage);
      this.pkgDatabaseManager.removePackage(installedPackage);
      logger.info("Uninstalled " + packageName);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManager#updatePackage(java.lang.String)
    */
   @Override
   @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
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
   @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
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

   @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
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
    * @see org.jboss.ejb3.packagemanager.PackageManager#getTransactionManager()
    */
   @Override
   public TransactionManager getTransactionManager()
   {
      return this.transactionManager;
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
    * Stores the scripts available in a package to a local store so 
    * that they are available during uninstallation of the package.
    * Currently, only post-uninstall and pre-uinstall scripts are 
    * stored because the rest of the scripts are not required 
    * after installation of a package.
    * 
    * @param pkgCtx Package whose scripts are to be stored
    */
   private void storeScripts(PackageContext pkgCtx) throws PackageManagerException
   {
      String relativePathToScriptStoreDir = this.pkgMgrCtx.getScriptStoreLocation(pkgCtx);
      File scriptStoreDir = new File(this.getPackageManagerEnvironment().getPackageManagerHome(),
            relativePathToScriptStoreDir);
      if (!scriptStoreDir.exists())
      {
         scriptStoreDir.mkdirs();
      }
      logger.debug("Scripts for " + pkgCtx + " will be stored in " + scriptStoreDir);
      // we store only uninstall scripts.

      // post-uninstall
      List<PostUnInstallScript> postUnInstallScripts = pkgCtx.getPostUnInstallScripts();
      if (postUnInstallScripts != null)
      {
         for (PostUnInstallScript script : postUnInstallScripts)
         {
            storeScript(pkgCtx, script, scriptStoreDir);

         }
      }
      // pre-uninstall
      List<PreUninstallScript> preUnInstallScripts = pkgCtx.getPreUnInstallScripts();
      if (preUnInstallScripts != null)
      {
         for (PreUninstallScript script : preUnInstallScripts)
         {
            storeScript(pkgCtx, script, scriptStoreDir);
         }
      }
   }

   /**
    * Stores the script to the <code>destDir</code>
    * 
    * @param pkgCtx Package to which this script belongs
    * @param script Script to be stored
    * @param destDir The destination directory where the script has to be stored
    * @throws PackageManagerException
    */
   private void storeScript(PackageContext pkgCtx, ScriptType script, File destDir) throws PackageManagerException
   {
      String scriptFileName = script.getName();
      File root = pkgCtx.getPackageRoot();
      File path = root;
      if (script.getPath() != null)
      {
         path = new File(root, script.getPath());
      }
      File scriptFile = new File(path, scriptFileName);
      if (!scriptFile.exists())
      {
         throw new PackageManagerException("Script file " + scriptFile + " for " + pkgCtx + " does not exist!");
      }
      try
      {
         File destFile = new File(destDir, scriptFile.getName());
         IOUtil.copy(scriptFile, destFile);
         logger.debug("Stored script file " + scriptFile + " at " + destDir);
      }
      catch (IOException e)
      {
         throw new PackageManagerException("Could not store script due to exception ", e);
      }
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
      List<PreInstallScript> preInstallScripts = pkgCtx.getPreInstallScripts();
      if (preInstallScripts == null || preInstallScripts.isEmpty())
      {
         logger.trace("There are no pre-install scripts for " + pkgCtx);
         return;
      }
      for (PreInstallScript script : preInstallScripts)
      {
         // TODO: Can we just have one instance of the script processor to process
         // all scripts? Stateful/stateless?
         ScriptProcessor scriptProcessor = new AntScriptProcessor();
         String scriptFileName = script.getName();
         File root = pkgCtx.getPackageRoot();
         File path = root;
         if (script.getPath() != null)
         {
            path = new File(root, script.getPath());
         }
         File scriptFile = new File(path, scriptFileName);
         if (!scriptFile.exists())
         {
            throw new PackageManagerException("Script file " + scriptFile + " for " + pkgCtx + " does not exist!");
         }
         scriptProcessor.processPreInstallScript(this.pkgMgrCtx, pkgCtx, scriptFile);
      }

   }

   /**
    * The pre-uninstallation step for packages. Each package can have multiple 
    * pre-uninstall scripts to be run. This method runs those pre-uninstall scripts
    * 
    * @param installedPackage The installed package
    * @throws PackageManagerException If any exception occurs during pre-uninstallation of the 
    * package
    */
   protected void preUnInstallPackage(InstalledPackage installedPackage) throws PackageManagerException
   {
      // find any pre-uninstall scripts
      Set<org.jboss.ejb3.packagemanager.entity.PreUnInstallScript> preUnInstallScripts = installedPackage
            .getPreUnInstallScripts();
      if (preUnInstallScripts == null || preUnInstallScripts.isEmpty())
      {
         logger.trace("There are no pre-uninstall scripts for package " + installedPackage.getPackageName());
         return;
      }
      for (org.jboss.ejb3.packagemanager.entity.PreUnInstallScript script : preUnInstallScripts)
      {
         ScriptProcessor scriptProcessor = new AntScriptProcessor();
         File packageManagerHome = this.pkgMgrCtx.getPackageManagerEnvironment().getPackageManagerHome();
         File scriptFileLocation = new File(packageManagerHome, script.getPath());
         File scriptFile = new File(scriptFileLocation, script.getName());
         if (!scriptFile.exists())
         {
            throw new PackageManagerException("Script file " + scriptFile + " for package "
                  + installedPackage.getPackageName() + " does not exist!");
         }
         scriptProcessor.processPreUnInstallScript(this.pkgMgrCtx, installedPackage, scriptFile);
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
      List<PostInstallScript> postInstallScripts = pkgCtx.getPostInstallScripts();
      if (postInstallScripts == null || postInstallScripts.isEmpty())
      {
         logger.trace("There are no post-install scripts for " + pkgCtx);
         return;
      }
      for (PostInstallScript script : postInstallScripts)
      {
         ScriptProcessor scriptProcessor = new AntScriptProcessor();
         // TODO: Can we just have one instance of the script processor to process
         // all scripts? Stateful/stateless?
         String scriptFileName = script.getName();
         File root = pkgCtx.getPackageRoot();
         File path = root;
         if (script.getPath() != null)
         {
            path = new File(root, script.getPath());
         }
         File scriptFile = new File(path, scriptFileName);
         if (!scriptFile.exists())
         {
            throw new PackageManagerException("Script file " + scriptFile + " for " + pkgCtx + " does not exist!");
         }
         scriptProcessor.processPostInstallScript(this.pkgMgrCtx, pkgCtx, scriptFile);

      }
   }

   /**
    * The post-uninstallation step for packages. Each package can have multiple 
    * post-uninstall scripts to be run. This method runs those post-uninstall scripts
    * 
    * @param installedPackage The installed package
    * @throws PackageManagerException If any exception occurs during post-uninstallation of the 
    * package
    */
   protected void postUnInstallPackage(InstalledPackage installedPackage) throws PackageManagerException
   {
      // find any post-uninstall scripts
      Set<org.jboss.ejb3.packagemanager.entity.PostUnInstallScript> postUnInstallScripts = installedPackage
            .getPostUnInstallScripts();
      if (postUnInstallScripts == null || postUnInstallScripts.isEmpty())
      {
         logger.trace("There are no post-uninstall scripts for package " + installedPackage.getPackageName());
         return;
      }
      for (org.jboss.ejb3.packagemanager.entity.PostUnInstallScript script : postUnInstallScripts)
      {
         ScriptProcessor scriptProcessor = new AntScriptProcessor();
         File packageManagerHome = this.pkgMgrCtx.getPackageManagerEnvironment().getPackageManagerHome();
         File scriptFileLocation = new File(packageManagerHome, script.getPath());
         File scriptFile = new File(scriptFileLocation, script.getName());
         if (!scriptFile.exists())
         {
            throw new PackageManagerException("Script file " + scriptFile + " for package "
                  + installedPackage.getPackageName() + " does not exist!");
         }
         scriptProcessor.processPostUnInstallScript(this.pkgMgrCtx, installedPackage, scriptFile);
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

   /**
    * @see javax.transaction.Synchronization#afterCompletion(int)
    */
   @Override
   public void afterCompletion(int status)
   {
      if (this.pkgDatabaseManager instanceof Synchronization)
      {
         Synchronization dbManager = (Synchronization) this.pkgDatabaseManager;
         dbManager.afterCompletion(status);
      }

   }

   /**
    * @see javax.transaction.Synchronization#beforeCompletion()
    */
   @Override
   public void beforeCompletion()
   {
      if (this.pkgDatabaseManager instanceof Synchronization)
      {
         Synchronization dbManager = (Synchronization) this.pkgDatabaseManager;
         dbManager.beforeCompletion();
      }

   }

}
