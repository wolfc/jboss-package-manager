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

import java.net.URL;
import java.util.Set;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.packagemanager.db.PackageDatabaseManager;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.exception.PackageNotInstalledException;
import org.jboss.ejb3.packagemanager.option.InstallOptions;
import org.jboss.ejb3.packagemanager.option.UnInstallOptions;
import org.jboss.ejb3.packagemanager.option.UpgradeOptions;

/**
 * PackageManager
 * 
 * Package Manager can be used to install/update/remove packages against
 * JBoss Application Server. 
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface PackageManager
{

   /**
    * Returns the environment associated with this package manager
    * @return
    */
   PackageManagerEnvironment getPackageManagerEnvironment();
   
   /**
    * Updates the package
    * 
    * @param packagePath The URL path to the package file
    */
   void updatePackage(String packagePath) throws PackageManagerException;
   
   /**
    * Updates the package
    * 
    * @param packagePath
    * @param upgradeOptions
    * @throws PackageManagerException
    */
   void updatePackage(String packagePath, UpgradeOptions upgradeOptions) throws PackageManagerException;
   
   /**
    * Updates the package
    * 
    * @param packageURL The URL of the package file
    */
   void updatePackage(URL packageURL) throws PackageManagerException;
   
   /**
    * Updates the package
    * 
    * @param packageURL
    * @param upgradeOptions
    * @throws PackageManagerException
    */
   void updatePackage(URL packageURL, UpgradeOptions upgradeOptions) throws PackageManagerException;
   
   /**
    * Installs a package 
    * 
    * @param packagePath The URL path of the package file
    * @throws PackageManagerException If any exceptions occur during installation
    */
   void installPackage(String packagePath) throws PackageManagerException;
   
   /**
    * Installs a package
    * 
    * @param packagePath
    * @param installOptions
    * @throws PackageManagerException
    */
   void installPackage(String packagePath, InstallOptions installOptions) throws PackageManagerException;
   
   /**
    * Installs a package 
    * 
    * @param packageURL The URL of the package file
    * @throws PackageManagerException If any exceptions occur during installation
    */
   void installPackage(URL packageURL) throws PackageManagerException;
   
   /**
    * Installs a package
    * 
    * @param packageURL
    * @param installOptions
    * @throws PackageManagerException
    */
   void installPackage(URL packageURL, InstallOptions installOptions) throws PackageManagerException;
   
   /**
    * Removes a package using the name of the package
    * @param packageName Name of the package to be uninstalled
    */
   void removePackage(String packageName) throws PackageNotInstalledException, PackageManagerException;
   
   /**
    * Removes a package
    * 
    * @param packageName
    * @param uninstallOptions
    * @throws PackageNotInstalledException
    * @throws PackageManagerException
    */
   void removePackage(String packageName, UnInstallOptions uninstallOptions) throws PackageNotInstalledException, PackageManagerException;
   
   /**
    * Returns the names of all installed packages. Returns an empty set if there 
    * are no packages installed
    * 
    * @return
    */
   Set<String> getAllInstalledPackages();
   
   
   /**
    * A package manager works against a JBoss AS server. This method returns the 
    * absolute path of the JBoss AS Home.
    * 
    * @return Returns the JBoss Server Home
    */
   String getServerHome();
   
   /**
    * @return Returns the transaction manager associated with this package manager. The 
    * transaction manager is responsible for managing transactions during the 
    * package manager operations
    */
   TransactionManager getTransactionManager();
   
   /**
    * @return Returns the database manager associated with the package manager.
    * The database manager is responsible for maintaining the package installation
    * details
    */
   PackageDatabaseManager getDatabaseManager();
   
   
}
