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
package org.jboss.ejb3.packagemanager.metadata;

/**
 * 
 *  A file element contains the information of the source and destination of the file to be installed.
 *  The src-path of the file is always relative to the location of the package.xml within a package.
 *  The dest-path of the file is always relative to the JBoss AS server home (JBOSS_HOME).
 *  A file can be of type library or config or script file.
 * 
 * @author Jaikiran Pai
 * 
 */
public interface InstallFile
{

   /**
    * @return Returns the name of the file 
    *     
    */
   String getName();

   /**
    * Sets the file name
    * 
    * @param name The name of the file
    *     
    */
   void setName(String name);

   /**
    * @return Returns the src-path of the file. The src-path
    * is relative to the location of the package.xml file in the package
    *     
    */
   String getSrcPath();

   /**
    * Sets the src-path of the file. The path is expected to be
    * relative to the location of package.xml file in the package
    * 
    * @param path Relative source path 
    *     
    */
   void setSrcPath(String path);

   /**
    * @return Returns the dest-path of the file. The dest-path
    * is relative to the location of the package.xml file in the package
    *     
    */
   String getDestPath();

   /**
    * Sets the dest-path of the file. The path is expected to be
    * relative to the location of package.xml file in the package
    *
    * @param path Relative destination path 
    *     
    */
   void setDestPath(String path);

   /**
    * @return Returns the type of this file
    *     
    */
   FileType getType();

   /**
    * Sets the type of the file
    * 
    * @param ftype File type
    *     
    */
   void setFileType(FileType ftype);
   
   /**
    * Sets the file type
    * 
    * @param type The type of file
    * @see #setFileType(FileType)
    */
   void setType(String type);
   
   /**
    * Returns the package through which this file was installed
    *  
    * @return
    */
   Package getPackage();

}
