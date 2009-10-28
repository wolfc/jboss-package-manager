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

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl;
import org.jboss.ejb3.packagemanager.main.Main;
import org.jboss.ejb3.packagemanager.test.common.PackageManagerTestCase;
import org.jboss.logging.Logger;
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
    * Package manager
    */
   private static PackageManager pkgMgr;

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
      PackageManagerEnvironment env = new PackageManagerEnvironment(pkgMgrHome.getAbsolutePath());
      pkgMgr = new DefaultPackageManagerImpl(env, jbossHome.getAbsolutePath());
   }

   /**
    * Test that the command line variant of the package manager works as expected. 
    * 
    *  TODO: Note that the params passed through the command line are still work-in-progress
    *  and they might change in future. This test case then needs to change appropriately. 
    * 
    * @throws Exception
    */
   @Test
   public void testMainMethodOfDefaultPackageManager() throws Exception
   {
      File commandLineTestPackage = this.createSimplePackage("command-line-test-package.jar");

      String commandLineArgs[] = new String[]
      {"-i", commandLineTestPackage.getAbsolutePath(), "-p", this.pkgMgrHome.getAbsolutePath(), "-s",
            this.jbossHome.getAbsolutePath()};

      // run the package manager
      Main.main(commandLineArgs);

      // now check that the file was installed in that location
      this.assertFileExistenceUnderJBossHome(this.jbossHome, "common/lib/dummy.jar");

   }
}
