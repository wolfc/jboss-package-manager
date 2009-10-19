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
package org.jboss.ejb3.packagemanager.installer;

import java.io.File;
import java.io.IOException;

import org.jboss.ejb3.packagemanager.exception.InstallerException;
import org.jboss.ejb3.packagemanager.metadata.InstallFile;
import org.jboss.ejb3.packagemanager.metadata.Package;
import org.jboss.ejb3.packagemanager.util.IOUtil;
import org.jboss.logging.Logger;

/**
 * DefaultInstaller
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DefaultInstaller extends AbstractInstaller
{

   private static Logger logger = Logger.getLogger(DefaultInstaller.class);

   /**
    * @see org.jboss.ejb3.packagemanager.installer.AbstractInstaller#doInstall(org.jboss.ejb3.packagemanager.metadata.InstallFile, java.io.File, java.io.File)
    */
   @Override
   protected void doInstall(InstallFile fileMetadata, File fileToInstall, File dest) throws InstallerException
   {
      // just copy from source to dest
      File destFile = new File(dest, fileMetadata.getName());
      Package pkgMeta = fileMetadata.getPackage();
      if (destFile.exists())
      {
         logger.info("File " + fileMetadata.getName() + " from package: " + pkgMeta.getName() + " version: "
               + pkgMeta.getVersion() + " already exists in " + dest + " - installer will overwrite it");
      }
      try
      {
         IOUtil.copy(fileToInstall, destFile);
      }
      catch (IOException e)
      {

         throw new InstallerException("Could not install file: " + fileMetadata.getName() + " from package: "
               + pkgMeta.getName() + " version: " + pkgMeta.getVersion() + " into " + dest.getAbsolutePath());
      }

   }

}
