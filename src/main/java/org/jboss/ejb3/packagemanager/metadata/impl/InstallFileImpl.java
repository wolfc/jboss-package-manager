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

import org.jboss.ejb3.packagemanager.metadata.InstallFileType;
import org.jboss.ejb3.packagemanager.metadata.FileType;
import org.jboss.ejb3.packagemanager.metadata.PackageType;

public class InstallFileImpl implements InstallFileType
{

   private PackageType pkg;

   public InstallFileImpl(PackageType pkgMetadata)
   {
      if (pkgMetadata == null)
      {
         throw new IllegalArgumentException("File metadata cannot be constructed out of a null package metadata");
      }
      this.pkg = pkgMetadata;

   }

   protected String name;

   protected String srcPath;

   protected String destPath;

   protected String type;

   protected FileType fileType;

   public String getName()
   {
      return name;
   }

   public void setName(String value)
   {
      this.name = value;
   }

   public String getSrcPath()
   {
      return srcPath;
   }

   public void setSrcPath(String value)
   {
      this.srcPath = value;
   }

   public String getDestPath()
   {
      return destPath;
   }

   public void setDestPath(String value)
   {
      this.destPath = value;
   }

   public FileType getType()
   {
      return this.fileType;
   }

   public void setType(String ftype)
   {

      if (ftype == null)
      {
         return;
      }

      if (ftype.equals("config"))
      {
         this.setFileType(FileType.CONFIG);
      }
      else if (ftype.equals("library"))
      {
         this.setFileType(FileType.LIBRARY);
      }
      else if (ftype.equals("script"))
      {
         this.setFileType(FileType.SCRIPT);
      }
      else
      {
         throw new IllegalArgumentException("Unrecognized file type " + ftype);
      }
      this.type = ftype;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.InstallFileType#setType(org.jboss.ejb3.packagemanager.metadata.FileType)
    */
   public void setFileType(FileType ftype)
   {
      this.fileType = ftype;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.InstallFileType#getPackage()
    */
   public PackageType getPackage()
   {
      return this.pkg;
   }

}
