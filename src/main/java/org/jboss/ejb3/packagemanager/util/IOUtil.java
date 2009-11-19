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
package org.jboss.ejb3.packagemanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * IOUtil
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class IOUtil
{
   /**
    * Copies a file from one location to other
    * 
    * @param src The source file (should not be a directory)
    * @param dest The destination file (should not be a directory)
    * @throws IOException
    */
   public static void copy(File src, File dest) throws IOException
   {
      FileInputStream fis = new FileInputStream(src);
      FileOutputStream fos = new FileOutputStream(dest);
      byte[] buffer = new byte[1024 * 4];
      int n = 0;
      while (-1 != (n = fis.read(buffer)))
      {
         fos.write(buffer, 0, n);

      }
      
   }

   public static void extractJarFile(File destDir, JarFile jar) throws IOException
   {
      if (!destDir.exists())
      {
         destDir.mkdirs();
      }
      Enumeration<JarEntry> jarEntries = jar.entries();
      while (jarEntries.hasMoreElements())
      {
         JarEntry entry = jarEntries.nextElement();
         File destFile = new File(destDir, entry.getName());
         if (entry.isDirectory())
         { // if its a directory, create it
            destFile.mkdirs();
            continue;
         }
         else
         {
            // if this is a file and not a directory, make
            // sure that it's parent directories have been
            // created. Remember, while iterating the entries
            // in a jar file, it's not guaranteed that the directories will be 
            // processed first and then the file. So we need to check if the file's
            // parent directories exist
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists())
            {
               parentDir.mkdirs();
            }
         }

         java.io.InputStream is = null;
         java.io.FileOutputStream fos = null;

         try
         {
            is = jar.getInputStream(entry); // get the input stream
            fos = new java.io.FileOutputStream(destFile);
            while (is.available() > 0)
            { // write contents of 'is' to 'fos'
               fos.write(is.read());
            }
         }
         finally
         {
            if (fos != null)
            {
               fos.close();
            }
            if (is != null)
            {
               is.close();
            }
         }

      }

   }
}
