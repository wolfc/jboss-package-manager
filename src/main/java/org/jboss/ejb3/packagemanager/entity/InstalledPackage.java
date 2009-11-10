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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForceDiscriminator;

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
   private String name;

   private String version;

   @OneToMany(mappedBy = "dependentPackage", cascade = CascadeType.ALL)
   private Set<PackageDependency> dependencies;

   @OneToMany(mappedBy = "pkg", cascade = CascadeType.ALL)
   private Set<InstalledFile> installationFiles;

   @OneToMany(mappedBy = "installedPkg", cascade = CascadeType.ALL)
   private Set<PreUnInstallScript> preUnInstallScripts;

   @OneToMany(mappedBy = "installedPkg", cascade = CascadeType.ALL)
   private Set<PostUnInstallScript> postUnInstallScripts;

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

   public String getPackageName()
   {
      return this.name;
   }

   public String getPackageVersion()
   {
      return this.version;
   }

   public Set<InstalledFile> getInstallationFiles()
   {
      return installationFiles;
   }

   public void setInstallationFiles(Set<InstalledFile> installationFiles)
   {
      this.installationFiles = installationFiles;
   }
   
   public void addInstallationFile(InstalledFile file)
   {
      if (this.installationFiles == null)
      {
         this.installationFiles = new HashSet<InstalledFile>();
      }
      this.installationFiles.add(file);
   }

   public Set<PreUnInstallScript> getPreUnInstallScripts()
   {
      return this.preUnInstallScripts;
   }

   public void setPreUnInstallScript(Set<PreUnInstallScript> preUnInstallScripts)
   {
      this.preUnInstallScripts = preUnInstallScripts;
   }

   public void addPreUnInstallScript(PreUnInstallScript preUnInstallScrtipt)
   {
      if (this.preUnInstallScripts == null)
      {
         this.preUnInstallScripts = new HashSet<PreUnInstallScript>();
      }
      this.preUnInstallScripts.add(preUnInstallScrtipt);
   }

   public Set<PostUnInstallScript> getPostUnInstallScripts()
   {
      return this.postUnInstallScripts;
   }

   public void setPostUnInstallScript(Set<PostUnInstallScript> postUnInstallScripts)
   {
      this.postUnInstallScripts = postUnInstallScripts;
   }

   public void addPostUnInstallScript(PostUnInstallScript postUnInstallScript)
   {
      if (this.postUnInstallScripts == null)
      {
         this.postUnInstallScripts = new HashSet<PostUnInstallScript>();
      }
      this.postUnInstallScripts.add(postUnInstallScript);

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

   public void addDependency(PackageDependency dependency)
   {
      if (this.dependencies == null)
      {
         this.dependencies = new HashSet<PackageDependency>();
      }
      this.dependencies.add(dependency);
   }

   public void addDependencies(Set<PackageDependency> dependencies)
   {
      if (this.dependencies == null)
      {
         this.dependencies = new HashSet<PackageDependency>();
      }
      this.dependencies.addAll(dependencies);
   }

   public void removeDependency(PackageDependency dependency)
   {
      if (this.dependencies == null)
      {
         return;
      }
      this.dependencies.remove(dependency);
   }

   public void removeDependency(InstalledPackage dependencyPackage)
   {
      if (this.dependencies == null)
      {
         return;
      }
      Set<PackageDependency> copyOfDependencies = new HashSet<PackageDependency>(this.dependencies);
      for (PackageDependency dependency : copyOfDependencies)
      {
         if (dependency.getDependentPackage().equals(dependencyPackage))
         {
            this.dependencies.remove(dependency);
         }
      }

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
      return this.name == otherPackge.name;

   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return this.name.hashCode();
   }
}
