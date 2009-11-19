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
package org.jboss.ejb3.packagemanager.db;

import java.util.Set;

import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.entity.PersistentPackage;
import org.jboss.ejb3.packagemanager.exception.PackageNotInstalledException;

/**
 * PackageDatabaseManager
 * 
 * Manages the database used by the Package manager for tracking the installed
 * packages. 
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface PackageDatabaseManager
{

   /**
    * Store the information, in DB, about a package that was installed.
    * 
    * This method is called to record the result of a successful package
    * installation
    *  
    * @param pkgCtx The package which was installed.
    * @return Returns the {@link PersistentPackage} representing the newly
    * installed package
    */
   PersistentPackage installPackage(PackageContext pkgCtx);
   
   /**
    * Returns a package with the package name - <code>name</code>, which is already installed. 
    * If such a package is not installed then {@link PackageNotInstalledException} is thrown.
    * If the caller is not sure whether the package with a given name is installed, then
    * use the {@link #isPackageInstalled(String)} method, before calling this method
    *   
    * @param name Name of the package
    * @return
    * @throws PackageNotInstalledException If the package with the name <code>name</code> is 
    * not installed
    */
   PersistentPackage getInstalledPackage(String name) throws PackageNotInstalledException;
   
   /**
    * Returns true if the package with the given <code>name</code> is already installed.
    * Else returns false.
    * 
    * @param name Name of the package
    * @return
    */
   boolean isPackageInstalled(String name);
   
   /**
    * Returns a set of {@link PersistentPackage}s which depend on the package with the
    * name <code>name</code>. If there are no such dependent packages then an empty
    * set is returned.
    * 
    * @param name Name of the package which has to be checked for dependent packages
    * @return
    * @throws If the package with <code>name</code> is not installed, then {@link PackageNotInstalledException}
    * is thrown
    */
   Set<PersistentPackage> getDependentPackages(String name) throws PackageNotInstalledException;
   
   /**
    * Removes the package from the DB records.
    * 
    * @param name The name of the package to remove
    * @throws PackageNotInstalledException If the package with name <code>name</code> is
    * not already installed
    */
   void removePackage(String name) throws PackageNotInstalledException;
   
   /**
    * Removes the package from the DB records.
    * 
    * @param installedPackage The package which has been installed
    */
   void removePackage(PersistentPackage installedPackage);
   
   /**
    * Returns the names of all installed packages. Returns an empty set if there 
    * are no packages installed
    * 
    * @return
    */
   Set<String> getAllInstalledPackages();
   
}
