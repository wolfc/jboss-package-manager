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

import jargs.gnu.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

import org.jboss.ejb3.packagemanager.Constants;
import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.PackageManagerFactory;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.util.DBUtil;
import org.jboss.logging.Logger;

/**
 * Main
 * 
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class Main
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(Main.class);
   
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
      CmdLineParser cmdLineParser = new CmdLineParser();
      CmdLineParser.Option installCmdOption = cmdLineParser.addStringOption('i', "install");
      CmdLineParser.Option upgradeCmdOption = cmdLineParser.addStringOption('u', "upgrade");
      CmdLineParser.Option removeCmdOption = cmdLineParser.addStringOption('r', "remove");
      CmdLineParser.Option queryCmdOption = cmdLineParser.addBooleanOption('q', "query");
      CmdLineParser.Option packageManagerHomeCmdOption = cmdLineParser.addStringOption('p', "pmhome");
      CmdLineParser.Option jbossHomeCmdOption = cmdLineParser.addStringOption('s', "jbossHome");

      try
      {
         cmdLineParser.parse(args);
      }
      catch (CmdLineParser.OptionException e)
      {
         System.err.println("Error parsing command " + e.getMessage());
         printUsage();
         throw new PackageManagerException(e.getMessage());
      }
      File currentDir = new File(".");
      // Get the package manager home from system property. If not set, then defaults to
      // current directory
      String packageManagerHomeEnvVariableValue = System.getProperty(Constants.PACKAGE_MANAGER_HOME_SYSTEM_PROPERTY, currentDir.getAbsolutePath());
      // Override the package manager home if it's explicitly passed through the -p option
      String packageManagerHome = (String) cmdLineParser.getOptionValue(packageManagerHomeCmdOption,packageManagerHomeEnvVariableValue);
      if (packageManagerHome == null)
      {
         throw new PackageManagerException("Package manager home has not been set");
      }
     
      File pmHome = new File(packageManagerHome);
      if (!pmHome.exists())
      {
         throw new PackageManagerException("Package manager home " + pmHome + " does not exist!");
      }
      logger.info("Using Package Manager Home: " + packageManagerHome);
      PackageManagerEnvironment env = new PackageManagerEnvironment(packageManagerHome);
      // Check for JBOSS_HOME
      String jbossHome = (String) cmdLineParser.getOptionValue(jbossHomeCmdOption);
      if (packageManagerHome == null)
      {
         throw new PackageManagerException("Package manager home has not been set");
      }
      if (jbossHome == null)
      {
         throw new PackageManagerException("JBoss Home has not been set");
      }
      File jbHome = new File(jbossHome);
      if (!jbHome.exists())
      {
         throw new PackageManagerException("JBoss home " + jbHome + " does not exist!");
      }
      logger.info("Using JBoss Home: " + jbossHome);
      
      // Create a package manager now
      PackageManager pm = PackageManagerFactory.getDefaultPackageManager(env, jbossHome);

      // Parse the options from the command line and do appropriate action(s) 
      Boolean query = (Boolean) cmdLineParser.getOptionValue(queryCmdOption, Boolean.FALSE);
      String packageToInstall = (String) cmdLineParser.getOptionValue(installCmdOption);
      String packageToUpgrade = (String) cmdLineParser.getOptionValue(upgradeCmdOption);
      String packageToRemove = (String) cmdLineParser.getOptionValue(removeCmdOption);
      
      if (query)
      {
         Set<String> installedPackages = pm.getAllInstalledPackages();
         if (installedPackages.isEmpty())
         {
            logger.info("There are no packages installed in the system");
         }
         else
         {
            logger.info("Following packages have been installed in the system: ");
            logger.info("----------------------------------------------------");
            for (String packageName : installedPackages)
            {
               logger.info(packageName);
            }
            logger.info("----------------------------------------------------");
         }
      }

      if (packageToInstall != null)
      {
         // it's time to install
         pm.installPackage(packageToInstall);

      }

      if (packageToUpgrade != null)
      {
         // upgrade!
         pm.updatePackage(packageToUpgrade);
      }

      if (packageToRemove != null)
      {
         // out you go!
         pm.removePackage(packageToRemove);
      }
      
   }

   
   private static void printUsage()
   {
      System.out
            .println("Usage: packagemanager [-i path_to_package] [-r package_name] [-u path_to_package] [-q] [-p path_to_package_manager_home]\n"
                  + " [-s path_to_jboss_home]");
   }

}
