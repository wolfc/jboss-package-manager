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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.dependency.DependencyManager;
import org.jboss.ejb3.packagemanager.dependency.impl.IvyDependencyManager;
import org.jboss.ejb3.packagemanager.exception.DependencyResoultionException;
import org.jboss.ejb3.packagemanager.exception.InvalidPackageException;
import org.jboss.ejb3.packagemanager.exception.PackageRetrievalException;
import org.jboss.ejb3.packagemanager.metadata.DependenciesType;
import org.jboss.ejb3.packagemanager.metadata.InstallFileType;
import org.jboss.ejb3.packagemanager.metadata.PackageType;
import org.jboss.ejb3.packagemanager.metadata.PackagedDependency;
import org.jboss.ejb3.packagemanager.metadata.PostInstallType;
import org.jboss.ejb3.packagemanager.metadata.PostUnInstallType;
import org.jboss.ejb3.packagemanager.metadata.PreInstallType;
import org.jboss.ejb3.packagemanager.metadata.PreUnInstallType;
import org.jboss.ejb3.packagemanager.metadata.SystemRequirementsType;
import org.jboss.ejb3.packagemanager.metadata.UnProcessedDependenciesType;
import org.jboss.ejb3.packagemanager.metadata.impl.PostInstallScript;
import org.jboss.ejb3.packagemanager.metadata.impl.PostUnInstallScript;
import org.jboss.ejb3.packagemanager.metadata.impl.PreInstallScript;
import org.jboss.ejb3.packagemanager.metadata.impl.PreUninstallScript;
import org.jboss.ejb3.packagemanager.retriever.PackageRetriever;
import org.jboss.ejb3.packagemanager.retriever.impl.PackageRetrievalFactory;
import org.jboss.ejb3.packagemanager.util.IOUtil;
import org.jboss.ejb3.packagemanager.xml.PackageUnmarshaller;
import org.jboss.logging.Logger;

/**
 * DefaultPackageContext
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DefaultPackageContext implements PackageContext
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(DefaultPackageContext.class);

   /**
    * Source of the of the package
    */
   private URL packageSource;

   /**
    * The root folder where the package is extracted during
    * package manager operation(s)
    */
   private File packageRoot;

   /**
    * The package manager context
    */
   private PackageManagerContext pkgMgrCtx;

   /**
    * The package metadata
    */
   private org.jboss.ejb3.packagemanager.metadata.PackageType pkg;

   /**
    * Set of dependency packages for this package context
    */
   private Set<PackageContext> dependencyPackages = new HashSet<PackageContext>();

   /**
    * Constructs a package context out of a package source, for 
    * a package manager context
    * 
    * @param pkgMgrCtx Package manager context
    * @param packageSrc Source of the package
    */
   public DefaultPackageContext(PackageManagerContext pkgMgrCtx, URL packageSrc) throws InvalidPackageException,
         DependencyResoultionException
   {
      this.packageSource = packageSrc;
      this.pkgMgrCtx = pkgMgrCtx;
      initPackageContext();
      initPackageDependencies();
   }

   /**
    * @see PackageContext#getPackageSource()
    */
   @Override
   public URL getPackageSource()
   {
      return this.packageSource;
   }

   /**
    * @see PackageContext#getPackageRoot()
    */
   @Override
   public File getPackageRoot()
   {
      return this.packageRoot;
   }

   public PackageType getPackage()
   {
      return this.pkg;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageContext#getDependencyPackages()
    */
   @Override
   public Set<PackageContext> getDependencyPackages()
   {
      return this.dependencyPackages;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageContext#getInstallationFiles()
    */
   @Override
   public List<InstallFileType> getInstallationFiles()
   {
      return Collections.unmodifiableList(this.pkg.getFiles());
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageContext#getPackageName()
    */
   @Override
   public String getPackageName()
   {
      return this.pkg.getName();
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageContext#getPackageVersion()
    */
   @Override
   public String getPackageVersion()
   {
      return this.pkg.getVersion();
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageContext#getPostInstallScripts()
    */
   @Override
   public List<PostInstallScript> getPostInstallScripts()
   {
      PostInstallType postInstall = this.pkg.getPostInstall();
      if (postInstall == null)
      {
         return Collections.EMPTY_LIST;
      }
      List<PostInstallScript> postInstallScripts = postInstall.getScripts();
      if (postInstallScripts == null)
      {
         return Collections.EMPTY_LIST;
      }
      return Collections.unmodifiableList(postInstallScripts);

   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageContext#getPreInstallScripts()
    */
   @Override
   public List<PreInstallScript> getPreInstallScripts()
   {
      PreInstallType preInstall = this.pkg.getPreInstall();
      if (preInstall == null)
      {
         return Collections.EMPTY_LIST;
      }
      List<PreInstallScript> preInstallScripts = preInstall.getScripts();
      if (preInstallScripts == null)
      {
         return Collections.EMPTY_LIST;
      }
      return Collections.unmodifiableList(preInstallScripts);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageContext#getPostUnInstallScripts()
    */
   @Override
   public List<PostUnInstallScript> getPostUnInstallScripts()
   {
      PostUnInstallType postUnInstall = this.pkg.getPostUninstall();
      if (postUnInstall == null)
      {
         return Collections.EMPTY_LIST;
      }
      List<PostUnInstallScript> postUnInstallScripts = postUnInstall.getScripts();
      if (postUnInstallScripts == null)
      {
         return Collections.EMPTY_LIST;
      }
      return Collections.unmodifiableList(postUnInstallScripts);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageContext#getPreUnInstallScripts()
    */
   @Override
   public List<PreUninstallScript> getPreUnInstallScripts()
   {
      PreUnInstallType preUnInstall = this.pkg.getPreUninstall();
      if (preUnInstall == null)
      {
         return Collections.EMPTY_LIST;
      }
      List<PreUninstallScript> preUnInstallScripts = preUnInstall.getScripts();
      if (preUnInstallScripts == null)
      {
         return Collections.EMPTY_LIST;
      }
      return Collections.unmodifiableList(preUnInstallScripts);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageContext#getSystemRequirements()
    */
   @Override
   public SystemRequirementsType getSystemRequirements()
   {
      return this.pkg.getSystemRequirements();
   }

   
   private void initPackageDependencies() throws DependencyResoultionException, InvalidPackageException
   {
      // packaged dependencies
      this.dependencyPackages.addAll(this.getPackagedDependencyPackages());
      // unprocessed dependencies
      this.dependencyPackages.addAll(this.getUnprocessedDependencyPackages());
   }

   private Set<PackageContext> getUnprocessedDependencyPackages() throws DependencyResoultionException
   {
      DependenciesType dependencies = this.pkg.getDependencies();
      if (dependencies == null || dependencies.getUnProcessedDependencies() == null)
      {
         logger.debug("No unprocessed dependencies for " + this);
         return Collections.EMPTY_SET;
      }
      UnProcessedDependenciesType unProcessedDeps = dependencies.getUnProcessedDependencies();
      File dependencyFile = new File(this.getPackageRoot(), unProcessedDeps.getFile());
      if (!dependencyFile.exists())
      {
         throw new DependencyResoultionException("Dependency file " + dependencyFile + " not found for " + this);
      }
      DependencyManager depManager = this.getDependencyManager(unProcessedDeps);
      if (depManager == null)
      {
         // TODO: Revisit this
         return Collections.EMPTY_SET;
      }
      Set<PackageContext> dependencyPackages = depManager.resolveDepedencies(this.pkgMgrCtx, this, unProcessedDeps);
      return dependencyPackages;
   }

   private Set<PackageContext> getPackagedDependencyPackages() throws InvalidPackageException,
         DependencyResoultionException
   {

      DependenciesType dependencies = this.pkg.getDependencies();
      if (dependencies == null || dependencies.getPackagedDependencies() == null
            || dependencies.getPackagedDependencies().isEmpty())
      {
         logger.debug("No packaged dependency for " + this);
         return Collections.EMPTY_SET;
      }
      List<PackagedDependency> packagedDeps = dependencies.getPackagedDependencies();
      Set<PackageContext> depPackageCtxs = new HashSet<PackageContext>();
      for (PackagedDependency packagedDep : packagedDeps)
      {
         String relativePathToDependencyPackage = packagedDep.getFile();
         File dependencyPackage = new File(this.getPackageRoot(), relativePathToDependencyPackage);
         if (!dependencyPackage.exists())
         {
            throw new DependencyResoultionException("packaged-dependency file " + dependencyPackage + " does not exist");
         }
         PackageContext dependencyPkgCtx;
         try
         {
            dependencyPkgCtx = new DefaultPackageContext(this.pkgMgrCtx, dependencyPackage.toURI().toURL());
            depPackageCtxs.add(dependencyPkgCtx);

         }
         catch (MalformedURLException mue)
         {
            throw new RuntimeException(mue);
         }
      }
      return depPackageCtxs;
   }

   /**
    * Retrieves the package from the package source and 
    * extracts it the package manager's build folder. It also parses the package.xml 
    * file in the package and creates metadata out of it.
    * 
    */
   private void initPackageContext() throws InvalidPackageException
   {
      // retrieve the package and extract it to our build folder
      PackageRetriever pkgRetriever = PackageRetrievalFactory.getPackageRetriever(this.packageSource);
      try
      {
         File pkg = pkgRetriever.retrievePackage(this.pkgMgrCtx, this.packageSource);
         // the directory to which the package will be extracted
         this.packageRoot = new File(pkgMgrCtx.getPackageManagerEnvironment().getPackageManagerBuildDir(), pkg
               .getName());
         if (!this.packageRoot.exists())
         {
            this.packageRoot.mkdirs();
         }
         JarFile jar = new JarFile(pkg);
         IOUtil.extractJarFile(this.packageRoot, jar);
         // validate that it contains a package.xml
         File packageXml = new File(this.packageRoot, "package.xml");
         if (!packageXml.exists())
         {
            throw new InvalidPackageException(pkg + " is not a valid package - it does not contain a package.xml");
         }
      }
      catch (PackageRetrievalException pre)
      {
         throw new RuntimeException(pre);
      }
      catch (IOException ioe)
      {
         throw new RuntimeException("Exception while creating context for " + this.packageSource, ioe);
      }

      // now that package has been retrieved and extracted, lets parse the package.xml
      this.initPackageMetadata();
   }

   /**
    * Parses the package.xml file and creates metadata out of it
    */
   private void initPackageMetadata()
   {
      File packageXmlFile = new File(this.packageRoot, "package.xml");
      try
      {
         this.pkg = new PackageUnmarshaller().unmarshal(packageXmlFile.toURI().toURL());
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create metadata from package.xml file " + packageXmlFile, e);
      }
   }

   private DependencyManager getDependencyManager(UnProcessedDependenciesType dependencies)
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

   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append("Package [name=");
      String pkgName = this.pkg == null ? null : this.pkg.getName();
      sb.append(pkgName);
      sb.append(" ,version=");
      String pkgVersion = this.pkg == null ? null : this.pkg.getVersion();
      sb.append(pkgVersion);
      //      sb.append(" ,source=");
      //      sb.append(this.packageSource);
      //      sb.append(" ,root=");
      //      sb.append(this.packageRoot);
      sb.append("]");
      return sb.toString();

   }

   
}
