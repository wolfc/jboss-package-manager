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

import javax.transaction.TransactionManager;

import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.exception.PackageNotInstalledException;

/**
 * PackageManager
 * 
 * Package Manager can be used to install/update/remove packages against
 * JBoss Application Server. 
 * TODO: More javadocs (once we have something usable)
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
    * @param packageURL The URL of the package file
    */
   void updatePackage(URL packageURL) throws PackageManagerException;
   
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
    * @param packageURL The URL of the package file
    * @throws PackageManagerException If any exceptions occur during installation
    */
   void installPackage(URL packageURL) throws PackageManagerException;
   
   /**
    * Removes a package using the name of the package
    * @param packageName Name of the package to be uninstalled
    */
   void removePackage(String packageName) throws PackageNotInstalledException, PackageManagerException;
   
   
   /**
    * A package manager works against a JBoss AS server. This method returns the 
    * absolute path of the JBoss AS Home.
    * 
    * @return Returns the JBoss Server Home
    */
   String getServerHome();
   
   TransactionManager getTransactionManager();
   
   
}
