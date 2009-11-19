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
package org.jboss.ejb3.packagemanager.test.commandline.unit;

import java.io.File;

import org.jboss.ejb3.packagemanager.exception.PackageNotInstalledException;
import org.jboss.ejb3.packagemanager.main.Main;
import org.jboss.ejb3.packagemanager.test.common.PackageManagerTestCase;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CommandLineTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class CommandLineTestCase extends PackageManagerTestCase
{

   private static Logger logger = Logger.getLogger(CommandLineTestCase.class);

   /**
    * The JBoss Home used in each test
    */
   private static File jbossHome;

   /**
    * Package manager home used in each test
    */
   private static File pkgMgrHome;

   /**
    * Do the necessary setup
    * @throws Exception
    */
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      pkgMgrHome = setupPackageManagerHome(CommandLineTestCase.class);
      jbossHome = setupDummyJBoss(CommandLineTestCase.class);

   }

   /**
    * Test that the install operation from the command line works as expected. 
    * 
    *  TODO: Note that the params passed through the command line are still work-in-progress
    *  and they might change in future. This test case then needs to change appropriately. 
    * 
    * @throws Exception
    */
   @Test
   public void testInstall() throws Exception
   {
      File commandLineTestPackage = this.createSimplePackage("command-line-test-package");

      String commandLineArgs[] = new String[]
      {"-i", commandLineTestPackage.getAbsolutePath(), "-p", this.pkgMgrHome.getAbsolutePath(), "-s",
            this.jbossHome.getAbsolutePath()};

      // run the package manager
      Main.main(commandLineArgs);

      // now check that the file was installed in that location
      this.assertFileExistenceUnderJBossHome(this.jbossHome, "common/lib/dummy.jar");

   }

   /**
    * Test that the uninstall operation from the command line works as expected. 
    * 
    *  TODO: Note that the params passed through the command line are still work-in-progress
    *  and they might change in future. This test case then needs to change appropriately. 
    * 
    * @throws Exception
    */
   @Test
   public void testUnInstallOfNonExistentPackage() throws Exception
   {
      // test that non-existent package uninstallation is not allowed

      String commandLineArgs[] = new String[]
      {"-r", "blahblahblah", "-p", this.pkgMgrHome.getAbsolutePath(), "-s", this.jbossHome.getAbsolutePath()};

      // run the package manager
      try
      {
         Main.main(commandLineArgs);
         Assert
               .fail("Uninstallation of non-existent package did not throw any errors. Expected package manager to throw error");
      }
      catch (PackageNotInstalledException pnie)
      {
         // expected
      }

   }

   /**
    * Test that the uninstall operation from the command line works as expected. 
    * 
    *  TODO: Note that the params passed through the command line are still work-in-progress
    *  and they might change in future. This test case then needs to change appropriately. 
    * 
    * @throws Exception
    */
   @Test
   public void testUnInstall() throws Exception
   {
      // first install and then uninstall
      File packageWithScripts = this.createPackageWithPreInstallScript("commandline-uninstall-test-package");
      // test that non-existent package uninstallation is not allowed

      String commandLineArgs[] = new String[]
      {"-i", packageWithScripts.toURI().toURL().toExternalForm(), "-p", this.pkgMgrHome.getAbsolutePath(), "-s",
            this.jbossHome.getAbsolutePath()};

      // run the package manager
      Main.main(commandLineArgs);

      // do a simple test that the package was installed
      this.assertFileExistenceUnderJBossHome(this.jbossHome, "server/default/deploy/dummy.jar");

      // now uninstall
      commandLineArgs = new String[]
      {"-r", "commandline-uninstall-test-package", "-p", this.pkgMgrHome.getAbsolutePath(), "-s",
            this.jbossHome.getAbsolutePath()};

      // run the package manager
      Main.main(commandLineArgs);

      // now test that the files were uninstalled
      this.assertFileAbsenceUnderJBossHome(jbossHome, "server/default/deploy/dummy.jar");

      // Remember that the JBOSS_HOME/bin/test.txt file was created
      // by an script and was NOT included as an installation file (i.e. through
      // "file" element in the package.xml). Such files are NOT tracked/controlled
      // by the package manager and hence will not be touched on uninstallation
      // (unless ofcourse, the post/pre uninstall scripts take care of these files)
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/test.txt");

   }

   /**
    * Tests the "query" (-q option) feature of the package manager, which returns the names
    * of all installed packages
    * @throws Exception
    */
   @Test
   public void testQueryPackages() throws Exception
   {
      String commandLineArgs[] = new String[]
      {"-q", "-p", this.pkgMgrHome.getAbsolutePath(), "-s", this.jbossHome.getAbsolutePath()};

      // run the package manager
      Main.main(commandLineArgs);
   }
}
