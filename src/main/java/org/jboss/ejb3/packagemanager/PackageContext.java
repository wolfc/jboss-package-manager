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
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jboss.ejb3.packagemanager.metadata.InstallFileType;
import org.jboss.ejb3.packagemanager.metadata.ScriptType;
import org.jboss.ejb3.packagemanager.metadata.SystemRequirementsType;

/**
 * PackageContext
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface PackageContext
{

   /**
    * Returns the source of the package
    * 
    * @return
    */
   URL getPackageSource();

   /**
    * The root of the package where it has been
    * extracted
    * @return
    */
   File getPackageRoot();

   String getPackageName();

   String getPackageVersion();

   /**
    * @return Returns the set of dependency package contexts for this 
    * package context. Returns an empty set if there are no dependencies
    */
   Set<PackageContext> getDependencyPackages();

   SystemRequirementsType getSystemRequirements();

   List<ScriptType> getPostInstallScripts();

   List<ScriptType> getPreInstallScripts();

   List<InstallFileType> getInstallationFiles();
}
