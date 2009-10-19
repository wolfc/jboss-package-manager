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
package org.jboss.ejb3.packagemanager.retriever.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageSource;
import org.jboss.ejb3.packagemanager.exception.PackageRetrievalException;
import org.jboss.ejb3.packagemanager.retriever.PackageRetriever;
import org.jboss.ejb3.packagemanager.util.IOUtil;

/**
 * FileSystemPackageRetriever
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class FileSystemPackageRetriever implements PackageRetriever
{

   /**
    * @see org.jboss.ejb3.packagemanager.retriever.PackageRetriever#retrievePackage(org.jboss.ejb3.packagemanager.PackageManager, java.net.URL)
    */
   public PackageSource retrievePackage(PackageManager pkgMgr, URL packagePathURL) throws PackageRetrievalException
   {
      if (packagePathURL == null)
      {
         throw new PackageRetrievalException("Invalid url " + packagePathURL);
      }
      if (!packagePathURL.getProtocol().equals("file"))
      {
         throw new PackageRetrievalException(FileSystemPackageRetriever.class
               + " can only retrieve package from a file: URL. It can't handle " + packagePathURL);
      }
      File pkg = new File(packagePathURL.getFile());
      // TODO: There should be a better way to check for a jar file
      if (!pkg.getName().endsWith(".jar"))
      {
         throw new PackageRetrievalException("File system package retriever can handle only .jar package files. " + pkg
               + " is not a .jar file");
      }
      if (!pkg.exists())
      {
         throw new PackageRetrievalException("Package file " + pkg + " does not exist");
      }
      try
      {
         // the directory to which the package will be extracted
         File extractedPkgDir = new File(pkgMgr.getPackageManagerEnvironment().getPackageManagerBuildDir(), pkg.getName());
         if (!extractedPkgDir.exists())
         {
            extractedPkgDir.mkdirs();
         }
         JarFile jar = new JarFile(pkg);
         IOUtil.extractJarFile(extractedPkgDir, jar);
         // validate that it contains a package.xml
         File packageXml = new File(extractedPkgDir, "package.xml");
         if (!packageXml.exists())
         {
            throw new PackageRetrievalException(pkg + " is not a valid package - it does not contain a package.xml");
         }
         return new PackageSource(extractedPkgDir);
      }
      catch (IOException ioe)
      {
         throw new PackageRetrievalException("Error while processing package file " + pkg, ioe);
      }
   }

}
