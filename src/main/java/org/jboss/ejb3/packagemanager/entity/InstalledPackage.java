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
package org.jboss.ejb3.packagemanager.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Package
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Entity
@Table(name = "package")
public class InstalledPackage
{

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long id;

   private String name;

   private String version;

   @OneToMany(mappedBy = "dependeePackage", cascade=CascadeType.ALL)
   private Set<PackageDependency> dependencies;

   @OneToMany(mappedBy = "pkg", cascade = CascadeType.ALL)
   private Set<InstalledFile> installationFiles;

   @ManyToOne 
   @JoinColumn(name = "package_manager_id")
   private PackageManagerEntity packageManager;

   private InstalledPackage()
   {
      // for JPA
   }

   public InstalledPackage(PackageManagerEntity pkgMgr, String packageName, String packageVersion)
   {
      this.packageManager = pkgMgr;
      this.name = packageName;
      this.version = packageVersion;
   }

   public InstalledPackage(long id)
   {
      this.id = id;
   }

   
   public String getPackageName()
   {
      return this.name;
   }

   public String getPackageVersion()
   {
      return this.version;
   }

   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   public Set<InstalledFile> getInstallationFiles()
   {
      return installationFiles;
   }

   public void setInstallationFiles(Set<InstalledFile> installationFiles)
   {
      this.installationFiles = installationFiles;
   }

   public PackageManagerEntity getPackageManager()
   {
      return packageManager;
   }

   public void setPackageManager(PackageManagerEntity packageManager)
   {
      this.packageManager = packageManager;
   }

   public Set<PackageDependency> getDependencies()
   {
      return dependencies;
   }

   public void setDependencies(Set<PackageDependency> dependencies)
   {
      this.dependencies = dependencies;
   }
   
   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof InstalledPackage))
      {
         return false;
      }
      InstalledPackage otherPackge = (InstalledPackage) obj;
      return this.getId() == otherPackge.getId(); 
      
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return Long.valueOf(this.id).hashCode();
   }
}
