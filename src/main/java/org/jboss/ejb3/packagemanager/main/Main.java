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
 * TODO: This needs a lot of rework - currently WIP.
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
    * @throws PackageManagerException 
    */
   public static void main(String[] args) throws PackageManagerException
   {
      
      Getopt arguments = new Getopt("packagemanager", args, "i:u:r:s:p:");
      int argument;
      String jbossHome = null;
      String pmHome = System.getProperty("java.io.tmpdir");
      // TODO: Better handling of commands/options
      while ((argument = arguments.getopt()) != -1)
      {
         switch (argument)
         {
            case 's' :
               jbossHome = arguments.getOptarg();
               break;
            case 'p' :
               pmHome = arguments.getOptarg();
               break;
         }
      }
      
      if (jbossHome == null)
      {
         throw new Error("JBoss Server Home not specified");
      }
      PackageManagerEnvironment env = new PackageManagerEnvironment(pmHome);
      PackageManager pm = new DefaultPackageManagerImpl(env, jbossHome);
      
      Getopt commands = new Getopt("packagemanager", args, "i:u:r:s:p:");
      int command;
      String packageToOperateOn = null;
      // TODO: Better handling of commands/options
      while ((command = commands.getopt()) != -1)
      {
         switch (command)
         {
            case 'i' :
               packageToOperateOn = commands.getOptarg();
               pm.installPackage(packageToOperateOn);
               break;
            case 'u' :
               packageToOperateOn = commands.getOptarg();
               pm.updatePackage(packageToOperateOn);
               break;
            case 'r' :
               packageToOperateOn = commands.getOptarg();
               pm.removePackage(packageToOperateOn);
               break;
         }
      }
      

   }
   
   

}
