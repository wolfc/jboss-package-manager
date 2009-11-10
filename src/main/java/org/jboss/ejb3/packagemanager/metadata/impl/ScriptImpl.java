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

import org.jboss.ejb3.packagemanager.metadata.PackageInstallationPhase;
import org.jboss.ejb3.packagemanager.metadata.ScriptType;

/**
 * ScriptImpl
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class ScriptImpl implements ScriptType
{

   /**
    * The script file name (default is package-script.xml)
    */
   private String scriptFileName = "package-script.xml";

   /**
    * Path to the script file. Path is relative to the location
    * of package.xml of the package. A null value means this 
    * script is at the same location as the package.xml
    */
   private String path;

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.ScriptType#getName()
    */
   public String getName()
   {
      return this.scriptFileName;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.ScriptType#setName(String)
    */
   public void setName(String name)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("Script file name cannot be null");
      }
      this.scriptFileName = name;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.ScriptType#getPath()
    */
   @Override
   public String getPath()
   {
      return this.path;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.ScriptType#setPath(java.lang.String)
    */
   @Override
   public void setPath(String path)
   {
      this.path = path;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.ScriptType#getScriptExecutionPhase()
    */
   public abstract PackageInstallationPhase getScriptExecutionPhase();
   
   /**
    * @see org.jboss.ejb3.packagemanager.metadata.ScriptType#isPersistent()
    */
   public abstract boolean isPersistent();

}
