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

package org.jboss.ejb3.packagemanager.test.install.unit;

import java.io.File;

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl;
import org.jboss.ejb3.packagemanager.main.Main;
import org.jboss.ejb3.packagemanager.test.common.PackageManagerTestCase;
import org.jboss.ejb3.packagemanager.test.uninstall.unit.UnInstallTestCase;
import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the basic install feature of the package manager
 * 
 * Author: Jaikiran Pai
 */
public class BasicInstallTestCase extends PackageManagerTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(BasicInstallTestCase.class);

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
      pkgMgrHome = setupPackageManagerHome(BasicInstallTestCase.class);
      jbossHome = setupDummyJBoss(BasicInstallTestCase.class);
      PackageManagerEnvironment env = new PackageManagerEnvironment(pkgMgrHome.getAbsolutePath());
      pkgMgr = new DefaultPackageManagerImpl(env, jbossHome.getAbsolutePath());
   }

   /**
    * Tests that a simple install feature works. The package to be installed
    * by the package manager contains a dummy jar file to be installed at
    * JBOSS_HOME/common/lib and a build script for the pre-install phase.
    * 
    * The package does *not* have any dependencies, system-requirements, pre-install or
    * post-install steps
    * 
    * @throws Exception
    */
   @Test
   public void testSimpleInstall() throws Exception
   {
      File simplePackage = this.createSimplePackage("simple-package.jar");

      // run the package manager
      pkgMgr.installPackage(simplePackage.toURI().toURL());

      // now check that the file was installed in that location
      this.assertFileExistenceUnderJBossHome(this.jbossHome, "common/lib/dummy.jar");
   }

   /**
    * Tests that a pre-install script packaged in a package runs as expected. 
    * The pre-install script is written such that it places a test.txt file
    * in JBOSS_HOME/bin to prove that the script was run.
    * 
    * @throws Exception
    */
   @Test
   public void testPreInstallScriptExecution() throws Exception
   {
      File packageWithPreInstallScript = this.createPackageWithPreInstallScript("package-with-pre-install-script.jar");

      // now run the package manager
      this.pkgMgr.installPackage(packageWithPreInstallScript.getAbsolutePath());
      // now assert that the JBOSS_HOME/bin/test.txt was created
      // If that file was created then it's enough to guarantee that the pre-install script was run
      this.assertFileExistenceUnderJBossHome(this.jbossHome, "bin/test.txt");

      // As a further test, also check that the dummy.jar packaged in this package was
      // also installed (at JBOSS_HOME/server/default/deploy folder)
      this.assertFileExistenceUnderJBossHome(this.jbossHome, "server/default/deploy/dummy.jar");

   }

   /**
    * Note: This test assumes the presence of a "package" at 
    * http://snapshots.jboss.org/maven2/org/jboss/ejb3/tmp/simple-package.jar
    * 
    * TODO: Once we have packages uploaded to Maven repo, we can point to a different
    * URL from this tmp one.
    * This test currently, is really just to show that the package manager can install
    * packages from a HTTP URL.
    *  
    * This test does *not* upload this package (not all users running this test will have
    * the upload privileges). So if the "package" is unavailable at that location then this
    * test will fail
    * 
    * @throws Exception
    */
   @Test
   public void testHttpPackageInstall() throws Exception
   {
      // TODO: See javadocs of this test. This URL will change in near future
      String httpPackageURL = "http://snapshots.jboss.org/maven2/org/jboss/ejb3/tmp/simple-package.jar";
      // install the package
      this.pkgMgr.installPackage(httpPackageURL);

      // now check that the file was installed in that location
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");
   }

}
