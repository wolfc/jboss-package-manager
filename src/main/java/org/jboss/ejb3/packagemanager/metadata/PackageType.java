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
package org.jboss.ejb3.packagemanager.metadata;

import java.util.List;

/**
 * Represents the metadata for a package.
 * 
 * A package represents a collection of files that are to be installed into
 *               the JBoss AS.
 *               A package can optionally have dependencies on other packages.
 *               A package can also optionally specify system requirements (example: A package might require a JBoss AS
 *               5.1.0 version of the server)
 *               A package also has optional pre-install, post-install, pre-uninstall, post-uninstall scripts
 *               
 * @author Jaikiran Pai              
 *
 */
public interface PackageType
{

   /**
    * @return Returns the system requirements of this package
    *     
    */
   SystemRequirementsType getSystemRequirements();

   /**
    * Sets the system requirements of this package
    * 
    * @param sysRequirements The system requirements for this package
    *     
    */
   void setSystemRequirements(SystemRequirementsType sysRequirements);

   /**
    * @return Returns the files that are to be installed for this package
    *     
    */
   List<InstallFileType> getFiles();

   /**
    * Sets the files to be installed by the package
    * 
    * @param files The files to be installed by the package
    *     
    */
   void setFiles(List<InstallFileType> files);

   /**
    * Add a file to be installed, into the list of files to install
    * through this package.
    * 
    * @param file File to install
    */
   void addFile(InstallFileType file);

   /**
    * 
    * @return Returns the dependencies of this package
    */
   DependenciesType getDependencies();

   /**
    * Sets the dependencies of this package
    * 
    * @param dependencies Dependencies of this package
    *     
    */
   void setDependencies(DependenciesType deps);

   /**
    * @return Returns the name of this package
    *     
    */
   String getName();

   /**
    * Sets the name of this package
    * 
    * @param name Package name
    *     
    */
   void setName(String name);

   /**
    * @return Returns the version of this package
    *     
    */
   String getVersion();

   /**
    * Sets the version of this package
    * 
    * @param version Package version
    *     
    */
   void setVersion(String version);

   /**
    * Returns any pre-install metadata related to this package
    * 
    * @return
    */
   PreInstallType getPreInstall();

   /**
    * Sets the pre-install related metadata for this package
    * 
    * @param preInstall Pre-install 
    */
   void setPreInstall(PreInstallType preInstall);

   /**
    * Returns any post-install metadata related to this package
    * 
    * @return
    */
   PostInstallType getPostInstall();

   /**
    * Sets the post-install related metadata for this package
    * 
    * @param postInstall Post-install 
    */
   void setPostInstall(PostInstallType postInstall);
   
   /**
    * Returns any pre-uninstall metadata related to this package
    * @return
    */
   PreUnInstallType getPreUninstall();
   
   /**
    * Sets the pre-uninstall metadata for this package
    * @param preUninstall
    */
   void setPreUnInstall(PreUnInstallType preUninstall);
   
   /**
    * Returns any post-uninstall metadata related to this package
    * @return
    */
   PostUnInstallType getPostUninstall();
   
   /**
    * Sets the post-uninstall metadata for this package
    * @param preUninstall
    */
   void setPostUnInstall(PostUnInstallType postUninstall);

}
