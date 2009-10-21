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
package org.jboss.ejb3.packagemanager.impl;

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;

/**
 * DefaultPackageManagerContext
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DefaultPackageManagerContext implements PackageManagerContext
{

   /**
    * The package manager to which this context corresponds
    */
   private PackageManager pkgMgr;

   /**
    * Constructs a context for the {@code pkgMgr}
    *  
    * @param pkgMgr The package manager to which this context corresponds
    */
   public DefaultPackageManagerContext(PackageManager pkgMgr)
   {
      this.pkgMgr = pkgMgr;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManagerContext#getJBossServerHome()
    */
   @Override
   public String getJBossServerHome()
   {
      return this.pkgMgr.getServerHome();
   }

   /**
    * @see org.jboss.ejb3.packagemanager.PackageManagerContext#getPackageManagerEnvironment()
    */
   @Override
   public PackageManagerEnvironment getPackageManagerEnvironment()
   {
      return this.pkgMgr.getPackageManagerEnvironment();
   }

}