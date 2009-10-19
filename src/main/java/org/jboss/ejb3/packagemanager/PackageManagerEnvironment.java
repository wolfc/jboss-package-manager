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

/**
 * PackageManagerEnvironment
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PackageManagerEnvironment
{

   private File packageManagerHome;

   private File packageManagerBuildDir;

   private File packageManagerTmpDir;

   /**
    * 
    */
   public PackageManagerEnvironment(String home)
   {
      this.packageManagerHome = new File(home);
      if (!this.packageManagerHome.exists() || !this.packageManagerHome.isDirectory())
      {
         throw new RuntimeException("Package manager home " + home + " doesn't exist or is not a directory");

      }
      initEnvironment();
   }

   private void initEnvironment()
   {
      this.packageManagerBuildDir = new File(packageManagerHome, "build");
      if (!this.packageManagerBuildDir.exists())
      {
         this.packageManagerBuildDir.mkdirs();
      }
      this.packageManagerTmpDir = new File(packageManagerHome, "tmp");
      if (!this.packageManagerTmpDir.exists())
      {
         this.packageManagerTmpDir.mkdirs();
      }
   }

   public File getPackageManagerHome()
   {
      return this.packageManagerHome;
   }

   public File getPackageManagerBuildDir()
   {
      return this.packageManagerBuildDir;
   }

   public File getPackageManagerTmpDir()
   {
      return this.packageManagerTmpDir;
   }
}
