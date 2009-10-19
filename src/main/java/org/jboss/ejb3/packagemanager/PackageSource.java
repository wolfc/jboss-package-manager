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
package org.jboss.ejb3.packagemanager;

import java.io.File;

import org.jboss.ejb3.packagemanager.metadata.Package;
import org.jboss.ejb3.packagemanager.xml.PackageUnmarshaller;

/**
 * PackageSource
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PackageSource
{

   private File pkgSource;

   private Package packageMetadata;

   /**
    * 
    */
   public PackageSource(String packageFilePath)
   {
      this(new File(packageFilePath));
   }

   public PackageSource(File src)
   {
      this.pkgSource = src;
      initMetadata();
   }

   private void initMetadata()
   {
      File packageXmlFile = new File(this.pkgSource, "package.xml");
      try
      {
         this.packageMetadata = new PackageUnmarshaller().unmarshal(packageXmlFile.toURL());
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create metadata from package.xml file " + packageXmlFile, e);
      }
   }

   public Package getPackageMetadata()
   {
      return this.packageMetadata;
   }

   public File getSource()
   {
      return this.pkgSource;
   }
   
   /**
    * Returns the string representation of this {@link PackageSource}
    */
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append("Package [source = ");
      sb.append(this.pkgSource);
      sb.append(" ,name = ");
      sb.append(this.packageMetadata.getName());
      sb.append(" ,version = ");
      sb.append(this.packageMetadata.getVersion());
      sb.append(" ]");
      return sb.toString();
   }
}
