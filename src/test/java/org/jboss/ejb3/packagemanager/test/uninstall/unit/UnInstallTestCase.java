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
package org.jboss.ejb3.packagemanager.test.uninstall.unit;

import java.io.File;

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.PackageManagerFactory;
import org.jboss.ejb3.packagemanager.exception.PackageNotInstalledException;
import org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl;
import org.jboss.ejb3.packagemanager.test.common.PackageManagerTestCase;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * UnInstallTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class UnInstallTestCase extends PackageManagerTestCase
{

   private static Logger logger = Logger.getLogger(UnInstallTestCase.class);

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
      pkgMgrHome = setupPackageManagerHome(UnInstallTestCase.class);
      jbossHome = setupDummyJBoss(UnInstallTestCase.class);
      PackageManagerEnvironment env = new PackageManagerEnvironment(pkgMgrHome.getAbsolutePath());
      pkgMgr = PackageManagerFactory.getDefaultPackageManager(env, jbossHome.getAbsolutePath());
   }

   
   
   @Test
   public void testUnInstallNonExistentPackge() throws Exception
   {
      try
      {
         this.pkgMgr.removePackage("test-package");
         Assert.fail("Uninstalling non-existent package did not raise error");
      }
      catch (PackageNotInstalledException pnie)
      {
         // expected
      }
   }

   @Test
   public void testSimpleUninstall() throws Exception
   {
      File simplePackage = this.createSimplePackage("simple-uninstall-test-package.jar");

      // first install
      this.pkgMgr.installPackage(simplePackage.getAbsolutePath());

      // do a simple check that the package was installed (no need
      // to do a lot of testing around installation, because that's covered by
      // other tests. This test is mainly for uninstall)
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");

      // now uninstall
      this.pkgMgr.removePackage("common-package-with-just-install-file");

      this.assertFileAbsenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");
   }

   @Test
   public void testUninstallationOfPackageContainingScripts() throws Exception
   {
      File packageWithScripts = this.createPackageWithPreInstallScript("package-with-script-uninstall-test.jar");
      // first install
      this.pkgMgr.installPackage(packageWithScripts.getAbsolutePath());

      // do a simple check that the package was installed (no need
      // to do a lot of testing around installation, because that's covered by
      // other tests. This test is mainly for uninstall)
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deploy/dummy.jar");

      // now uninstall
      this.pkgMgr.removePackage("common-package-with-pre-install");

      this.assertFileAbsenceUnderJBossHome(jbossHome, "server/default/deploy/dummy.jar");

      // Remember that the JBOSS_HOME/bin/test.txt file was created
      // by an script and was NOT included as an installation file (i.e. through
      // "file" element in the package.xml). Such files are NOT tracked/controlled
      // by the package manager and hence will not be touched on uninstallation
      // (unless ofcourse, the post/pre uninstall scripts take care of these files)
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/test.txt");
   }

   @Test
   public void testUnInstallationOfPackageWithDependencies() throws Exception
   {
      File packageWithDependencies = this
            .createPackageWithSimplePackagedDependency("uninstall-package-with-packaged-dependency.jar");
      // first install
      this.pkgMgr.installPackage(packageWithDependencies.getAbsolutePath());

      // do a simple check that the package was installed (no need
      // to do a lot of testing around installation, because that's covered by
      // other tests. This test is mainly for uninstall)
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/lib/dummy.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");

      // uninstall
      this.pkgMgr.removePackage("common-package-with-packaged-dependency");

      // the file installed by the main package (the package which was uninstalled)
      // should no longer be present
      this.assertFileAbsenceUnderJBossHome(jbossHome, "server/default/lib/dummy.jar");
      // The file installed by the dependency package SHOULD BE PRESENT,
      // because removing the dependent package should not remove dependency packages
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");

   }
   
   /**
    * Tests that the post-uninstall script runs during the uninstall process  
    * 
    * @throws Exception
    */
   @Test
   public void testPostUnInstallScriptExecution() throws Exception
   {
      File postUnInstallScriptPackage = this.createPackageWithPostUnInstallScript("post-uninstall-test-package.jar");
      
      // As a sanity check, ensure that the file supposed to be created by our post-uninstall 
      // step is not already present
      this.assertFileAbsenceUnderJBossHome(jbossHome, "bin/post-uninstall.txt");
      
      // first install
      pkgMgr.installPackage(postUnInstallScriptPackage.getAbsolutePath());
      
      // simple check to ensure installation was successful
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deploy/dummy.jar");
      
      // now uninstall
      pkgMgr.removePackage("common-package-with-post-uninstall");
      // make sure uninstall was successful
      this.assertFileAbsenceUnderJBossHome(jbossHome, "server/default/deploy/dummy.jar");
      // check that post-uninstall script was run
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/post-uninstall.txt");
      
   }

}
