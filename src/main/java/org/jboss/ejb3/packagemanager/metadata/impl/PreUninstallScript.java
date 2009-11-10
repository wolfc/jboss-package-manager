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
import org.jboss.ejb3.packagemanager.metadata.PostUnInstallType;
import org.jboss.ejb3.packagemanager.metadata.PreUnInstallType;

/**
 * PreUninstallScript
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PreUninstallScript extends ScriptImpl
{

   /**
    * The pre-uninstall step to which this script 
    * belongs
    */
   private PreUnInstallType preUninstallMeta;

   /**
    * Constructor
    * 
    * @param preUninstall The {@link PostUnInstallType} to which this
    * pre-uninstall script belongs to
    */
   public PreUninstallScript(PreUnInstallType preUninstall)
   {
      this.preUninstallMeta = preUninstall;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.impl.ScriptImpl#getScriptExecutionPhase()
    */
   @Override
   public PackageInstallationPhase getScriptExecutionPhase()
   {
      return PackageInstallationPhase.PRE_UNINSTALL;
   }

   /**
    * Returns the {@link PreUnInstallType} to which this pre-uninstall script
    * belongs to
    * 
    * @return
    */
   public PreUnInstallType getPreUnInstallMetadata()
   {
      return this.preUninstallMeta;
   }
   
   /**
    * Pre uninstall scripts are persistent
    * @see org.jboss.ejb3.packagemanager.metadata.impl.ScriptImpl#isPersistent()
    */
   @Override
   public boolean isPersistent()
   {
      return true;
   }
}
