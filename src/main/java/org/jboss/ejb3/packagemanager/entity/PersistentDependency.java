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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * PackageDependency
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Entity
@Table(name = "package_dependency")
public class PersistentDependency
{

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long id;

   @ManyToOne(cascade = CascadeType.REFRESH)
   @JoinColumn(name = "dependent_package")
   private PersistentPackage dependentPackage;

   @ManyToOne(cascade = CascadeType.REFRESH)
   @JoinColumn(name = "dependee_package")
   private PersistentPackage dependeePackage;

   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   public PersistentPackage getDependentPackage()
   {
      return dependentPackage;
   }

   public void setDependentPackage(PersistentPackage dependentPackage)
   {
      this.dependentPackage = dependentPackage;
   }

   public PersistentPackage getDependeePackage()
   {
      return dependeePackage;
   }

   public void setDependeePackage(PersistentPackage dependeePackage)
   {
      this.dependeePackage = dependeePackage;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof PersistentDependency))
      {
         return false;
      }
      PersistentDependency other = (PersistentDependency) obj;
      if (this.dependeePackage != null && this.dependentPackage != null)
      {
         return this.dependeePackage.equals(other.getDependeePackage()) && this.dependentPackage.equals(other.getDependentPackage());
      }
      return false;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      // TODO Auto-generated method stub
      return super.hashCode();
   }
}
