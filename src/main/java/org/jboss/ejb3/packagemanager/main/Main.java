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

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.PackageManagerFactory;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl;
import org.jboss.ejb3.packagemanager.util.DBUtil;
import org.jboss.logging.Logger;

/**
 * Main
 * 
 * TODO: Command line parsing is WIP.
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
      CmdLineParser.Option setupCmdOption = cmdLineParser.addStringOption("setup");
      CmdLineParser.Option installCmdOption = cmdLineParser.addStringOption('i', "install");
      CmdLineParser.Option upgradeCmdOption = cmdLineParser.addStringOption('u', "upgrade");
      CmdLineParser.Option removeCmdOption = cmdLineParser.addStringOption('r', "remove");
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
      String packageManagerHome = (String) cmdLineParser.getOptionValue(packageManagerHomeCmdOption,currentDir.getAbsolutePath());
      String jbossHome = (String) cmdLineParser.getOptionValue(jbossHomeCmdOption);
      if (packageManagerHome == null)
      {
         throw new PackageManagerException("Package manager home has not been set");
      }
      if (jbossHome == null)
      {
         throw new PackageManagerException("JBoss Home has not been set");
      }

      File pmHome = new File(packageManagerHome);
      if (!pmHome.exists())
      {
         throw new PackageManagerException("Package manager home " + pmHome + " does not exist!");
      }

      File jbHome = new File(jbossHome);
      if (!jbHome.exists())
      {
         throw new PackageManagerException("JBoss home " + jbHome + " does not exist!");
      }
      logger.info("Using Package Manager Home: " + packageManagerHome);
      logger.info("Using JBoss Home: " + jbossHome);
      PackageManagerEnvironment env = new PackageManagerEnvironment(packageManagerHome);
      PackageManager pm = PackageManagerFactory.getDefaultPackageManager(env, jbossHome);

      String schemaSetupScript = (String) cmdLineParser.getOptionValue(setupCmdOption);
      if (schemaSetupScript != null)
      {
         File schemaFile = new File(schemaSetupScript);
         if (!schemaFile.exists())
         {
            throw new PackageManagerException(
                  "Could not setup the database for package manager, because of non-existent schema file "
                        + schemaSetupScript);
         }
         Connection conn = null;
         try
         {
            conn = DriverManager.getConnection("jdbc:derby:pmdb;create=true");
            DBUtil.runSql(conn, schemaFile);
            logger.info("Successfully setup the package manager database");
         }
         catch (SQLException sqle)
         {
            throw new PackageManagerException("Could not setup package manager database: ", sqle);
         }
         catch (IOException ioe)
         {
            throw new PackageManagerException("Could not setup package manager database: ", ioe);
         }
         finally
         {
            if (conn != null)
            {
               try
               {
                  conn.close();
               }
               catch (SQLException sqle)
               {
                  // can't do much
                  logger.trace("Could not close connection:",sqle);
               }
            }
         }

      }

      String packageToInstall = (String) cmdLineParser.getOptionValue(installCmdOption);
      String packageToUpgrade = (String) cmdLineParser.getOptionValue(upgradeCmdOption);
      String packageToRemove = (String) cmdLineParser.getOptionValue(removeCmdOption);

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
            .println("Usage: packagemanager [-i path_to_package] [-r package_name] [-u path_to_package] [-p path_to_package_manager_home]\n"
                  + " [-s path_to_jboss_home]");
   }

}
