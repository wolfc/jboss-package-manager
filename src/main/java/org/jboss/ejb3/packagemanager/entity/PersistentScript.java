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

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForceDiscriminator;

/**
 * Script
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Entity
@Table (name="script")
@Inheritance (strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn (name="scriptType")
// Hibernate specific issue - Without the "ForcedDiscriminator" annotation
// it fetches both pre-uninstall/post-uninstall scripts in the set maintained in InstalledPackage
// OneToMany association. (i.e. if ForceDiscriminator is not used, 
// thenHibernate does not use the discriminator column in the where clause which fetching the set, 
// it just uses the join column). See https://forum.hibernate.org/viewtopic.php?t=961213
@ForceDiscriminator
public abstract class PersistentScript
{

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   protected long id;

   protected String name;
   
   protected String path;
   
   

   protected PersistentScript()
   {
      // for jpa
   }
   
   protected PersistentScript(String fileName, String path)
   {
      this.name = fileName;
      this.path = path;
   }
   
   
   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   public String getScriptFile()
   {
      return name;
   }

   public void setScriptFile(String scriptFile)
   {
      this.name = scriptFile;
   }

   

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

   
   
   
}
