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

import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.packagemanager.metadata.PackageType;
import org.jboss.ejb3.packagemanager.metadata.PreInstallType;

/**
 * PreInstallMetadataImpl
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PreInstallImpl implements PreInstallType
{

   /**
    * The package to which this pre-install belongs
    */
   private PackageType pkgMetadata;

   /**
    * pre-install scripts
    */
   private List<PreInstallScript> scripts;

   /**
    * Constructor
    * @param pkgMeta The {@link PackageType} to which this pre-install belongs
    */
   public PreInstallImpl(PackageType pkgMeta)
   {
      this.pkgMetadata = pkgMeta;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PreInstallType#addScript(PreInstallScript)
    */
   public void addScript(PreInstallScript script)
   {
      if (script == null)
      {
         return;
      }
      if (this.scripts == null)
      {
         this.scripts = new ArrayList<PreInstallScript>();
      }
      this.scripts.add(script);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PreInstallType#getPackage()
    */
   public PackageType getPackage()
   {
      return this.pkgMetadata;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PreInstallType#getScripts()
    */
   public List<PreInstallScript> getScripts()
   {
      return this.scripts;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.PreInstallType#setScripts(java.util.List)
    */
   public void setScripts(List<PreInstallScript> scripts)
   {
      if (scripts == null)
      {
         throw new IllegalArgumentException("pre-install scripts cannot be set to null scripts");
      }
      this.scripts = scripts;

   }

}
