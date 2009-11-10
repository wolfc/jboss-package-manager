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

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.PackageManagerFactory;
import org.jboss.ejb3.packagemanager.test.common.PackageManagerTestCase;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * UpgradeUnitTestCase
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

   private static File perTestTargetDir = getPerTestTargetDir(UpgradeUnitTestCase.class);

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
   
   @Test
   public void testSimpleUpgrade() throws Exception
   {
      // first install a simple package, then call upgrade on the same package
      File simplePackage = this.createSimplePackage("simple-package.jar");
      
      this.pkgMgr.installPackage(simplePackage.getAbsolutePath());
      
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");
      
      // now upgrade
      this.pkgMgr.updatePackage(simplePackage.getAbsolutePath());
      this.assertFileExistenceUnderJBossHome(jbossHome, "common/lib/dummy.jar");
   }
}
