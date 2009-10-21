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
import java.net.URL;
import java.util.jar.JarFile;

import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.exception.InvalidPackageException;
import org.jboss.ejb3.packagemanager.exception.PackageRetrievalException;
import org.jboss.ejb3.packagemanager.metadata.Package;
import org.jboss.ejb3.packagemanager.retriever.PackageRetriever;
import org.jboss.ejb3.packagemanager.retriever.impl.PackageRetrievalFactory;
import org.jboss.ejb3.packagemanager.util.IOUtil;
import org.jboss.ejb3.packagemanager.xml.PackageUnmarshaller;

/**
 * DefaultPackageContext
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DefaultPackageContext implements PackageContext
{

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
   private org.jboss.ejb3.packagemanager.metadata.Package pkg;

   /**
    * Constructs a package context out of a package source, for 
    * a package manager context
    * 
    * @param pkgMgrCtx Package manager context
    * @param packageSrc Source of the package
    */
   public DefaultPackageContext(PackageManagerContext pkgMgrCtx, URL packageSrc) throws InvalidPackageException
   {
      this.packageSource = packageSrc;
      this.pkgMgrCtx = pkgMgrCtx;
      initPackageContext();
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

   /**
    * @see PackageContext#getPackage()
    */
   @Override
   public Package getPackage()
   {
      return this.pkg;
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
