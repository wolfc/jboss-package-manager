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
import org.jboss.ejb3.packagemanager.metadata.Script;

/**
 * ScriptMetadataImpl
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class ScriptImpl implements Script
{

   /**
    * The script processor
    */
   private String scriptProcessor;

   /**
    * The script file
    */
   private String scriptFile;

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Script#getFile()
    */
   public String getFile()
   {
      return this.scriptFile;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Script#getProcessor()
    */
   public String getProcessor()
   {
      return this.scriptProcessor;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Script#setFile(java.lang.String)
    */
   public void setFile(String scriptFile)
   {
      if (scriptFile == null)
      {
         throw new IllegalArgumentException("Script file value cannot be null");
      }
      this.scriptFile = scriptFile;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Script#setProcessor(java.lang.String)
    */
   public void setProcessor(String scriptProcessor)
   {
      this.scriptProcessor = scriptProcessor;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Script#getScriptExecutionPhase()
    */
   public abstract PackageInstallationPhase getScriptExecutionPhase();

}
