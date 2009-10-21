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
 * UnProcessedDependencies
 * 
 *
 * The "unprocessed-dependencies"  element is used to point to the file containing a list of dependencies
 * of the package. The "file" attribute is relative to the location of the package.xml within the package.
 * Optionally a dependency manager can be specified through the "manager" attribute. The manager attribute should
 * contain the fully qualified class name of the dependency manager which is responsible for parsing the
 * dependencies file and managing the dependencies listed in that file. The class should implement the
 * org.jboss.ejb3.packagemanager.dependency.DependencyManager interface.
 * By default, org.jboss.ejb3.packagemanager.dependency.impl.IvyDependencyManager will be
 * used as the dependency manager. 
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface UnProcessedDependencies
{

   /**
    * @return Returns the file path (including the filename) of the dependencies file,
    *  relative  to the location of package.xml file in the package
    */
   String getFile();

   /**
    * Sets the file path (including hte filename) of the dependencies file.
    * 
    * @param depFile Relative file path of the dependencies file of this package
    *     
    */
   void setFile(String depFile);
   
   /**
    * Returns the fully qualified class name of the dependencies manager.
    * 
    * @return
    */
   String getManager();
   
   /**
    * Sets the fully qualified class name of the dependencies manager.
    * 
    * @param depManager Fully qualified class name of the dependencies manager
    */
   void setManager(String depManager);
   
   /**
    * Returns the package for which this is an dependency
    * @return
    */
   Package getPackage();
}
