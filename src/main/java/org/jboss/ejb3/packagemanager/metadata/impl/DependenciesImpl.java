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
package org.jboss.ejb3.packagemanager.metadata.impl;

import org.jboss.ejb3.packagemanager.metadata.Dependencies;
import org.jboss.ejb3.packagemanager.metadata.Package;

public class DependenciesImpl implements Dependencies
{

   /**
    * The package to which this dependency metadata corresponds
    */
   private Package pkg;

   /**
    * Fully qualified class name of the dependencies manager
    */
   private String depManager;

   /**
    * The file containing the dependencies 
    */
   private String file;

   /**
    * Constructor
    * @param pkgMetadata The package to which this dependencies correspond
    */
   public DependenciesImpl(Package pkgMetadata)
   {
      this.pkg = pkgMetadata;
   }

   /**
    * Returns the file path, which contains the dependency listing.
    * The file path is relative to the location of package.xml in the package
    */
   public String getFile()
   {
      return file;
   }

   /**
    * Sets the file path containing the dependency listing.
    * The file path is relative to the location of package.xml in the package
    */
   public void setFile(String value)
   {
      this.file = value;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Dependencies#getPackage()
    */
   public Package getPackage()
   {
      return this.pkg;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Dependencies#getManager()
    */
   public String getManager()
   {
      return this.depManager;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Dependencies#setManager(java.lang.String)
    */
   public void setManager(String depManager)
   {
      this.depManager = depManager;

   }

}
