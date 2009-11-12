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

import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.entity.PersistentFile;
import org.jboss.ejb3.packagemanager.entity.PersistentPackage;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.metadata.InstallFileType;
import org.jboss.logging.Logger;

/**
 * AbstractInstaller
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class AbstractInstaller implements Installer
{

   private static Logger logger = Logger.getLogger(AbstractInstaller.class);

   protected PackageManagerContext packageMgrContext;

   public AbstractInstaller(PackageManagerContext packageMgrCtx)
   {
      this.packageMgrContext = packageMgrCtx;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.installer.Installer#uninstall(org.jboss.ejb3.packagemanager.PackageManagerContext, org.jboss.ejb3.packagemanager.PackageContext)
    */
   @Override
   public void uninstall(PersistentPackage pkg, PersistentFile installedFile) throws PackageManagerException
   {
      String jbossHome = pkg.getPackageManager().getJbossHome();
      File relativePathToFile = new File(jbossHome, installedFile.getInstalledPath());
      File fileToUninstall = new File(relativePathToFile, installedFile.getFileName());
      
      if (!fileToUninstall.exists())
      {
         throw new PackageManagerException("Installed file missing: " + fileToUninstall.getAbsolutePath()
               + " - cannot uninstall!");
      }
      if (fileToUninstall.isDirectory())
      {
         throw new PackageManagerException("Installed file is a directory : " + fileToUninstall.getAbsolutePath()
               + " - cannot uninstall!");
      }
      fileToUninstall.delete();
      logger.info("Uninstalled file " + fileToUninstall + " from package  " + pkg.getPackageName());

   }


   /**
    * 
    */
   @Override
   public final void install(PackageContext pkgCtx, InstallFileType fileMeta) throws PackageManagerException
   {
      // do templating
      File pkgRoot = pkgCtx.getPackageRoot();
      File srcPathOfFileToInstall = pkgRoot;
      if (fileMeta.getSrcPath() != null)
      {
         srcPathOfFileToInstall = new File(pkgRoot, fileMeta.getSrcPath());
      }
      File fileToInstall = new File(srcPathOfFileToInstall, fileMeta.getName());

      if (!fileToInstall.exists())
      {
         throw new PackageManagerException(fileToInstall.getAbsolutePath() + " does not exist, package: " + pkgCtx
               + " being installed from " + pkgCtx.getPackageRoot()
               + " is probably corrupt!");
      }

      if (fileMeta.getDestPath() == null)
      {
         throw new PackageManagerException("File " + fileMeta.getName() + " in package: " + pkgCtx
               + " does not specify a destination");
      }
      String destServerHome = this.packageMgrContext.getJBossServerHome();
      File locationToInstall = new File(destServerHome, fileMeta.getDestPath());
      // TODO: Provide an option on <file> to allow for creating missing destination folders
      // Till then just throw an exception if dest-path is not actually available
      if (!locationToInstall.exists() || !locationToInstall.isDirectory())
      {
         throw new PackageManagerException("dest-path " + locationToInstall.getAbsolutePath() + " for file: "
               + fileMeta.getName() + " in package: " + pkgCtx
               + " is either not present or is not a directory");
      }
      try
      {
         doInstall(fileMeta, fileToInstall, locationToInstall);
         logger.info("Installed file " + fileMeta.getName() + " from package: " + pkgCtx + " to " + locationToInstall.getAbsolutePath());

         // TODO: Write to DB about file install completion
      }
      catch (Throwable t)
      {
         // TODO: Think about this - do we need to do something specific in DB when a package file
         // fails to install?
         throw new RuntimeException(t);
      }
   }

   protected abstract void doInstall(InstallFileType fileMetadata, File fileToInstall, File dest)
         throws PackageManagerException;

}
