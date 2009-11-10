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
import org.jboss.ejb3.packagemanager.metadata.PreInstallType;

/**
 * PreInstallScript
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PreInstallScript extends ScriptImpl
{
   /**
    * The pre-install step to which this script 
    * belongs
    */
   private PreInstallType preInstallMeta;

   /**
    * Constructor
    * 
    * @param preInstallMeta The {@link PreInstallType} to which this
    * pre-install script belongs to
    */
   public PreInstallScript(PreInstallType preInstallMeta)
   {
      this.preInstallMeta = preInstallMeta;
   }
   
   /**
    * @see org.jboss.ejb3.packagemanager.metadata.impl.ScriptImpl#getScriptExecutionPhase()
    */
   @Override
   public PackageInstallationPhase getScriptExecutionPhase()
   {
      return PackageInstallationPhase.PRE_INSTALL;
   }
   
   /**
    * Returns the {@link PreInstallType} to which this pre-install script
    * belongs to
    * 
    * @return
    */
   public PreInstallType getPreInstallMetadata()
   {
      return this.preInstallMeta;
   }
   
   /**
    * Pre install scripts are not persistent
    * @see org.jboss.ejb3.packagemanager.metadata.impl.ScriptImpl#isPersistent()
    */
   @Override
   public boolean isPersistent()
   {
      return false;
   }

}
