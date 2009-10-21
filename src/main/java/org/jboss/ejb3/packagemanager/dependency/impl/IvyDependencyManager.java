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
package org.jboss.ejb3.packagemanager.dependency.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.filter.Filter;
import org.apache.ivy.util.filter.FilterHelper;
import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.dependency.DependencyManager;
import org.jboss.ejb3.packagemanager.exception.DependencyResoultionException;
import org.jboss.ejb3.packagemanager.exception.InvalidPackageException;
import org.jboss.ejb3.packagemanager.impl.DefaultPackageContext;
import org.jboss.ejb3.packagemanager.metadata.UnProcessedDependencies;
import org.jboss.logging.Logger;

/**
 * IvyDependencyManager
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class IvyDependencyManager implements DependencyManager
{

   public static final String PROPERTY_IVY_SETTINGS_FILE = "ivy.settings.file";

   /**
    * Logger
    */
   private Logger logger = Logger.getLogger(IvyDependencyManager.class);

   /**
    * 
    * @param pkgMgrContext
    * @param deps
    * @return
    * @throws DependencyResoultionException
    */
   @Override
   public Set<PackageContext> resolveDepedencies(PackageManagerContext pkgMgrContext, PackageContext pkgCtx,
         UnProcessedDependencies deps) throws DependencyResoultionException
   {
      IvySettings ivySettings = new IvySettings();
      // set the basedir to the package root
      ivySettings.setBaseDir(pkgCtx.getPackageRoot());
      logger.info("Ivy basedir is " + ivySettings.getBaseDir());

      // get hold of the ivy settings xml file
      URL ivySettingsFile = this.getIvySettingsFile(pkgMgrContext, pkgCtx);
      if (ivySettingsFile == null)
      {
         throw new DependencyResoultionException("Ivy settings file not found for package manager " + pkgMgrContext);
      }
      logger.info("Ivy settings file " + ivySettingsFile + " being used for dependency resolution of " + pkgCtx);
      // create an ivy instance
      Ivy ivy = Ivy.newInstance(ivySettings);
      try
      {
         // configure Ivy (repos, etc...) through the settings file
         ivy.configure(ivySettingsFile);

         // file containing the list of dependencies (ex: ivy.xml)
         File dependencyFile = new File(pkgCtx.getPackageRoot(), deps.getFile());
         // resolve the dependencies
         ResolveReport resolveReport = ivy.resolve(dependencyFile);
         // check for errors (if any) during resolve
         if (resolveReport.hasError())
         {
            List<String> problems = resolveReport.getAllProblemMessages();
            if (problems != null && !problems.isEmpty())
            {
               StringBuffer errorMsgs = new StringBuffer();
               for (String problem : problems)
               {
                  errorMsgs.append(problem);
                  errorMsgs.append("\n");
               }
               logger.error("Errors encountered during dependency resolution for package " + pkgCtx + " :");
               logger.error(errorMsgs);
               throw new DependencyResoultionException("Dependencies could not be resolved for package " + pkgCtx);
            }
         }
         // Now that the dependencies have been resolved, let now retrieve them
         ModuleDescriptor md = resolveReport.getModuleDescriptor();
         // the dependency packages will be retrieved to a sub folder under the package manager's
         // tmp folder
         File pkgTmpDir = new File(pkgMgrContext.getPackageManagerEnvironment().getPackageManagerTmpDir(), pkgCtx
               .getPackage().getName());
         pkgTmpDir.mkdir();

         ModuleRevisionId mRID = md.getModuleRevisionId();
         RetrieveOptions retrieveOptions = new RetrieveOptions();

         String pattern = pkgTmpDir.getAbsolutePath() + "/[organization]/[module]/[type]/[artifact]-[revision].[ext]";
         retrieveOptions.setDestIvyPattern(pattern);
         // We only retrieve "jar" type artifacts (i.e. we are *not* interested in "source"
         // or "javadoc" or any other artifact types
         Filter jarArtifactFilter = FilterHelper.getArtifactTypeFilter("jar");
         retrieveOptions.setArtifactFilter(jarArtifactFilter);

         // default logging option
         retrieveOptions.setLog(LogOptions.LOG_DEFAULT);
         // retrieve them!
         int packagesRetrieved = ivy.retrieve(mRID, pattern, retrieveOptions);
         logger.info("Retrieved " + packagesRetrieved + " dependencies for package " + pkgCtx);
         Set<PackageContext> depPkgCtxs = new HashSet<PackageContext>();
         // for each of the retrieved packages, create a package context
         for (File depPkg : getJarFilesRecursively(pkgTmpDir))
         {
            PackageContext depPkgCtx;
            try
            {
               depPkgCtx = new DefaultPackageContext(pkgMgrContext, depPkg.toURI().toURL());
               depPkgCtxs.add(depPkgCtx);
            }
            catch (InvalidPackageException e)
            {
               // this is not a package file so skip it and log a WARN message
               logger.warn("Skipping dependency file " + depPkg
                     + " since it's not a package. Was listed as a dependency of " + pkgCtx);
               continue;
            }

         }
         return depPkgCtxs;
      }
      catch (IOException ioe)
      {
         throw new DependencyResoultionException(ioe);
      }
      catch (ParseException pe)
      {
         throw new DependencyResoultionException(pe);
      }

   }

   /**
    * Returns the URL to the Ivy settings file. The Ivy settings file is searched for in the
    * following order:
    * <ol>
    *   <li>
    *       First the <code>pkgCtx</code> is searched for a file named ivy-settings.xml at the 
    *       root of the package {@link PackageContext#getPackageRoot()}. If such a file is 
    *       found, the URL corresponding to it, is returned. 
    *   </li>
    *   <li>
    *       If not found in the package, the JVM system level property {@link #PROPERTY_IVY_SETTINGS_FILE}
    *       is checked to see if any value is set. If set, then the corresponding value is used as the
    *       absolute file path to the Ivy settings file. The URL corresponding to this file is returned
    *   </li>
    *   <li>
    *       If neither of the above to steps find the settings file, then the default ivy-default-settings.xml
    *       packaged within the package manager is used. 
    *   </li>
    *   <li>
    *       If none of the above steps lead to the settings file, then NULL is returned
    *   </li>
    * </ol>
    * 
    * @param pkgMrgCtx Package manager context
    * @param pkgCtx The context of the package being processed
    * @return Returns the URL to the Ivy settings file. If no suitable file is found then this method returns null
    */
   private URL getIvySettingsFile(PackageManagerContext pkgMrgCtx, PackageContext pkgCtx)
   {
      // first check for ivy-settings.xml at the root of the package 
      File ivySettingsInPackage = new File(pkgCtx.getPackageRoot(), "ivy-settings.xml");
      if (ivySettingsInPackage.exists())
      {
         try
         {
            return ivySettingsInPackage.toURI().toURL();
         }
         catch (MalformedURLException mue)
         {
            throw new RuntimeException(mue);
         }
      }
      // The ivy-settings.xml wasn't available in the package, so now check
      // whether the jvm system level property was set to point to the settings file.
      String ivySettingsLocation = System.getProperty(PROPERTY_IVY_SETTINGS_FILE);
      if (ivySettingsLocation != null)
      {
         File userSpecifiedIvySettings = new File(ivySettingsLocation);
         try
         {
            return userSpecifiedIvySettings.toURI().toURL();
         }
         catch (MalformedURLException e)
         {
            throw new RuntimeException("Incorrect location " + userSpecifiedIvySettings + " specified for "
                  + PROPERTY_IVY_SETTINGS_FILE + " property");
         }
      }
      // if the property was *not* set to point to a specific ivy settings file,
      // then fallback on the ivy-default-settings.xml file which is packaged within the package manager jar

      // TODO: This needs to be more fool proof to ensure that we 
      // pick up the ivy-default-settings.xml file from within the package manager
      // jar and not some other file in the classpath. Probably use the CodeSource?
      return pkgMrgCtx.getClass().getClassLoader().getResource("ivy-default-settings.xml");

   }

   /**
    * 
    * @param parent
    * @return
    */
   private List<File> getJarFilesRecursively(File parent)
   {
      List<File> result = new ArrayList<File>();
      File[] filesAndDirs = parent.listFiles();
      List<File> filesDirs = Arrays.asList(filesAndDirs);
      for (File file : filesDirs)
      {
         if (file.isFile() && file.getName().endsWith(".jar"))
         {
            result.add(file);
         }
         else if (file.isDirectory())
         {
            //recursive call!
            List<File> deeperList = getJarFilesRecursively(file);
            result.addAll(deeperList);
         }
      }
      return result;

   }
}
