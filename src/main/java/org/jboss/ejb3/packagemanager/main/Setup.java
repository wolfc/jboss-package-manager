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

import org.jboss.ejb3.packagemanager.Constants;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.util.DBUtil;
import org.jboss.logging.Logger;

/**
 * Setup
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class Setup
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(Setup.class);
   
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
      CmdLineParser.Option schemaFileCmdOption = cmdLineParser.addStringOption('f', "file");
      
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
      String packageManagerHome = System.getProperty(Constants.PACKAGE_MANAGER_HOME_SYSTEM_PROPERTY, currentDir.getAbsolutePath());
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
      
      // Run the setup script
      String schemaSetupScript = (String) cmdLineParser.getOptionValue(schemaFileCmdOption);
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
         // We use Derby Embedded which is file based (so point to the DB home).
         // TODO: This should ideally be handled by the PackageDatabaseManager,
         // or some central place which is aware of the DB type. Let's just
         // do this here for now.
         File dbHome = env.getDataDir();
         // set the Derby system home property to point to the package manager db
         System.setProperty("derby.system.home", dbHome.getAbsolutePath());
         logger.info("Package manager DB home set to " + System.getProperty("derby.system.home"));

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
   }

   
   private static void printUsage()
   {
      System.out.println("Usage: setup [-f path_to_schema_file]");
   }

}
