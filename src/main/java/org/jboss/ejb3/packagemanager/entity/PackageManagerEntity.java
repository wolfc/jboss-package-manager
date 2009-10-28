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
package org.jboss.ejb3.packagemanager.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.jboss.ejb3.packagemanager.PackageManagerContext;

/**
 * PackageManagerEntity
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Entity
@Table(name = "package_manager")
public class PackageManagerEntity
{

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long id;

   private String jbossHome;

   private PackageManagerEntity()
   {
      // for jpa
   }

   public PackageManagerEntity(String jbossHome)
   {
      this.jbossHome = jbossHome;
   }

   public PackageManagerEntity(PackageManagerContext pkgMgrCtx)
   {
      this.jbossHome = pkgMgrCtx.getJBossServerHome();
   }

   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   public String getJbossHome()
   {
      return jbossHome;
   }

   public void setJbossHome(String jbossHome)
   {
      this.jbossHome = jbossHome;
   }

}
