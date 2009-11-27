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
package org.jboss.ejb3.packagemanager.test.dependency.unit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

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
 * DependencyInstallTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DependencyInstallTestCase extends PackageManagerTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(DependencyInstallTestCase.class);

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

   private static File perTestTargetDir = getPerTestTargetDir(DependencyInstallTestCase.class);

   /**
    * Do the necessary setup
    * @throws Exception
    */
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      pkgMgrHome = setupPackageManagerHome(DependencyInstallTestCase.class);
      jbossHome = setupDummyJBoss(DependencyInstallTestCase.class);
      PackageManagerEnvironment env = new PackageManagerEnvironment(pkgMgrHome.getAbsolutePath());
      pkgMgr = PackageManagerFactory.getDefaultPackageManager(env, jbossHome.getAbsolutePath());
   }

   /**
    * Tests that a package containing packaged-dependency is installed correctly.
    * 
    * @throws Exception
    */
   @Test
   public void testPackageWithPackagedDependency() throws Exception
   {
      File packageWithPackagedDependency = this
            .createPackageWithSimplePackagedDependency("package-with-packaged-dependency");

      // now install
      this.pkgMgr.installPackage(packageWithPackagedDependency.getAbsolutePath());

      // and now check that the files have been installed
      // this package was expected to install dummy.jar to JBOSS_HOME/common/lib and JBOSS_HOME/server/default/lib folders
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/lib/dummy.jar");

   }

   /**
    * Tests that a package containing multiple packaged dependencies is installed correctly
    *  
    * @throws Exception
    */
   @Test
   public void testPackageWithMultiplePackagedDependencies() throws Exception
   {
      File packageToInstall = this
            .createPackageWithMultiplePackagedDependencies("package-with-multiple-packaged-dependencies");

      this.pkgMgr.installPackage(packageToInstall.toURI().toURL());

      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deployers/dummy-main.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deployers/some-deployer-jboss-beans.xml");
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deployers/dummy1.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deployers/dummy2.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deployers/dummy3.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/where-is-kilroy.txt");
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/who-was-here.txt");
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/kilroy-was-here.txt");
      this.assertFileExistenceUnderJBossHome(jbossHome, "bin/kilroy-was-here-again.txt");

   }

   /**
    * 
    * @throws Exception
    */
   @Test
   public void testPackageWithUnProcessedDependencies() throws Exception
   {
      File dummyJar = this.createDummyJar();
      // create the package
      File packageWithUnprocessedDeps = new File(perTestTargetDir, "package-with-unprocessed-dependencies.jar");
      JavaArchive archive = JavaArchiveFactory.create(packageWithUnprocessedDeps.getName());
      archive.addResource("dummy.jar", dummyJar);
      List<URL> resources = this.getResources(this.getClass(), "some-deployer-jboss-beans.xml", "ivy.xml");
      for (URL resource : resources)
      {
         File resourceFile = new File(resource.getFile());
         archive.addResource(resourceFile.getName(), resourceFile);
      }

      URL packageXmlURL = this.getResource(this.getClass(), "package-with-unprocessed-dependencies.xml");
      File file = new File(packageXmlURL.getFile());
      file = this.processPackageXml(file, "package-with-unprocessed-dependencies", DEFAULT_PACKAGE_VERSION);
      archive.addResource("package.xml", file);

      // now write out the package to disk
      logger.debug("Writing out the package " + archive.toString(true));
      this.exportZip(archive, packageWithUnprocessedDeps);

      // install
      this.pkgMgr.installPackage(packageWithUnprocessedDeps.getAbsolutePath());

      // test the files have been installed
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deployers/dummy.jar");
      this.assertFileExistenceUnderJBossHome(jbossHome, "server/default/deployers/some-deployer-jboss-beans.xml");

   }

   /**
    * 
    * @param packageFileName
    * @return
    * @throws IOException
    */
   protected File createPackageWithMultiplePackagedDependencies(String packageName) throws IOException
   {

      String packageFileName = packageName + JAR_SUFFIX;
      
      File dummyJar = this.createDummyJar();
      // create dependee package#1
      File dependeePackageOne = new File(perTestTargetDir, "dependee-package-one.jar");
      JavaArchive archiveOne = JavaArchiveFactory.create(dependeePackageOne.getName());
      archiveOne.addResource("dummy1.jar", dummyJar);
      URL packageXmlURL = this.getResource(this.getClass(), "dependee-package1.xml");
      File file = new File(packageXmlURL.getFile());
      file = this.processPackageXml(file, "dependee-package1" , DEFAULT_PACKAGE_VERSION);
      archiveOne.addResource("package.xml", file);
      // pre-install script
      URL preInstallScriptXml = this.getResource(this.getClass(), "pre-install-build.xml");
      File preInstallScriptFile = new File(preInstallScriptXml.getFile());
      archiveOne.addResource("scripts/pre-install-build.xml", preInstallScriptFile);

      // now write out the package to disk
      logger.debug("Writing out the first dependee package " + archiveOne.toString(true));
      this.exportZip(archiveOne, dependeePackageOne);

      // create dependee package#2
      File dependeePackageTwo = new File(perTestTargetDir, "dependee-package-two.jar");
      JavaArchive archiveTwo = JavaArchiveFactory.create(dependeePackageTwo.getName());
      archiveTwo.addResource("dummy2.jar", dummyJar);
      URL packageXmlTwo = this.getResource(this.getClass(), "dependee-package2.xml");
      File packageXmlTwoFile = new File(packageXmlTwo.getFile());
      packageXmlTwoFile = this.processPackageXml(packageXmlTwoFile, "dependee-package2" , DEFAULT_PACKAGE_VERSION);
      archiveTwo.addResource("package.xml", packageXmlTwoFile);
      // post-install script
      URL postInstallXml = this.getResource(this.getClass(), "post-install-build.xml");
      File postInstallScriptFile = new File(postInstallXml.getFile());
      archiveTwo.addResource("scripts/post-install-build.xml", postInstallScriptFile);

      // now write out the package to disk
      logger.debug("Writing out the second dependee package " + archiveTwo.toString(true));
      this.exportZip(archiveTwo, dependeePackageTwo);

      // create dependee package#3
      File dependeePackageThree = new File(this.perTestTargetDir, "dependee-package-three.jar");
      JavaArchive archiveThree = JavaArchiveFactory.create(dependeePackageThree.getName());
      archiveThree.addResource("dummy3.jar", dummyJar);
      URL packageXmlThree = this.getResource(this.getClass(), "dependee-package3.xml");
      File packageXmlThreeFile = new File(packageXmlThree.getFile());
      packageXmlThreeFile = this.processPackageXml(packageXmlThreeFile, "dependee-package3" , DEFAULT_PACKAGE_VERSION);
      archiveThree.addResource("package.xml", packageXmlThreeFile);
      // pre-install and post-install script
      URL scriptXml = this.getResource(this.getClass(), "build.xml");
      File scriptFile = new File(scriptXml.getFile());
      archiveThree.addResource("build.xml", scriptFile);

      // now write out the package to disk
      logger.debug("Writing out the third dependee package " + archiveThree.toString(true));
      this.exportZip(archiveThree, dependeePackageThree);

      // Create the final package with all these 3 packages as the package-dependencies
      File aggregatedPacakge = new File(perTestTargetDir, packageFileName);
      JavaArchive aggregatedArchive = JavaArchiveFactory.create(aggregatedPacakge.getName());
      aggregatedArchive.addResource("dummy-main.jar", dummyJar);
      URL aggPackageXml = this.getResource(this.getClass(), "package-with-multiple-packaged-dependencies.xml");
      File aggPackageXmlFile = new File(aggPackageXml.getFile());
      aggPackageXmlFile = this.processPackageXml(aggPackageXmlFile, packageName , DEFAULT_PACKAGE_VERSION);
      aggregatedArchive.addResource("package.xml", aggPackageXmlFile);
      URL someDeployerConfigXml = this.getResource(this.getClass(), "some-deployer-jboss-beans.xml");
      File deployerConfigFile = new File(someDeployerConfigXml.getFile());
      aggregatedArchive.addResource("some-config-folder/some-deployer-jboss-beans.xml", deployerConfigFile);

      // add all the created packages at appropriate locations
      aggregatedArchive.addResource("dependee-package1.jar", dependeePackageOne);
      aggregatedArchive.addResource("subfolder/dependee-package2.jar", dependeePackageTwo);
      aggregatedArchive.addResource("subfolder/deep-nested-folder/dependee-package3.jar", dependeePackageThree);

      // now write out the package to disk
      logger.debug("Writing out the (main aggregated) package containing the packaged dependencies"
            + aggregatedArchive.toString(true));
      this.exportZip(aggregatedArchive, aggregatedPacakge);

      return aggregatedPacakge;

   }
}
