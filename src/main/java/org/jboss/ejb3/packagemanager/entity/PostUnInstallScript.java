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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * PostUnInstallScript
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Entity
@DiscriminatorValue(value="post-uninstall")
public class PostUnInstallScript extends Script
{

   // Bad! Hibernate wants this here, instead of on the super class.
   // If this field is made available in the super class, and then
   // there's a one-to-many mapping to this PostUnInstallScript, then
   // you run into:
   // Caused by: org.hibernate.AnnotationException: mappedBy reference an unknown target entity property: 
   // org.jboss.ejb3.packagemanager.entity.PostUnInstallScript.installedPkg 
   // in org.jboss.ejb3.packagemanager.entity.InstalledPackage.postUnInstallScripts
   @ManyToOne 
   @JoinColumn(name="package_name")
   protected InstalledPackage installedPkg;
   
   private PostUnInstallScript()
   {
      //for jpa
   }
   
   public PostUnInstallScript(String fileName, String path)
   {
      super(fileName, path);
   }
   
   /**
    * @param pkg
    * @param scriptFile
    */
   public PostUnInstallScript(InstalledPackage pkg, String fileName, String path)
   {
      super(fileName, path);
      this.installedPkg = pkg;
   }

   public InstalledPackage getInstalledPkg()
   {
      return installedPkg;
   }

   public void setInstalledPkg(InstalledPackage installedPkg)
   {
      this.installedPkg = installedPkg;
   }
}
