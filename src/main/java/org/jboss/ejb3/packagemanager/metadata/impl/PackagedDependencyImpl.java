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
package org.jboss.ejb3.packagemanager.metadata.impl;

import org.jboss.ejb3.packagemanager.metadata.Package;
import org.jboss.ejb3.packagemanager.metadata.PackagedDependency;

/**
 * PackagedDependencyImpl
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PackagedDependencyImpl implements PackagedDependency
{

   private Package pkg;
   
   private String file;
   
   public PackagedDependencyImpl(Package pkg)
   {
      this.pkg = pkg;
       
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackagedDependency#getFile()
    */
   @Override
   public String getFile()
   {
      return this.file;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackagedDependency#getPackage()
    */
   @Override
   public Package getPackage()
   {
      return this.pkg;
      
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PackagedDependency#setFile(java.lang.String)
    */
   @Override
   public void setFile(String file)
   {
      if (file == null)
      {
         throw new IllegalArgumentException("A non-null file is mandatory for a packaged-dependency");
      }
      this.file = file;
      
   }
   
}
