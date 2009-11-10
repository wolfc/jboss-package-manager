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

import org.jboss.ejb3.packagemanager.metadata.impl.PostUnInstallScript;
import org.jboss.ejb3.packagemanager.metadata.impl.PreUninstallScript;


/**
 * ScriptMetadata
 *
 * Represents the metadata for a script element used in pre-install
 * or post-install of a package.
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface ScriptType
{

   /**
    * Returns the name of the file
    * @return
    */
   String getName();
   
   /**
    * Sets the name of the file
    * 
    * @param name Name of the script file 
    */
   void setName(String name);
   
   /**
    * Sets the path of this script file.
    * @param path
    */
   void setPath(String path);
   
   /**
    * Returns the path of this script file. Path is relative to
    * the location of package.xml of the package to which this
    * script belongs
    * @return
    */
   String getPath();
   
   /**
    * Returns the package installation phase, during which this script
    * is expected to be executed
    *  
    * @return
    */
   PackageInstallationPhase getScriptExecutionPhase();
   
   /**
    * Returns true, if the script has to be stored, during installation, to some
    * location (usually within the package manager home), so that
    * the script is available for use during uninstallation of the package.
    * 
    * Uninstall scripts (pre-uninstall and post-uninstall) are by default persisted
    * @return
    */
   boolean isPersistent();
}
