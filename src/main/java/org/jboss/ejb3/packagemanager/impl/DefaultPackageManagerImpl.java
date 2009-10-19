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

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.PackageSource;
import org.jboss.ejb3.packagemanager.exception.InstallerException;
import org.jboss.ejb3.packagemanager.exception.PackageRetrievalException;
import org.jboss.ejb3.packagemanager.exception.ScriptProcessingException;
import org.jboss.ejb3.packagemanager.installer.DefaultInstaller;
import org.jboss.ejb3.packagemanager.installer.Installer;
import org.jboss.ejb3.packagemanager.installer.MergingInstaller;
import org.jboss.ejb3.packagemanager.metadata.Dependencies;
import org.jboss.ejb3.packagemanager.metadata.FileType;
import org.jboss.ejb3.packagemanager.metadata.InstallFile;
import org.jboss.ejb3.packagemanager.metadata.Package;
import org.jboss.ejb3.packagemanager.metadata.PostInstall;
import org.jboss.ejb3.packagemanager.metadata.PreInstall;
import org.jboss.ejb3.packagemanager.metadata.Script;
import org.jboss.ejb3.packagemanager.retriever.PackageRetriever;
import org.jboss.ejb3.packagemanager.retriever.impl.PackageRetrievalFactory;
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
    * Creates the default package manager for a server 
    * 
    * @param environment The package manager environment
    * @param jbossHome The JBoss AS server home
    */
   public DefaultPackageManagerImpl(PackageManagerEnvironment environment, String jbossHome)
   {
      this.environment = environment;
      this.installationServerHome = jbossHome;
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
    * @throws InstallerException If any exceptions occur during installation of the package
    * @see org.jboss.ejb3.packagemanager.PackageManager#installPackage(java.lang.String)
    */
   @Override
   public void installPackage(String pkgPath) throws InstallerException
   {
      if (pkgPath == null)
      {
         throw new InstallerException("Package path is null");
      }
      URL packageURL = null;
      try
      {
         packageURL = this.getPackageURL(pkgPath);
      }
      catch (MalformedURLException mue)
      {
         throw new InstallerException("Cannot parse path " + pkgPath, mue);
      }
      this.installPackage(packageURL);
   }

   /**
    * Installs the package from the {@code packageURL}
    * 
    * @param packageURL The URL to the package that is to be installed
    * @throws InstallerException If any exceptions occur during installation of the package
    * @see org.jboss.ejb3.packagemanager.PackageManager#installPackage(URL)
    * 
    */
   @Override
   public void installPackage(URL packageURL) throws InstallerException
   {
      if (packageURL == null)
      {
         throw new InstallerException("Package URL is null");
      }
      // get the appropriate retriever
      PackageRetriever pkgRetriever = PackageRetrievalFactory.getPackageRetriever(packageURL);
      // retrieve the package source
      PackageSource pkgSource = null;
      try
      {
         logger.debug("Retrieving package from " + packageURL + " using retriever " + pkgRetriever);
         pkgSource = pkgRetriever.retrievePackage(this, packageURL);
      }
      catch (PackageRetrievalException pre)
      {
         throw new InstallerException("Could not retrieve package: " + packageURL, pre);
      }
      // proceed with installation of the package
      Package pkgToInstall = pkgSource.getPackageMetadata();
      if (pkgToInstall.getFiles() == null)
      {
         throw new InstallerException("There are no files to install for package: " + pkgToInstall.getName()
               + " version: " + pkgToInstall.getVersion());
      }
      // work on dependencies first
      this.processDependencies(pkgSource);
      // pre-installation step
      this.preInstallPackage(pkgSource);
      // install files in this package
      for (InstallFile fileToInstall : pkgToInstall.getFiles())
      {
         Installer installer = getInstaller(fileToInstall);
         installer.install(this, pkgSource, fileToInstall);
      }
      // post-installation step
      this.postInstallPackage(pkgSource);

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

   /**
    * The pre-installation step for packages. Each package can have multiple 
    * pre-install scripts to be run. This method runs those pre-install scripts
    * 
    * @param pkgSource The package source
    * @throws InstallerException If any exception occurs during pre-installation of the 
    * package
    */
   protected void preInstallPackage(PackageSource pkgSource) throws InstallerException
   {
      Package pkgMetadata = pkgSource.getPackageMetadata();
      // find any pre-install scripts
      PreInstall preInstall = pkgMetadata.getPreInstall();
      if (preInstall == null || preInstall.getScripts() == null || preInstall.getScripts().isEmpty())
      {
         logger.debug("There are no pre-install scripts for " + pkgSource);
         return;
      }
      for (Script script : preInstall.getScripts())
      {
         ScriptProcessor scriptProcessor = this.getScriptProcessor(script);
         try
         {
            scriptProcessor.processScript(this, pkgSource, script);
         }
         catch (ScriptProcessingException spe)
         {
            throw new InstallerException("Error while processing script: " + script.getFile() + " for " + pkgSource,
                  spe);
         }
      }

   }

   /**
    * The post-installation step for packages. Each package can have multiple 
    * post-install scripts to be run. This method runs those post-install scripts
    * 
    * @param pkgSource The package source
    * @throws InstallerException If any exception occurs during post-installation of the 
    * package
    */
   protected void postInstallPackage(PackageSource pkgSource) throws InstallerException
   {
      Package pkgMetadata = pkgSource.getPackageMetadata();
      // find any post-install scripts
      PostInstall postInstall = pkgMetadata.getPostInstall();
      if (postInstall == null || postInstall.getScripts() == null || postInstall.getScripts().isEmpty())
      {
         logger.debug("There are no post-install scripts for " + pkgSource);
         return;
      }
      for (Script script : postInstall.getScripts())
      {
         ScriptProcessor scriptProcessor = this.getScriptProcessor(script);
         try
         {
            scriptProcessor.processScript(this, pkgSource, script);
         }
         catch (ScriptProcessingException spe)
         {
            throw new InstallerException("Error while processing script: " + script.getFile() + " for " + pkgSource,
                  spe);
         }
      }
   }

   /**
    * Process any dependencies listed for the package. Dependency processing
    * will include resolving and retrieving the appropriate dependency packages
    * and install (/un-installing?) those packages.
    * 
    * TODO: Work-in-progress
    * @param pkgSource
    * @throws InstallerException
    */
   protected void processDependencies(PackageSource pkgSource) throws InstallerException
   {
      Package pkgMeta = pkgSource.getPackageMetadata();
      Dependencies dependencies = pkgMeta.getDependencies();
      if (dependencies == null)
      {
         logger.debug(pkgSource + " does not have any dependencies");
         return;
      }
      File dependencyFile = new File(pkgSource.getSource(), dependencies.getFile());
      if (!dependencyFile.exists())
      {
         throw new InstallerException("Dependency file " + dependencyFile + " not found for " + pkgSource);
      }

   }

   /**
    * One of the entry points to the package manager.
    * Accepts the command line arguments and carries out appropriate operations
    * through the package-manager.
    * 
    *  TODO: The command line arguments, haven't yet been finalized
    * 
    * @param args
    */
   public static void main(String[] args)
   {
      logger.debug("comm line length = " + args.length);
      StringBuffer sb = new StringBuffer();
      for (String arg : args)
      {
         sb.append(arg);
         sb.append(" ");
      }
      logger.debug(DefaultPackageManagerImpl.class + " invoked with args: " + sb.toString());

      Getopt getOpt = new Getopt("packagemanager", args, "i:u:e:s:p:");
      int opt;
      String packageFilePath = null;
      String jbossHome = null;
      String packageNameToUninstall = null;
      String pmHome = System.getProperty("java.io.tmpdir");
      while ((opt = getOpt.getopt()) != -1)
      {
         switch (opt)
         {
            case 'i' :
               packageFilePath = getOpt.getOptarg();
               break;
            case 'u' :
               packageFilePath = getOpt.getOptarg();
               break;
            case 'e' :
               packageNameToUninstall = getOpt.getOptarg();
               break;
            case 's' :
               jbossHome = getOpt.getOptarg();
               break;
            case 'p' :
               pmHome = getOpt.getOptarg();
               break;
            default :
               throw new Error("Unhandled code " + opt);
         }
      }
      if (jbossHome == null || packageFilePath == null)
      {
         throw new Error("JBoss home or package file not specified");
      }
      PackageManagerEnvironment env = new PackageManagerEnvironment(pmHome);
      PackageManager pm = new DefaultPackageManagerImpl(env, jbossHome);

      // install the package
      try
      {
         pm.installPackage(packageFilePath);
      }
      catch (InstallerException ie)
      {
         throw new RuntimeException(ie);
      }

   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManager#getScriptProcessor(org.jboss.ejb3.packagemanager.metadata.Script)
    */
   public ScriptProcessor getScriptProcessor(Script script)
   {
      if (script == null)
      {
         return null;
      }
      String scriptProcessorClassName = script.getProcessor();
      if (scriptProcessorClassName == null)
      {
         // our default is ant script processor
         return new AntScriptProcessor();
      }
      Class<?> scriptProcessor = null;
      // load the script processor
      try
      {
         scriptProcessor = Class
               .forName(scriptProcessorClassName, true, Thread.currentThread().getContextClassLoader());
      }
      catch (ClassNotFoundException cnfe)
      {
         throw new RuntimeException("Could not load script processor: " + scriptProcessorClassName, cnfe);
      }
      // make sure the script processor specified in the metadata
      // does indeed implement the ScriptProcessor interface
      if (!ScriptProcessor.class.isAssignableFrom(scriptProcessor))
      {
         throw new RuntimeException("Script processor " + scriptProcessorClassName + " does not implement "
               + ScriptProcessor.class);
      }
      try
      {
         return (ScriptProcessor) scriptProcessor.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not instantiate script processor " + scriptProcessorClassName, e);
      }

   }
}
