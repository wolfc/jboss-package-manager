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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * InstallationFile
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Entity
@Table(name="installation_file")
public class InstalledFile
{

   @Id
   @GeneratedValue (strategy = GenerationType.IDENTITY)
   private long id;
   
   private String fileName;
   
   private String installedPath;
   
   private String fileType;
   
   @ManyToOne 
   @JoinColumn(name="package_name")
   private InstalledPackage pkg;
   
   private InstalledFile()
   {
      // for jpa
   }
   
   /**
    * Constructor
    * 
    * @param fileName The name of the file
    * @param pathWhereInstalled The path, relative to JBOSS_HOME where this file is installed
    */
   public InstalledFile(String fileName, String pathWhereInstalled)
   {
      this.fileName = fileName;
      this.installedPath = pathWhereInstalled;
   }

   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   public String getFileName()
   {
      return fileName;
   }

   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   public String getInstalledPath()
   {
      return installedPath;
   }

   public void setInstalledPath(String installedPath)
   {
      this.installedPath = installedPath;
   }

   public String getFileType()
   {
      return fileType;
   }

   public void setFileType(String fileType)
   {
      this.fileType = fileType;
   }

   public InstalledPackage getPkg()
   {
      return pkg;
   }

   public void setPkg(InstalledPackage pkg)
   {
      this.pkg = pkg;
   }
   
   
   
}
