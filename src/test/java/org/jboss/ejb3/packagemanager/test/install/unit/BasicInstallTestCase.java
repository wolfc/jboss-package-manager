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
import java.net.URL;

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl;
import org.jboss.ejb3.packagemanager.test.common.Dummy;
import org.jboss.ejb3.packagemanager.test.common.PackageManagerTestCase;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchiveFactory;
import org.junit.Assert;
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
    * The JBoss Home used in this test
    */
   private static File jbossHome;

   /**
    * Package manager home used in this test
    */
   private static File pkgMgrHome;

   /**
    * Do the necessary setup
    * @throws Exception
    */
   @BeforeClass
   public static void setup() throws Exception
   {
      pkgMgrHome = setupPackageManagerHome();
      jbossHome = setupDummyJBoss();
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
      File dummyJar = this.createDummyJar();

      // Now let's package the dummy.jar, package.xml into a package
      File simplePackage = new File(targetDir, "simple-package.jar");
      JavaArchive pkg = JavaArchiveFactory.create(simplePackage.getName());
      pkg.addResource("dummy.jar", dummyJar);
      URL packageXmlURL = this.getResource(this.getClass(), "package-with-just-install-file.xml");
      File file = new File(packageXmlURL.getFile());
      pkg.addResource("package.xml", file);
      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, simplePackage);

      // We are done with the package creation, now let's install it
      // using the package manager
      // But let's first make sure that the jar file to be installed 
      // through this package is not already in the JBoss AS (this check
      // is just for the sanity of this test case). If it exists, just
      // clean it up before running the package manager
      File dummyJarInJBoss = new File(jbossHome, "common/lib/dummy.jar");
      if (dummyJarInJBoss.exists())
      {
         logger
               .debug("JBOSS_HOME/common/lib/dummy.jar already exists - cleaning it up before running the package manager test");
         dummyJarInJBoss.delete();
      }
      // run the package manager
      pkgMgr.installPackage(simplePackage.toURL());

      // now check that the file was installed in that location
      Assert.assertTrue("Package manager did NOT install dummy.jar into JBOSS_HOME/common/lib", dummyJarInJBoss
            .exists());
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
      File dummyJar = this.createDummyJar();

      // Now let's package the dummy.jar, package.xml and build.xml into a package
      File packageWithPreInstallScript = new File(targetDir, "package-with-preinstall-script.jar");
      JavaArchive pkg = JavaArchiveFactory.create(packageWithPreInstallScript.getName());
      pkg.addResource("dummy.jar", dummyJar);
      URL packageXmlURL = this.getResource(this.getClass(), "package-with-pre-install-script.xml");
      File packageXmlFile = new File(packageXmlURL.getFile());
      pkg.addResource("package.xml", packageXmlFile);
      URL buildXmlURL = this.getResource(this.getClass(), "build.xml");
      File buildFile = new File(buildXmlURL.getFile());
      pkg.addResource("build.xml", buildFile);

      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, packageWithPreInstallScript);

      // The pre-install build script is written such that it creates a test.txt file in the JBOSS_HOME/bin
      // folder. So before running the package manager, just cleanup the JBOSS_HOME/bin folder of any such existing
      // files
      File testFileInJBossBin = new File(this.jbossHome, "bin/test.txt");
      if (testFileInJBossBin.exists())
      {
         logger
               .debug("JBOSS_HOME/bin/test.txt already exists - cleaning it up before running the package manager test");
         testFileInJBossBin.delete();
      }
      // now run the package manager
      this.pkgMgr.installPackage(packageWithPreInstallScript.getAbsolutePath());
      // now assert that the JBOSS_HOME/bin/test.txt was created
      // If that file was created then it's enough to guarantee that the pre-install script was run
      Assert.assertTrue(
            testFileInJBossBin.getAbsolutePath() + " was NOT created. The pre-install script in the package "
                  + packageWithPreInstallScript + " did not run", testFileInJBossBin.exists());

      // As a further test, also check that the dummy.jar packaged in this package was
      // also installed (at JBOSS_HOME/server/default/deploy folder)
      File dummyJarInServerDefaultDeploy = new File(this.jbossHome, "server/default/deploy/dummy.jar");
      Assert.assertTrue(dummyJarInServerDefaultDeploy + " was not installed by the package manager",
            dummyJarInServerDefaultDeploy.exists());
   }

   /**
    * The {@link DefaultPackageManagerImpl}'s {@link DefaultPackageManagerImpl#main(String[])}
    * method acts as a valid entry point for package manager clients. Test that it parses the
    * command line arguments corrects and functionalities of the package manager work as expected
    * 
    *  TODO: Note that the params passed through the command line are still work-in-progress
    *  and they might change in future. This test case then needs to change appropriately. 
    * 
    * @throws Exception
    */
   @Test
   public void testMainMethodOfDefaultPackageManager() throws Exception
   {
      File dummyJar = this.createDummyJar();
      // Now let's package the dummy.jar, package.xml into a package
      File commandLineTestPackage = new File(targetDir, "commandline-test-package.jar");
      JavaArchive pkg = JavaArchiveFactory.create(commandLineTestPackage.getName());
      pkg.addResource("dummy.jar", dummyJar);
      URL packageXmlURL = this.getResource(this.getClass(), "package-with-just-install-file.xml");
      File packageXmlFile = new File(packageXmlURL.getFile());
      pkg.addResource("package.xml", packageXmlFile);
      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, commandLineTestPackage);

      String commandLineArgs[] = new String[]
      {"-i", commandLineTestPackage.getAbsolutePath(), "-p", this.pkgMgrHome.getAbsolutePath(), "-s",
            this.jbossHome.getAbsolutePath()};

      // cleanup any appropriate existing files in JBoss server
      File dummyJarInJBoss = new File(jbossHome, "common/lib/dummy.jar");
      if (dummyJarInJBoss.exists())
      {
         logger
               .debug("JBOSS_HOME/common/lib/dummy.jar already exists - cleaning it up before running the package manager test");
         dummyJarInJBoss.delete();
      }

      // run the package manager
      DefaultPackageManagerImpl.main(commandLineArgs);

      // now check that the file was installed in that location
      Assert.assertTrue("Package manager did NOT install dummy.jar into JBOSS_HOME/common/lib", dummyJarInJBoss
            .exists());

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
      // cleanup any appropriate existing files in JBoss server
      File dummyJarInJBoss = new File(jbossHome, "common/lib/dummy.jar");
      if (dummyJarInJBoss.exists())
      {
         logger
               .debug("JBOSS_HOME/common/lib/dummy.jar already exists - cleaning it up before running the package manager test");
         dummyJarInJBoss.delete();
      }
      // TODO: See javadocs of this test. This URL will change in near future
      String httpPackageURL = "http://snapshots.jboss.org/maven2/org/jboss/ejb3/tmp/simple-package.jar";
      // install the package
      this.pkgMgr.installPackage(httpPackageURL);

      // now check that the file was installed in that location
      Assert.assertTrue("Package manager did NOT install dummy.jar into JBOSS_HOME/common/lib", dummyJarInJBoss
            .exists());
   }

   /**
    * Creates a dummy jar file containing a dummy .class file
    * The dummy.jar file is created in the project's "target" directory. If target/dummy.jar
    * already exists, then the existing File is returned.
    * 
    * @return Returns target/dummy.jar File
    * @throws Exception
    */
   private File createDummyJar() throws Exception
   {
      // Create a jar using ShrinkWrap API.
      // The jar will look like:
      // dummy.jar
      //    |
      //    |--- org/jboss/ejb3/packagemanager/test/common/Dummy.class
      //           |--- jboss
      //                    |--- ejb3
      //                            |--- packagemanager
      //                                    |--- test
      //                                            |--- common
      //                                                    |--- Dummy.class

      File dummyJar = new File(targetDir, "dummy.jar");
      // if it already exists then no need to recreate it
      if (dummyJar.exists())
      {
         return dummyJar;
      }
      JavaArchive jar = JavaArchiveFactory.create(dummyJar.getName());
      jar.addClass(Dummy.class);
      // write out to file system at target/dummy.jar
      logger.debug("Writing out the created jar " + jar.toString(true));
      this.exportZip(jar, dummyJar);
      return dummyJar;

   }
}
