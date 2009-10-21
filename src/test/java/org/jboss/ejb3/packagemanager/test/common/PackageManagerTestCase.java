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
package org.jboss.ejb3.packagemanager.test.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.export.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchiveFactory;
import org.junit.Assert;

/**
 * PackageManagerTestCase
 *
 * Common utility methods for testcases involving package-manager
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class PackageManagerTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(PackageManagerTestCase.class);

   /**
    * Base dir
    */
   protected static File baseDir = new File(System.getProperty("basedir"));

   /**
    * The project's target dir
    */
   protected static File targetDir = new File(baseDir, "target");

   /**
    * Sets up an dummy JBoss instance and returns the File corresponding
    * to this JBoss instance. This "JBoss instance" will just be a directory
    * structure similar to the JBoss AS-5 directory structure and can be 
    * used for testing the package manager functionalities without having
    * to download/setup a real JBoss instance
    *
    * @return Returns the File corresponding to the (dummy) JBOSS_HOME
    * @throws IOException If there are any IO exceptions during creation of the 
    *   JBoss AS directory structure
    */
   protected static File setupDummyJBoss() throws IOException
   {
      File dummyJBossHome = new File(targetDir, "jboss-as");
      dummyJBossHome.mkdirs();
      createJBossDirectoryStructure(dummyJBossHome);
      return dummyJBossHome;

   }

   /**
    * Sets up a "home" for the package manager within the project's 
    * "target" folder. Returns the File corresponding to this
    * home folder of the package manager. This can be used 
    * within package manager tests
    * 
    * @return Returns the File corresponding to the package manager home
    * @throws IOException
    */
   protected static File setupPackageManagerHome() throws IOException
   {
      File pkgMgrHome = new File(targetDir, "pm-home");
      pkgMgrHome.mkdirs();
      return pkgMgrHome;
   }

   /**
    * Returns the URLs to the {@code resources}. The resources are looked 
    * under {@code klass}'s package name. For example, if the 
    * klass is org.jboss.ejb3.packagemanager.test.install.unit.BasicInstallTestCase and the
    * resource name is package.xml, then this method looks for package.xml under
    * org/jboss/ejb3/packagemanager/test/install/unit in the classpath. 
    * 
    *  Note: The list returned by the method could contain null elements if the
    *  corresponding resource wasn't found in the classpath.
    *   
    * @param klass The Class under whose package the resources are searched for
    * @param resources The name of the resources
    * @return Returns a list of resource URLs
    */
   protected List<URL> getResources(Class<?> klass, String... resources)
   {
      List<URL> urls = new ArrayList<URL>();
      for (String resource : resources)
      {
         urls.add(this.getResource(klass, resource));
      }
      return urls;
   }

   /**
    * Returns the URL to the {@code resourceName}. The resource is looked 
    * under {@code klass}'s package name. For example, if the 
    * klass is org.jboss.ejb3.packagemanager.test.install.unit.BasicInstallTestCase and the
    * resource name is package.xml, then this method looks for package.xml under
    * org/jboss/ejb3/packagemanager/test/install/unit in the classpath. 
    * 
    *  This method returns null if the resource was not found in the classpath
    * @param klass The Class under whose package the resources are searched for
    * @param resourceName The name of the resource
    * @return Returns the URL to the resource or NULL if not found in classpath
    */
   protected URL getResource(Class<?> klass, String resourceName)
   {
      String resourceDir = klass.getPackage().getName().toString() + ".";
      resourceDir = resourceDir.replace('.', '/');
      String resourcePath = resourceDir + resourceName;
      return klass.getClassLoader().getResource(resourcePath);
   }

   /**
    * Writes out an {@link JavaArchive} to the {@code destFile}
    * The directories under which this {@code destFile} is to be 
    * created are expected to be present. This method does *not* create
    * any non-existent directories
    * 
    * @param jar The jar file which is to be written to the file system
    * @param destFile The file to which the jar is to be written
    * @throws IOException
    */
   protected void exportZip(JavaArchive jar, File destFile) throws IOException
   {
      InputStream is = ZipExporter.exportZip(jar);
      FileOutputStream fos = new FileOutputStream(destFile);
      BufferedOutputStream bos = null;
      BufferedInputStream bis = null;
      try
      {
         bos = new BufferedOutputStream(fos);
         bis = new BufferedInputStream(is);
         byte[] content = new byte[4096];
         int length;
         while ((length = bis.read(content)) != -1)
         {
            bos.write(content, 0, length);
         }
         bos.flush();
      }
      finally
      {
         if (bos != null)
         {
            bos.close();
         }
         if (bis != null)
         {
            bis.close();
         }
      }
   }

   /**
    * Creates a dummy jar file containing a dummy .class file
    * The dummy.jar file is created in the project's "target" directory. If target/dummy.jar
    * already exists, then the existing File is returned.
    * 
    * @return Returns target/dummy.jar File
    * @throws IOException
    */
   protected File createDummyJar() throws IOException
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

   /**
    * Creates a simple package with the following contents:
    * 
    *  <package-name>
    *   |
    *   |--- dummy.jar
    *   |
    *   |--- package.xml
    *   
    * The package.xml is configured to install the dummy.jar to JBOSS_HOME/common/lib. Nothing 
    * else is configured in the package.xml
    *   
    * @param packageFileName The name of the package to be created. Ex: simple-package.jar
    *   Note: It is mandatory to specify the extension (.jar) which passing the packageFileName
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createSimplePackage(String packageFileName) throws IOException
   {
      File dummyJar = this.createDummyJar();

      // Now let's package the dummy.jar, package.xml into a package
      File simplePackage = new File(targetDir, packageFileName);
      JavaArchive pkg = JavaArchiveFactory.create(simplePackage.getName());
      pkg.addResource("dummy.jar", dummyJar);
      URL packageXmlURL = this.getResource(PackageManagerTestCase.class, "package-with-just-install-file.xml");
      File file = new File(packageXmlURL.getFile());
      pkg.addResource("package.xml", file);
      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, simplePackage);

      return simplePackage;
   }

   /**
    * Creates a package containing a file to install and a pre-install script. The package
    * will look as follows:
    * 
    *  <package-name>
    *   |
    *   |--- dummy.jar
    *   |
    *   |--- package.xml
    *   |
    *   |--- build.xml
    *   
    * The package.xml is configured to install the dummy.jar to JBOSS_HOME/server/default/deploy. 
    * Additionally, the package.xml is also configured for the pre-install script named build.xml,
    * which is available at the root of the package. The build.xml script is implemented
    * to place a file named "test.txt" under JBOSS_HOME/bin folder, when the script is run.
    *   
    * @param packageFileName The name of the package to be created. Ex: simple-package.jar
    *   Note: It is mandatory to specify the extension (.jar) which passing the packageFileName
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithPreInstallScript(String packageFileName) throws IOException
   {
      File dummyJar = this.createDummyJar();

      // Now let's package the dummy.jar, package.xml and build.xml into a package
      File packageWithPreInstallScript = new File(targetDir, packageFileName);
      JavaArchive pkg = JavaArchiveFactory.create(packageWithPreInstallScript.getName());
      pkg.addResource("dummy.jar", dummyJar);
      URL packageXmlURL = this.getResource(PackageManagerTestCase.class, "package-with-pre-install-script.xml");
      File packageXmlFile = new File(packageXmlURL.getFile());
      pkg.addResource("package.xml", packageXmlFile);
      URL buildXmlURL = this.getResource(PackageManagerTestCase.class, "build.xml");
      File buildFile = new File(buildXmlURL.getFile());
      pkg.addResource("build.xml", buildFile);

      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, packageWithPreInstallScript);
      return packageWithPreInstallScript;
   }

   /**
    * Creates a package  containing a file to install and a packaged-dependency. The package
    * will look as follows:
    * 
    *  <package-name>
    *   |
    *   |--- dependee-package.jar (this is the packaged dependency)
    *   |
    *   |--- package.xml
    *   |
    *   |--- dummy.jar
    *   
    * The package.xml is configured to install the dummy.jar to JBOSS_HOME/server/default/lib. Also
    * the package.xml is configured to mark dependee-package.jar as a packaged-dependency.
    *  
    * The "dependee" package contains the following:
    * 
    * <package-name>
    *   |
    *   |--- dummy.jar
    *   |
    *   |--- package.xml
    * 
    * The package.xml of the dependee package is configured to install the dummy.jar to JBOSS_HOME/common/lib.
    * Nothing else is configured in the package.xml of the dependee package
    *   
    * @param packageFileName The name of the package to be created. Ex: simple-package.jar
    *   Note: It is mandatory to specify the extension (.jar) which passing the packageFileName
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithSimplePackagedDependency(String packageFileName) throws IOException
   {
      // let's create a simple "dependee" package
      File dependeePackage = this.createSimplePackage("dependee-package.jar");

      // Now package this dependee package and also any other files in the dependent package
      File packageWithPackagedDependency = new File(targetDir, packageFileName);
      JavaArchive pkg = JavaArchiveFactory.create(packageWithPackagedDependency.getName());
      pkg.addResource("dependee-package.jar", dependeePackage);
      // package the other files
      File dummyJar = this.createDummyJar();
      pkg.addResource("dummy.jar", dummyJar);
      URL packageXmlURL = this.getResource(PackageManagerTestCase.class, "package-with-packaged-dependency.xml");
      File packageXmlFile = new File(packageXmlURL.getFile());
      pkg.addResource("package.xml", packageXmlFile);

      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, packageWithPackagedDependency);

      return packageWithPackagedDependency;

   }

   /**
    * Creates a JBoss AS-5 directory structure for the given {@code jbossHome}
    * 
    * @param jbossHome The server home
    * @throws IOException
    */
   private static void createJBossDirectoryStructure(File jbossHome) throws IOException
   {
      // bin folder
      File jbossBin = new File(jbossHome, "bin");
      jbossBin.mkdirs();
      // common/lib
      File jbossCommonLib = new File(jbossHome, "common/lib");
      jbossCommonLib.mkdirs();
      // server 
      File serverDefault = new File(jbossHome, "server");
      serverDefault.mkdirs();
      // create "default" and "all" server configs
      createServerProfile(jbossHome, "default");
      createServerProfile(jbossHome, "all");
   }

   /**
    * Create the directory structure for a JBoss AS-5 server profile
    * 
    * @param jbossHome JBOSS_HOME
    * @param serverProfileName The name of the server profile (ex: "default", "all")
    * @throws IOException
    */
   private static void createServerProfile(File jbossHome, String serverProfileName) throws IOException
   {
      // server/<servername>
      File serverProf = new File(jbossHome, "server/" + serverProfileName);
      serverProf.mkdirs();
      // server/<servername>/lib
      File serverProfLib = new File(serverProf, "lib");
      serverProfLib.mkdirs();
      // server/<servername>/conf
      File serverConf = new File(serverProf, "conf");
      serverConf.mkdirs();
      // server/<servername>/deploy
      File serverDeploy = new File(serverProf, "deploy");
      serverDeploy.mkdirs();
      // server/<servername>/deployers
      File serverDeployers = new File(serverProf, "deployers");
      serverDeployers.mkdirs();
   }

   /**
    * (Recursively) removes any files from the JBoss installation represented by
    * {@code jbossHome}
    * 
    * NOTE: Use with extreme caution and 
    * 
    * @param jbossHome
    * @throws IOException
    */
   protected void cleanupJBossInstance(File jbossHome) throws IOException
   {
      // Deleting is extremely dangerous, since we never know if the user 
      // unintentionally (or maliciously) passed a directory which is not JBOSS_HOME.
      // So let's atleast make sure that the jbossHome is within this project's 
      // "target" directory. If not, we won't delete anything (outside our scope)
      // and just throw an IOException

      if (this.isChildOfTargetFolder(jbossHome))
      {
         this.recursivelyDeleteFiles(jbossHome);
         return;
      }
      else
      {
         throw new IOException(
               jbossHome
                     + " is not under the project \"target\" folder -" +
                     		" Deleting the contents of that folder is not in the scope of this method");
      }

   }

   protected void assertFileExistenceUnderJBossHome(File jbossHome, String file)
   {
      File fileToBeChecked = new File(jbossHome, file);
      Assert.assertTrue("Expected to find file " + fileToBeChecked + " but not found", fileToBeChecked.exists());
   }
   
   /**
    * 
    * @param file
    * @return
    */
   private boolean isChildOfTargetFolder(File file)
   {
      if (file == null)
      {
         return false;
      }
      if (file.equals(this.targetDir))
      {
         return false;
      }
      file = file.getParentFile();
      while (file != null)
      {
         if (this.targetDir.equals(file))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * 
    * @param parent
    * @throws IOException
    */
   private void recursivelyDeleteFiles(File parent) throws IOException
   {
      File[] filesAndDirs = parent.listFiles();
      List<File> filesDirs = Arrays.asList(filesAndDirs);
      for (File file : filesDirs)
      {
         if (file.isFile())
         {
            file.delete();
         }
         else if (file.isDirectory())
         {
            // recurse
            this.recursivelyDeleteFiles(file);
         }
      }
   }

   protected File getPerTestTargetDir(Class<?> testClass)
   {
      File testCaseTargetDir = new File(targetDir, testClass.getName().replace('.', '/'));
      testCaseTargetDir.mkdirs();
      return testCaseTargetDir;
   }
}
