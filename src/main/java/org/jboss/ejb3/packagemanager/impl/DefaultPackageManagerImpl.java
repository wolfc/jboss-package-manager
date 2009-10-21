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

import gnu.getopt.Getopt;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.dependency.DependencyManager;
import org.jboss.ejb3.packagemanager.dependency.impl.IvyDependencyManager;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.installer.DefaultInstaller;
import org.jboss.ejb3.packagemanager.installer.Installer;
import org.jboss.ejb3.packagemanager.installer.MergingInstaller;
import org.jboss.ejb3.packagemanager.metadata.Dependencies;
import org.jboss.ejb3.packagemanager.metadata.FileType;
import org.jboss.ejb3.packagemanager.metadata.InstallFile;
import org.jboss.ejb3.packagemanager.metadata.Package;
import org.jboss.ejb3.packagemanager.metadata.PackagedDependency;
import org.jboss.ejb3.packagemanager.metadata.PostInstall;
import org.jboss.ejb3.packagemanager.metadata.PreInstall;
import org.jboss.ejb3.packagemanager.metadata.Script;
import org.jboss.ejb3.packagemanager.metadata.UnProcessedDependencies;
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
      // proceed with installation of the package
      Package pkgToInstall = pkgContext.getPackage();
      if (pkgToInstall.getFiles() == null)
      {
         throw new PackageManagerException("There are no files to install for package: " + pkgToInstall.getName()
               + " version: " + pkgToInstall.getVersion());
      }
      // work on dependencies first (because if deps are not satisfied then no point
      // running the pre-install step.
      // BUT, think about this (should we first run pre-install and then deps?). 
      // What would be a ideal behaviour? 
      this.installDependencies(pkgContext);
      // pre-installation step
      this.preInstallPackage(pkgContext);
      // install files in this package
      for (InstallFile fileToInstall : pkgToInstall.getFiles())
      {
         Installer installer = getInstaller(fileToInstall);
         installer.install(this.pkgMgrCtx, pkgContext, fileToInstall);
      }
      // post-installation step
      this.postInstallPackage(pkgContext);
      logger.info("Installed " + pkgContext);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManager#removePackage(java.lang.String)
    */
   public void removePackage(String packageName)
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManager#updatePackage(java.lang.String)
    */
   @Override
   public void updatePackage(String packageFilePath)
   {
      // WIP
      //      URL packageUrl = null;
      //      try
      //      {
      //         packageUrl = this.getPackageURL(packageFilePath);
      //      }
      //      catch (MalformedURLException mue)
      //      {
      //         throw new InstallerException("Cannot parse path " + packageFilePath, mue);
      //      }
      //      this.updatePackage(packageUrl);
   }

   @Override
   public void updatePackage(URL packageURL)
   {

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
    * Returns the appropriate {@link Installer} for the {@link InstallFile}
    */
   public Installer getInstaller(InstallFile file)
   {
      if (file == null)
      {
         return null;
      }
      FileType fileType = file.getType();
      if (fileType == FileType.CONFIG)
      {
         return new MergingInstaller();
      }
      return new DefaultInstaller();
   }

   private DependencyManager getDependencyManager(UnProcessedDependencies dependencies)
   {
      if (dependencies == null)
      {
         return null;
      }
      String depManagerClassName = dependencies.getManager();
      if (depManagerClassName == null)
      {
         // our default is ivy dependency manager
         return new IvyDependencyManager();
      }
      Class<?> dependencyManager = null;
      // load the script processor
      try
      {
         dependencyManager = Class.forName(depManagerClassName, true, Thread.currentThread().getContextClassLoader());
      }
      catch (ClassNotFoundException cnfe)
      {
         throw new RuntimeException("Could not load dependency manager: " + depManagerClassName, cnfe);
      }
      // make sure the dependency manager specified in the metadata
      // does indeed implement the DependencyManager interface
      if (!DependencyManager.class.isAssignableFrom(dependencyManager))
      {
         throw new RuntimeException("Dependency manager " + depManagerClassName + " does not implement "
               + DependencyManager.class);
      }
      try
      {
         return (DependencyManager) dependencyManager.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not instantiate dependency manager " + depManagerClassName, e);
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
      Package pkgMetadata = pkgCtx.getPackage();
      // find any pre-install scripts
      PreInstall preInstall = pkgMetadata.getPreInstall();
      if (preInstall == null || preInstall.getScripts() == null || preInstall.getScripts().isEmpty())
      {
         logger.debug("There are no pre-install scripts for " + pkgCtx);
         return;
      }
      for (Script script : preInstall.getScripts())
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
      Package pkgMetadata = pkgCtx.getPackage();
      // find any post-install scripts
      PostInstall postInstall = pkgMetadata.getPostInstall();
      if (postInstall == null || postInstall.getScripts() == null || postInstall.getScripts().isEmpty())
      {
         logger.debug("There are no post-install scripts for " + pkgCtx);
         return;
      }
      for (Script script : postInstall.getScripts())
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
    * or {@link UnProcessedDependencies} will be installed during this process.
    * 
    * First the packaged dependencies are installed and then the unprocessed dependencies
    * 
    * @param pkgContext
    * @throws PackageManagerException
    */
   protected void installDependencies(PackageContext pkgContext) throws PackageManagerException
   {
      // first process packaged dependencies
      installPackagedDependencies(pkgContext);
      // now now process the dependencies, that have been listed in a dependency file, through
      // a dependency manager
      installUnProcessedPackages(pkgContext);
   }

   /**
    * Processes {@link UnProcessedDependencies} by using the dependency manager specified
    * through {@link UnProcessedDependencies#getManager()}
    * 
    * @param pkgContext The package context for which the dependencies are being resolved
    * @throws PackageManagerException If any exceptions occur while processing the dependencies for the package
    */
   protected void installUnProcessedPackages(PackageContext pkgContext) throws PackageManagerException
   {
      Package pkgMeta = pkgContext.getPackage();
      Dependencies dependencies = pkgMeta.getDependencies();
      if (dependencies == null || dependencies.getUnProcessedDependencies() == null)
      {
         logger.debug("No unprocessed dependencies for " + pkgContext);
         return;
      }
      UnProcessedDependencies unProcessedDeps = dependencies.getUnProcessedDependencies();
      File dependencyFile = new File(pkgContext.getPackageRoot(), unProcessedDeps.getFile());
      if (!dependencyFile.exists())
      {
         throw new PackageManagerException("Dependency file " + dependencyFile + " not found for " + pkgContext);
      }
      DependencyManager depManager = this.getDependencyManager(unProcessedDeps);
      if (depManager == null)
      {
         return;
      }
      Set<PackageContext> dependencyPackages = depManager.resolveDepedencies(this.pkgMgrCtx, pkgContext,
            unProcessedDeps);
      if (dependencyPackages == null || dependencyPackages.isEmpty())
      {
         logger.debug("Dependency manager did not find any dependency packages to be installed for " + pkgContext);
         return;
      }
      for (PackageContext dependencyPkg : dependencyPackages)
      {
         logger.info("Installing dependency package: " + dependencyPkg + " for dependent package: " + pkgContext);
         this.installPackage(dependencyPkg);
      }
   }

   /**
    * Processes packaged dependencies {@link PackagedDependency} of a package. These
    * dependency packages are installed during this process.
    *  
    * @param pkgContext
    * @throws PackageManagerException
    */
   protected void installPackagedDependencies(PackageContext pkgContext) throws PackageManagerException
   {
      Package pkgMeta = pkgContext.getPackage();
      Dependencies dependencies = pkgMeta.getDependencies();
      if (dependencies == null || dependencies.getPackagedDependencies() == null
            || dependencies.getPackagedDependencies().isEmpty())
      {
         logger.debug("No packaged dependency for " + pkgContext);
         return;
      }
      List<PackagedDependency> packagedDeps = dependencies.getPackagedDependencies();
      for (PackagedDependency packagedDep : packagedDeps)
      {
         String relativePathToDependencyPackage = packagedDep.getFile();
         File dependencyPackage = new File(pkgContext.getPackageRoot(), relativePathToDependencyPackage);
         if (!dependencyPackage.exists())
         {
            throw new PackageManagerException("packaged-dependency file " + dependencyPackage + " does not exist");
         }
         PackageContext dependencyPkgCtx;
         try
         {
            dependencyPkgCtx = new DefaultPackageContext(this.pkgMgrCtx, dependencyPackage.toURI().toURL());
         }
         catch (MalformedURLException mue)
         {
            throw new RuntimeException(mue);
         }

         logger.info("Installing packaged dependency: " + dependencyPkgCtx + " for dependent package: " + pkgContext);
         this.installPackage(dependencyPkgCtx);
      }
   }

}
