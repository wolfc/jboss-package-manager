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

import org.jboss.ejb3.packagemanager.metadata.Dependencies;
import org.jboss.ejb3.packagemanager.metadata.InstallFile;
import org.jboss.ejb3.packagemanager.metadata.Package;
import org.jboss.ejb3.packagemanager.metadata.PostInstall;
import org.jboss.ejb3.packagemanager.metadata.PreInstall;
import org.jboss.ejb3.packagemanager.metadata.SystemRequirements;

public class PackageImpl implements Package
{

   protected SystemRequirements systemRequirements;

   protected List<InstallFile> files;

   protected Dependencies dependencies;

   protected String name;

   protected String version;

   /**
    * Post-install for this package
    */
   protected PostInstall postInstall;

   /**
    * Pre-install for this package
    */
   protected PreInstall preInstall;

   public PackageImpl()
   {

   }

   public SystemRequirements getSystemRequirements()
   {
      return systemRequirements;
   }

   public void setSystemRequirements(SystemRequirements value)
   {
      this.systemRequirements = ((SystemRequirementsImpl) value);
   }

   public List<InstallFile> getFiles()
   {
      return this.files;
   }

   public void setFiles(List<InstallFile> files)
   {
      if (files == null || files.isEmpty())
      {
         throw new IllegalArgumentException("A package is expected to have atleast one file to install");
      }
      this.files = files;
   }

   public Dependencies getDependencies()
   {
      return dependencies;
   }

   public void setDependencies(Dependencies value)
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
    * @see org.jboss.ejb3.packagemanager.metadata.Package#addFile(org.jboss.ejb3.packagemanager.metadata.InstallFile)
    */
   public void addFile(InstallFile file)
   {
      if (file == null)
      {
         return;
      }
      if (this.files == null)
      {
         this.files = new ArrayList<InstallFile>();
      }
      this.files.add(file);

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Package#getPostInstall()
    */
   public PostInstall getPostInstall()
   {
      return this.postInstall;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Package#getPreInstall()
    */
   public PreInstall getPreInstall()
   {
      return this.preInstall;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Package#setPostInstall(org.jboss.ejb3.packagemanager.metadata.PostInstall)
    */
   public void setPostInstall(PostInstall postInstall)
   {
      this.postInstall = postInstall;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Package#setPreInstall(org.jboss.ejb3.packagemanager.metadata.PreInstall)
    */
   public void setPreInstall(PreInstall preInstall)
   {
      this.preInstall = preInstall;

   }

}
