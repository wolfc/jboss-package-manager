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
package org.jboss.ejb3.packagemanager.main;

import gnu.getopt.Getopt;

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl;

/**
 * Main
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class Main
{

   /**
    * One of the entry points to the package manager.
    * Accepts the command line arguments and carries out appropriate operations
    * through the package-manager.
    * 
    *  TODO: The command line arguments, haven't yet been finalized
    * 
    * @param args
    */
   public static void main(String[] args)
   {
      Getopt getOpt = new Getopt("packagemanager", args, "i:u:e:s:p:");
      int opt;
      String packageFilePath = null;
      String jbossHome = null;
      String packageNameToUninstall = null;
      String pmHome = System.getProperty("java.io.tmpdir");
      while ((opt = getOpt.getopt()) != -1)
      {
         switch (opt)
         {
            case 'i' :
               packageFilePath = getOpt.getOptarg();
               break;
            case 'u' :
               packageFilePath = getOpt.getOptarg();
               break;
            case 'e' :
               packageNameToUninstall = getOpt.getOptarg();
               break;
            case 's' :
               jbossHome = getOpt.getOptarg();
               break;
            case 'p' :
               pmHome = getOpt.getOptarg();
               break;
            default :
               throw new Error("Unhandled code " + opt);
         }
      }
      if (jbossHome == null || packageFilePath == null)
      {
         throw new Error("JBoss home or package file not specified");
      }
      PackageManagerEnvironment env = new PackageManagerEnvironment(pmHome);
      PackageManager pm = new DefaultPackageManagerImpl(env, jbossHome);

      // install the package
      try
      {
         pm.installPackage(packageFilePath);
      }
      catch (PackageManagerException ie)
      {
         throw new RuntimeException(ie);
      }

   }

}
