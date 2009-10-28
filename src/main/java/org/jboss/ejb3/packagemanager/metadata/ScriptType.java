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
    * Returns the file path (including the file name) relative to 
    * the location of the package.xml in the package 
    * @return
    */
   String getFile();
   
   /**
    * Sets the file path (including the name of the file)
    * 
    * @param scriptFile Path relative to the location of package.xml in the package 
    */
   void setFile(String scriptFile);
   
//   /**
//    * Returns the fully qualified name of the class which implements
//    * {@link ScriptProcessor} and is responsible for processing the 
//    * {@link #getFile()} script file
//    * @return
//    */
//   String getProcessor();
//   
//   /**
//    * Sets the script processor which is responsible for processing the
//    * {@link #getFile()} script file
//    * 
//    * @param scriptProcessor Fully qualified name of the class which implements
//    *   {@link ScriptProcessor} 
//    */
//   void setProcessor(String scriptProcessor);
   
   /**
    * Returns the package installation phase, during which this script
    * is expected to be executed
    *  
    * @return
    */
   PackageInstallationPhase getScriptExecutionPhase();
}
