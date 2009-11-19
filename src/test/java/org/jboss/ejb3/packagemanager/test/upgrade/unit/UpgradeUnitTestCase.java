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
package org.jboss.ejb3.packagemanager.test.upgrade.unit;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.PackageManagerFactory;
import org.jboss.ejb3.packagemanager.test.common.PackageManagerTestCase;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchiveFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * UpgradeUnitTestCase
 * 
 * Tests the upgrade functionality of the package manager
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class UpgradeUnitTestCase extends PackageManagerTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(UpgradeUnitTestCase.class);

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
      pkgMgrHome = setupPackageManagerHome(UpgradeUnitTestCase.class);
      jbossHome = setupDummyJBoss(UpgradeUnitTestCase.class);
      PackageManagerEnvironment env = new PackageManagerEnvironment(pkgMgrHome.getAbsolutePath());
      pkgMgr = PackageManagerFactory.getDefaultPackageManager(env, jbossHome.getAbsolutePath());
   }

   /**
    * Tests that a package installed at a lower version can be upgraded successfully to a 
    * higher version
    * @throws Exception
    */
   @Test
   public void testSimpleUpgrade() throws Exception
   {
      // first install a simple package, then call upgrade to a newer version
      File versionOne = this.createPackageVersionOne("upgrade-to-higher-version-test-package");

      this.pkgMgr.installPackage(versionOne.getAbsolutePath());
      // ensure install was successful
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deploy/dummy-version1.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/common-util.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/post-install-version1.txt");

      // now upgrade
      File versionTwo = this.createPackageVersionTwo("upgrade-to-higher-version-test-package");
      this.pkgMgr.updatePackage(versionTwo.getAbsolutePath());
      // check that the previous version's files are no longer there
      this.assertFileAbsenceUnderJBossHome(jbossHome, "bin/post-install-version1.txt");
      this.assertFileAbsenceUnderJBossHome(jbossHome, "server/default/deploy/dummy-version1.jar");
      // check that newer version files have been installed
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deploy/dummy-version2.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/common-util.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/post-install-version2.txt");

   }

   /**
    * Test that the package manager does not allow "upgrading" from an already installed
    * higher version of a package to a lower version of the same package
    * 
    * @throws Exception
    */
   @Test
   public void testUpgradeToLowerVersion() throws Exception
   {

      // first install a higher version of the package and then try to "upgrade"
      // to a lower version
      File versionTwo = this.createPackageVersionTwo("upgrade-to-lower-version-test-package");

      this.pkgMgr.installPackage(versionTwo.getAbsolutePath());
      // ensure install was successful
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deploy/dummy-version2.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/common-util.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/post-install-version2.txt");

      // now "upgrade" to lower version
      File versionOne = this.createPackageVersionOne("upgrade-to-lower-version-test-package");
      this.pkgMgr.updatePackage(versionOne.getAbsolutePath());

      // ensure that the package wasn't degraded to a lower a version
      // by checking that the previous files are *not* uninstalled and also the
      // lower version files are *not* installed
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deploy/dummy-version2.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/common-util.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/post-install-version2.txt");

      this.assertFileAbsenceUnderJBossHome(jbossHome, "server/default/deploy/dummy-version1.jar");
      this.assertFileAbsenceUnderJBossHome(jbossHome, "bin/post-install-version1.txt");

   }
   
   /**
    * Test that a package being upgraded to same version does not cause any issues.
    * Internally, such a package upgrade is ignored (skipped) by the package manager,
    * but that's not the concern of this test case
    * 
    * @throws Exception
    */
   @Test
   public void testUpgradeToSameVersion() throws Exception
   {
      File simplePackageWithDependency = this.createPackageWithSimplePackagedDependency("upgrade-to-same-version","1.0.0-beta-1");
      // install
      this.pkgMgr.installPackage(simplePackageWithDependency.getAbsolutePath());
      
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/lib/dummy.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");
      
      // now upgrade. Note that even though we are upgrading to a different
      // package contents, this upgrade will be skipped and the contents installed
      // previously by this package will stay.
      File simplePackage = this.createSimplePackage("upgrade-to-same-version", "1.0.0-beta-1");
      this.pkgMgr.updatePackage(simplePackage.getAbsolutePath());
      
      // ensure that the upgrade did not result in loss of installed files
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/lib/dummy.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");
      
      
   }

   /**
    * Creates a simple package with the following structure:
    * 
    * <package-name>
    *   |
    *   |--- dummy-version1.jar (will be installed to server/default/deploy) 
    *   |--- common-util.jar (will be installed to common/lib)
    *   |--- build-version1.xml
    *   |--- package.xml
    *   
    * @param packageFileName
    * @return
    * @throws IOException
    */
   private File createPackageVersionOne(String packageName) throws IOException
   {
      File dummyJar = this.createDummyJar();
      String packageFileName = packageName + JAR_SUFFIX;

      // Now let's package the dummy.jar, package.xml into a package
      File simplePackage = new File(getPerTestTargetDir(this.getClass()), packageFileName);
      JavaArchive pkg = JavaArchiveFactory.create(simplePackage.getName());
      pkg.addResource("dummy-version1.jar", dummyJar);
      pkg.addResource("common-util.jar", dummyJar);
      URL packageXmlURL = this.getResource(this.getClass(), "package-version1.xml");
      File file = new File(packageXmlURL.getFile());
      File processedPackageXmlFile = this.processPackageXml(file, packageName, "1.0.0-alpha-1");
      pkg.addResource("package.xml", processedPackageXmlFile);
      URL buildXmlURL = this.getResource(this.getClass(), "build-version1.xml");
      File buildFile = new File(buildXmlURL.getFile());
      pkg.addResource("build-version1.xml", buildFile);
      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, simplePackage);

      return simplePackage;
   }

   /**
    * Creates a package with the following structure:
    * <package-name>
    *   |
    *   |--- dummy-version2.jar (will be installed to server/default/deploy) 
    *   |--- common-util.jar (will be installed to common/lib)
    *   |--- build-version2.xml
    *   |--- package.xml
    * 
    * @param packageFileName
    * @return
    * @throws IOException
    */
   private File createPackageVersionTwo(String packageName) throws IOException
   {
      String packageFileName = packageName + JAR_SUFFIX;

      File dummyJar = this.createDummyJar();

      // Now let's package the dummy.jar, package.xml into a package
      File simplePackage = new File(getPerTestTargetDir(this.getClass()), packageFileName);
      JavaArchive pkg = JavaArchiveFactory.create(simplePackage.getName());
      pkg.addResource("dummy-version2.jar", dummyJar);
      pkg.addResource("common-util.jar", dummyJar);
      URL packageXmlURL = this.getResource(this.getClass(), "package-version2.xml");
      File file = new File(packageXmlURL.getFile());
      File processedPackageXmlFile = this.processPackageXml(file, packageName, "1.0.0-alpha-2");
      pkg.addResource("package.xml", processedPackageXmlFile);
      URL buildXmlURL = this.getResource(this.getClass(), "build-version2.xml");
      File buildFile = new File(buildXmlURL.getFile());
      pkg.addResource("build-version2.xml", buildFile);
      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, simplePackage);

      return simplePackage;
   }
}
