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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.ejb3.packagemanager.util.DBUtil;
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
    * File name of the package manager DB schema file
    */
   private static final String PACKAGE_MANAGER_DB_SCHEMA_FILE_NAME = "schema.sql";

   /**
    * Base dir
    */
   protected static File baseDir = new File(System.getProperty("basedir"));

   /**
    * The project's target dir
    */
   protected static File targetDir = new File(baseDir, "target");
   
   protected static final String DEFAULT_PACKAGE_VERSION = "1.0.0";
   
   protected static final String JAR_SUFFIX = ".jar";

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
   protected static File setupDummyJBoss(Class<?> testClass) throws IOException
   {
      File dummyJBossHome = new File(getPerTestTargetDir(testClass), "jboss-as");
      dummyJBossHome.mkdirs();
      createJBossDirectoryStructure(dummyJBossHome);

      return dummyJBossHome;

   }

   private static void setupDatabase(File dbHome) throws IOException, SQLException
   {
      System.setProperty("derby.system.home", dbHome.getAbsolutePath());
//      System.setProperty("derby.stream.error.logSeverityLevel", "20000");
//      System.setProperty("derby.language.logStatementText", "true");
//      //System.setProperty("derby.locks.escalationThreshold", "50000");
//      
//      System.setProperty("derby.locks.monitor", "true");
//      System.setProperty("derby.locks.deadlockTrace", "true");
      InputStream sql = Thread.currentThread().getContextClassLoader().getResourceAsStream(PACKAGE_MANAGER_DB_SCHEMA_FILE_NAME);

      if (sql == null)
      {
         throw new RuntimeException(
               "Could not find package-manager-sql-scripts.sql in classpath - Cannot setup database");
      }
      Connection conn = DriverManager.getConnection("jdbc:derby:pmdb;create=true");
      try
      {
         DBUtil.runSql(conn, sql);
      }
      finally
      {
         if (conn != null)
         {
            conn.close();
         }
      }
      logger.info("Successfully setup package manager database at " + dbHome);

   }

   protected static void removeInstalledPackages() throws IOException, SQLException
   {
      InputStream sql = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("remove-packages-script.sql");
      if (sql == null)
      {
         throw new RuntimeException("Could not find remove-packages-script.sql in classpath - Cannot cleanup database");
      }

      Connection conn = DriverManager.getConnection("jdbc:derby:pmdb");
      DBUtil.runSql(conn, sql);

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
   protected static File setupPackageManagerHome(Class<?> testClass) throws IOException
   {
      File pkgMgrHome = new File(getPerTestTargetDir(testClass), "pm-home");
      if (pkgMgrHome.exists())
      {
         recursivelyDeleteFiles(pkgMgrHome, true);
      }

      pkgMgrHome.mkdirs();
      File packageManagerDBHome = new File(pkgMgrHome, "package-manager-db-home");
      packageManagerDBHome.mkdirs();
      try
      {
         setupDatabase(packageManagerDBHome);
      }
      catch (SQLException sqle)
      {
         throw new RuntimeException(sqle);
      }
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

      File dummyJar = new File(getPerTestTargetDir(this.getClass()), "dummy.jar");
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
    * Creates a simple package with the default version {@link #DEFAULT_PACKAGE_VERSION}
    * with the following contents:
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
    * @param packageName The name of the package to be created. Ex: "simple-package"
    *   
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createSimplePackage(String packageName) throws IOException
   {
      return this.createSimplePackage(packageName, DEFAULT_PACKAGE_VERSION);
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
    * @param packageName The name of the package to be created. Ex: "simple-package"
    * @param packageVersion Version of the package  
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createSimplePackage(String packageName, String packageVersion) throws IOException
   {
      File dummyJar = this.createDummyJar();
      String packageFileName = packageName + JAR_SUFFIX;
      // Now let's package the dummy.jar, package.xml into a package
      File simplePackage = new File(getPerTestTargetDir(this.getClass()), packageFileName);
      JavaArchive pkg = JavaArchiveFactory.create(simplePackage.getName());
      pkg.addResource("dummy.jar", dummyJar);
      URL packageXmlURL = this.getResource(PackageManagerTestCase.class, "package-with-just-install-file.xml");
      File file = new File(packageXmlURL.getFile());
      File processedPackageXml = this.processPackageXml(file, packageName, packageVersion);
      pkg.addResource("package.xml", processedPackageXml);
      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, simplePackage);

      return simplePackage;
   }

   /**
    * Creates a package with the default version {@link #DEFAULT_PACKAGE_VERSION} and
    * containing a file to install and a pre-install script. The package
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
    * @param packageNam The name of the package to be created. Ex: simple-package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithPreInstallScript(String packageName) throws IOException
   {
      return this.createPackageWithPreInstallScript(packageName, DEFAULT_PACKAGE_VERSION);
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
    * @param packageNam The name of the package to be created. Ex: "simple-package"
    * @param packageVersion Version of the package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithPreInstallScript(String packageName, String packageVersion) throws IOException
   {
      
      URL packageXmlURL = this.getResource(PackageManagerTestCase.class, "package-with-pre-install-script.xml");
      File packageXmlFile = new File(packageXmlURL.getFile());
      File processedPackageXmlFile = this.processPackageXml(packageXmlFile, packageName, packageVersion);
      URL buildXmlURL = this.getResource(PackageManagerTestCase.class, "build.xml");
      File buildFile = new File(buildXmlURL.getFile());
      return this.createPackageWithScript(packageName, packageVersion, buildFile, processedPackageXmlFile);
   }
   
   
   
   /**
    * Creates a package with the {@link #DEFAULT_PACKAGE_VERSION} and
    * containing a file to install and a pre-install script. The package
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
    * to place a file named "pre-uninstall.txt" under JBOSS_HOME/bin folder, when the script is run.
    *   
    * @param packageName The name of the package to be created. Ex: simple-package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithPreUnInstallScript(String packageName) throws IOException
   {
      
      return this.createPackageWithPreUnInstallScript(packageName, DEFAULT_PACKAGE_VERSION);
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
    * to place a file named "pre-uninstall.txt" under JBOSS_HOME/bin folder, when the script is run.
    *   
    * @param packageName The name of the package to be created. Ex: simple-package
    * @param packageVersion Version of the package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithPreUnInstallScript(String packageName, String packageVersion) throws IOException
   {
      
      URL packageXmlURL = this.getResource(PackageManagerTestCase.class, "package-with-pre-uninstall-script.xml");
      File packageXmlFile = new File(packageXmlURL.getFile());
      File processedPackageXmlFile = this.processPackageXml(packageXmlFile, packageName, packageVersion);
      URL buildXmlURL = this.getResource(PackageManagerTestCase.class, "build.xml");
      File buildFile = new File(buildXmlURL.getFile());
      return this.createPackageWithScript(packageName, packageVersion, buildFile, processedPackageXmlFile);
   }
   
   /**
    * Creates a package with the {@link #DEFAULT_PACKAGE_VERSION} and 
    * containing a file to install and a pre-install script. The package
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
    * to place a file named "post-install.txt" under JBOSS_HOME/bin folder, when the script is run.
    *   
    * @param packageName The name of the package to be created. Ex: simple-package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithPostInstallScript(String packageName) throws IOException
   {
      
      return this.createPackageWithPostInstallScript(packageName, DEFAULT_PACKAGE_VERSION);
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
    * to place a file named "post-install.txt" under JBOSS_HOME/bin folder, when the script is run.
    *   
    * @param packageName The name of the package to be created. Ex: simple-package
    * @param packageVersion Version of the package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithPostInstallScript(String packageName, String packageVersion) throws IOException
   {
      
      URL packageXmlURL = this.getResource(PackageManagerTestCase.class, "package-with-post-install-script.xml");
      File packageXmlFile = new File(packageXmlURL.getFile());
      File processedPackageXmlFile = this.processPackageXml(packageXmlFile, packageName, packageVersion);
      URL buildXmlURL = this.getResource(PackageManagerTestCase.class, "build.xml");
      File buildFile = new File(buildXmlURL.getFile());
      return this.createPackageWithScript(packageName, packageVersion, buildFile, processedPackageXmlFile);
   }
   
   /**
    * Creates a package with the {@link #DEFAULT_PACKAGE_VERSION} and
    * containing a file to install and a pre-install script. The package
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
    * Additionally, the package.xml is also configured for the post-uninstall script named build.xml,
    * which is available at the root of the package. The build.xml script is implemented
    * to place a file named "post-uninstall.txt" from JBOSS_HOME/bin folder
    *   
    * @param packageName The name of the package to be created. Ex: simple-package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithPostUnInstallScript(String packageName) throws IOException
   {
      
     return this.createPackageWithPostUnInstallScript(packageName, DEFAULT_PACKAGE_VERSION);
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
    * Additionally, the package.xml is also configured for the post-uninstall script named build.xml,
    * which is available at the root of the package. The build.xml script is implemented
    * to place a file named "post-uninstall.txt" from JBOSS_HOME/bin folder
    *   
    * @param packageName The name of the package to be created. Ex: simple-package
    * @param packageVersion Version of the package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithPostUnInstallScript(String packageName, String packageVersion) throws IOException
   {
      
      URL packageXmlURL = this.getResource(PackageManagerTestCase.class, "package-with-post-uninstall-script.xml");
      File packageXmlFile = new File(packageXmlURL.getFile());
      File processedPackageXmlFile = this.processPackageXml(packageXmlFile, packageName, packageVersion);
      URL buildXmlURL = this.getResource(PackageManagerTestCase.class, "build.xml");
      File buildFile = new File(buildXmlURL.getFile());
      return this.createPackageWithScript(packageName, packageVersion, buildFile, processedPackageXmlFile);
   }
   
   /**
    * Utility method to create a package with a script file.
    * 
    * The package
    * will look as follows:
    * 
    *  <package-name>
    *   |
    *   |--- dummy.jar
    *   |
    *   |--- package.xml
    *   |
    *   |--- [script-file]
    *   
    * @param packageName Package file name
    * @param scriptFile Script file
    * @param packageXmlFile package.xml
    * @return Returns the created package
    * @throws IOException
    */
   private File createPackageWithScript(String packageName, String packageVersion, File scriptFile, File packageXmlFile) throws IOException
   {
      File dummyJar = this.createDummyJar();
      String packageFileName = packageName + JAR_SUFFIX;
      File packageWithScript = new File(getPerTestTargetDir(this.getClass()), packageFileName);
      JavaArchive pkg = JavaArchiveFactory.create(packageWithScript.getName());
      pkg.addResource("dummy.jar", dummyJar);
      pkg.addResource("package.xml", packageXmlFile);
      pkg.addResource(scriptFile.getName(), scriptFile);
      
      // now write out the package to disk
      logger.debug("Writing out the created package " + pkg.toString(true));
      this.exportZip(pkg, packageWithScript);
      return packageWithScript;
   }
   
   /**
    * Creates a package with the {@link #DEFAULT_PACKAGE_VERSION}
    * and containing a file to install and a packaged-dependency. The package
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
    * @param packageName The name of the package to be created. Ex: simple-package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithSimplePackagedDependency(String packageName) throws IOException
   {
      return this.createPackageWithSimplePackagedDependency(packageName, DEFAULT_PACKAGE_VERSION);

   }
   
   
   /**
    * Creates a package with the {@link #DEFAULT_PACKAGE_VERSION}
    * and containing a file to install and a packaged-dependency. The package
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
    * @param packageName The name of the package to be created. Ex: simple-package
    * @param packageVersion Version of the package
    * @return Returns the {@link File} corresponding the to the created package
    * @throws IOException If any IO exceptions occur during the package creation
    */
   protected File createPackageWithSimplePackagedDependency(String packageName, String packageVersion) throws IOException
   {
      String packageFileName = packageName + JAR_SUFFIX;
      
      // let's create a simple "dependee" package
      File dependeePackage = this.createSimplePackage("dependee-package");

      // Now package this dependee package and also any other files in the dependent package
      File packageWithPackagedDependency = new File(getPerTestTargetDir(this.getClass()), packageFileName);
      JavaArchive pkg = JavaArchiveFactory.create(packageWithPackagedDependency.getName());
      pkg.addResource("dependee-package.jar", dependeePackage);
      // package the other files
      File dummyJar = this.createDummyJar();
      pkg.addResource("dummy.jar", dummyJar);
      URL packageXmlURL = this.getResource(PackageManagerTestCase.class, "package-with-packaged-dependency.xml");
      File packageXmlFile = new File(packageXmlURL.getFile());
      File processedPacackageXmlFile = this.processPackageXml(packageXmlFile, packageName, packageVersion);
      pkg.addResource("package.xml", processedPacackageXmlFile);

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
      cleanupJBossInstance(jbossHome, true);
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
      // server/<servername>/data
      File serverData = new File(serverProf, "data");
      serverData.mkdirs();
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
   protected static void cleanupJBossInstance(File jbossHome) throws IOException
   {
      cleanupJBossInstance(jbossHome, false);

   }

   protected static void cleanupJBossInstance(File jbossHome, boolean deleteDir) throws IOException
   {
      // Deleting is extremely dangerous, since we never know if the user 
      // unintentionally (or maliciously) passed a directory which is not JBOSS_HOME.
      // So let's atleast make sure that the jbossHome is within this project's 
      // "target" directory. If not, we won't delete anything (outside our scope)
      // and just throw an IOException

      if (isChildOfTargetFolder(jbossHome))
      {
         recursivelyDeleteFiles(jbossHome, deleteDir);
      }
      else
      {
         throw new IOException(jbossHome + " is not under the project \"target\" folder -"
               + " Deleting the contents of that folder is not in the scope of this method");
      }

      return;

   }

   protected void assertFileExistenceUnderJBossHome(File jbossHome, String file)
   {
      File fileToBeChecked = new File(jbossHome, file);
      Assert.assertTrue("Expected to find file " + fileToBeChecked + " but not found", fileToBeChecked.exists());
   }

   protected void assertFileAbsenceUnderJBossHome(File jbossHome, String file)
   {
      File fileToBeChecked = new File(jbossHome, file);
      Assert.assertFalse("Did not expect to find file " + fileToBeChecked, fileToBeChecked.exists());
   }

   /**
    * 
    * @param file
    * @return
    */
   private static boolean isChildOfTargetFolder(File file)
   {
      if (file == null)
      {
         return false;
      }
      if (file.equals(targetDir))
      {
         return false;
      }
      file = file.getParentFile();
      while (file != null)
      {
         if (targetDir.equals(file))
         {
            return true;
         }
         file = file.getParentFile();
      }
      return false;
   }

   /**
    * 
    * @param parent
    * @throws IOException
    */
   private static void recursivelyDeleteFiles(File parent, boolean deleteDir) throws IOException
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
            recursivelyDeleteFiles(file, deleteDir);
         }
      }
      if (deleteDir)
      {
         parent.delete();
      }

   }

   protected static File getPerTestTargetDir(Class<?> testClass)
   {
      File testCaseTargetDir = new File(targetDir, testClass.getName().replace('.', '/'));
      testCaseTargetDir.mkdirs();
      return testCaseTargetDir;
   }
   
   /**
    * Processes the <code>originalPackageXml</code> file to replace any occurences of
    * ${package.name} and ${package.version} variables in that file. These variables
    * are replaced with the actual values. The original package.xml file is left unchanged
    * and instead a copy of the package.xml with the variable replacements is returned back.
    * 
    * @param originalPackageXml The package.xml which needs to be processed
    * @param packageName Package name
    * @param packageVersion Package version
    * @return Returns the processed copy of package.xml
    * @throws IOException If any IO errors occur during processing
    */
   protected File processPackageXml(File originalPackageXml, String packageName, String packageVersion) throws IOException
   {
      if (!originalPackageXml.exists())
      {
         throw new IOException(originalPackageXml + " does not exist");
      }
      if (originalPackageXml.isDirectory())
      {
         throw new IOException(originalPackageXml + " is a directory. Cannot create package.xml out of a directory!");
      }
      BufferedReader bufferedReader = new BufferedReader(new FileReader(originalPackageXml));
      File resultantPackageXmlFile = File.createTempFile("pkg", "xml", getPerTestTargetDir(this.getClass()));
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(resultantPackageXmlFile));
      try
      {
         String line = null;
         while ((line = bufferedReader.readLine()) != null)
         {
            line = line.replaceAll("\\$\\{package.name\\}", packageName);
            line = line.replaceAll("\\$\\{package.version\\}", packageVersion);
            bufferedWriter.write(line);
            bufferedWriter.write("\n");
         }
      }
      finally
      {
         if (bufferedReader != null)
         {
            bufferedReader.close();
         }
         if (bufferedWriter != null)
         {
            bufferedWriter.close();
         }
      }
      return resultantPackageXmlFile;

   }
}
