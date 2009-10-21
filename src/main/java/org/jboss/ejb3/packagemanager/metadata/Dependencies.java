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
 * Represents the metadata for dependencies within a package.
 * 
 * A package can have dependencies on other packages. Dependencies can be specified
 *               and provided in 2 ways.
 *                   1) A package which depends on other packages can package those dependencies
 *               within it package jar/zip file. It can then use the "package-dependency"
 *               element to list such dependencies. 
 *                   2) Dependencies can be listed separately in a file. The "unprocessed-dependencies" element points
 *               to that file.
 *               Irrespective of how the dependencies are specified, the dependencies must always be packages.
 *             
 * @see Package
 * @author Jaikiran Pai
 * 
 */
public interface Dependencies
{

   /**
    * Returns the list of packaged dependencies of a package
    * @return
    */
   List<PackagedDependency> getPackagedDependencies();
   
   /**
    * Sets the list of packaged dependencies of a package
    * @param packagedDependencies
    */
   void setPackagedDependencies(List<PackagedDependency> packagedDependencies);
   
   /**
    * Adds a packaged dependency to the list of packaged dependencies
    * 
    * @param packagedDep
    */
   void addPackagedDependency(PackagedDependency packagedDep);
   
   /**
    * Returns the unprocessed dependencies of a package
    * @return
    */
   UnProcessedDependencies getUnProcessedDependencies();
   
   /**
    * Sets the unprocessed dependencies of a package
    *  
    * @param unProcessedDependencies
    */
   void setUnProcessedDependencies(UnProcessedDependencies unProcessedDependencies);
   
   /**
    * Returns the package to which this dependencies correspond to
    * 
    * @return
    */
   Package getPackage();
}
