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

import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.packagemanager.metadata.DependenciesType;
import org.jboss.ejb3.packagemanager.metadata.InstallFileType;
import org.jboss.ejb3.packagemanager.metadata.PackageType;
import org.jboss.ejb3.packagemanager.metadata.PostInstallType;
import org.jboss.ejb3.packagemanager.metadata.PostUnInstallType;
import org.jboss.ejb3.packagemanager.metadata.PreInstallType;
import org.jboss.ejb3.packagemanager.metadata.PreUnInstallType;
import org.jboss.ejb3.packagemanager.metadata.SystemRequirementsType;

public class PackageImpl implements PackageType
{

   protected SystemRequirementsType systemRequirements;

   protected List<InstallFileType> files;

   protected DependenciesType dependencies;

   protected String name;

   protected String version;

   /**
    * Post-install for this package
    */
   protected PostInstallType postInstall;

   /**
    * Pre-install for this package
    */
   protected PreInstallType preInstall;

   /**
    * Post-UnInstall
    */
   protected PostUnInstallType postUnInstall;

   /**
    * Pre-Uninstall
    */
   protected PreUnInstallType preUnInstall;

   public PackageImpl()
   {

   }

   public SystemRequirementsType getSystemRequirements()
   {
      return systemRequirements;
   }

   public void setSystemRequirements(SystemRequirementsType value)
   {
      this.systemRequirements = ((SystemRequirementsImpl) value);
   }

   public List<InstallFileType> getFiles()
   {
      return this.files;
   }

   public void setFiles(List<InstallFileType> files)
   {
      if (files == null || files.isEmpty())
      {
         throw new IllegalArgumentException("A package is expected to have atleast one file to install");
      }
      this.files = files;
   }

   public DependenciesType getDependencies()
   {
      return dependencies;
   }

   public void setDependencies(DependenciesType value)
   {
      this.dependencies = value;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String value)
   {
      this.name = value;
   }

   public String getVersion()
   {
      return version;
   }

   public void setVersion(String value)
   {
      this.version = value;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackageType#addFile(org.jboss.ejb3.packagemanager.metadata.InstallFileType)
    */
   public void addFile(InstallFileType file)
   {
      if (file == null)
      {
         return;
      }
      if (this.files == null)
      {
         this.files = new ArrayList<InstallFileType>();
      }
      this.files.add(file);

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackageType#getPostInstall()
    */
   public PostInstallType getPostInstall()
   {
      return this.postInstall;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackageType#getPreInstall()
    */
   public PreInstallType getPreInstall()
   {
      return this.preInstall;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackageType#setPostInstall(org.jboss.ejb3.packagemanager.metadata.PostInstallType)
    */
   public void setPostInstall(PostInstallType postInstall)
   {
      this.postInstall = postInstall;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackageType#setPreInstall(org.jboss.ejb3.packagemanager.metadata.PreInstallType)
    */
   public void setPreInstall(PreInstallType preInstall)
   {
      this.preInstall = preInstall;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackageType#getPostUninstall()
    */
   @Override
   public PostUnInstallType getPostUninstall()
   {
      return this.postUnInstall;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackageType#getPreUninstall()
    */
   @Override
   public PreUnInstallType getPreUninstall()
   {
      return this.preUnInstall;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackageType#setPostUnInstall(org.jboss.ejb3.packagemanager.metadata.PostUnInstallType)
    */
   @Override
   public void setPostUnInstall(PostUnInstallType postUninstall)
   {
      this.postUnInstall = postUninstall;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackageType#setPreUnInstall(org.jboss.ejb3.packagemanager.metadata.PreUnInstallType)
    */
   @Override
   public void setPreUnInstall(PreUnInstallType preUninstall)
   {
      this.preUnInstall = preUninstall;

   }

}
