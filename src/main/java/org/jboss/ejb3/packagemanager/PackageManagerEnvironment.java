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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
   
   private File packageManagerScriptStoreDir;
   
   private File packageManagerDataDir;

   private Map<String, String> properties = new HashMap<String, String>();

   /**
    * 
    */
   public PackageManagerEnvironment(String home)
   {
      this(home, new HashMap<String, String>());
   }

   public PackageManagerEnvironment(String home, Map<String, String> props)
   {
      this.packageManagerHome = new File(home);
      this.properties = props == null ? new HashMap<String, String>() : props;

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
      this.packageManagerDataDir = new File(packageManagerHome, "data");
      if (!this.packageManagerDataDir.exists())
      {
         this.packageManagerDataDir.mkdirs();
      }
      this.packageManagerScriptStoreDir = new File(packageManagerDataDir, "scripts");
      if (!this.packageManagerScriptStoreDir.exists())
      {
         this.packageManagerScriptStoreDir.mkdirs();
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
   
   public File getDataDir()
   {
      return this.packageManagerDataDir;
   }
   
   public File getScriptStoreDir()
   {
      return this.packageManagerScriptStoreDir;
   }

   public String getProperty(String propertyName)
   {
      return this.properties.get(propertyName);
   }

   public void setProperty(String propertyName, String propertyValue)
   {
      this.properties.put(propertyName, propertyValue);

   }

   public Map<String, String> getProperties()
   {
      return Collections.unmodifiableMap(this.properties);
   }
}
