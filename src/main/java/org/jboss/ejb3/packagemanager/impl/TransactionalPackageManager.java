package org.jboss.ejb3.packagemanager.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;

import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageManagerEnvironment;
import org.jboss.ejb3.packagemanager.annotation.TransactionAttribute;
import org.jboss.ejb3.packagemanager.annotation.TransactionAttributeType;
import org.jboss.ejb3.packagemanager.exception.PackageManagerException;
import org.jboss.ejb3.packagemanager.exception.PackageNotInstalledException;
import org.jboss.ejb3.packagemanager.option.InstallOptions;
import org.jboss.ejb3.packagemanager.option.UnInstallOptions;
import org.jboss.ejb3.packagemanager.option.UpgradeOptions;
import org.jboss.ejb3.packagemanager.proxy.TransactionalPackageManagerInvocationHandler;
import org.jboss.ejb3.packagemanager.tx.TransactionManagerImpl;
import org.jboss.logging.Logger;

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

/**
 * TransactionalPackageManager
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class TransactionalPackageManager extends DefaultPackageManagerImpl implements Synchronization
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(TransactionalPackageManager.class);

   /**
    * Transaction manager
    */
   private TransactionManager txManager;

   /**
    * 
    * @param environment
    * @param jbossHome
    * @return
    */
   public static PackageManager createNewInstance(PackageManagerEnvironment environment, String jbossHome)
   {
      PackageManager packageManager = new TransactionalPackageManager(environment, jbossHome);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InvocationHandler invocationHandler = new TransactionalPackageManagerInvocationHandler(packageManager);
      return (PackageManager) Proxy.newProxyInstance(cl, new Class[]
      {PackageManager.class}, invocationHandler);
   }

   /**
    * @param environment
    * @param jbossHome
    */
   protected TransactionalPackageManager(PackageManagerEnvironment environment, String jbossHome)
   {
      super(environment, jbossHome);
      this.txManager = TransactionManagerImpl.getInstance();
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#getTransactionManager()
    */
   @Override
   public TransactionManager getTransactionManager()
   {
      return this.txManager;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#installPackage(java.lang.String)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void installPackage(String pkgPath) throws PackageManagerException
   {
      super.installPackage(pkgPath);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#installPackage(java.net.URL)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void installPackage(URL packageURL) throws PackageManagerException
   {
      super.installPackage(packageURL);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#installPackage(java.lang.String, org.jboss.ejb3.packagemanager.option.InstallOptions)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void installPackage(String pkgPath, InstallOptions installOptions) throws PackageManagerException
   {
      super.installPackage(pkgPath, installOptions);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#installPackage(java.net.URL, org.jboss.ejb3.packagemanager.option.InstallOptions)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void installPackage(URL packageURL, InstallOptions installOptions) throws PackageManagerException
   {
      super.installPackage(packageURL, installOptions);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#updatePackage(java.lang.String)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void updatePackage(String packageFilePath) throws PackageManagerException
   {
      super.updatePackage(packageFilePath);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#updatePackage(java.lang.String, org.jboss.ejb3.packagemanager.option.UpgradeOptions)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void updatePackage(String packageFilePath, UpgradeOptions upgradeOptions) throws PackageManagerException
   {
      super.updatePackage(packageFilePath, upgradeOptions);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#updatePackage(java.net.URL)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void updatePackage(URL packageURL) throws PackageManagerException
   {
      super.updatePackage(packageURL);
   }

   /** 
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#updatePackage(java.net.URL, org.jboss.ejb3.packagemanager.option.UpgradeOptions)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void updatePackage(URL packageURL, UpgradeOptions upgradeOptions) throws PackageManagerException
   {
      super.updatePackage(packageURL, upgradeOptions);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#removePackage(java.lang.String)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void removePackage(String packageName) throws PackageNotInstalledException, PackageManagerException
   {
      super.removePackage(packageName);
   }

   /**
    * @see org.jboss.ejb3.packagemanager.impl.DefaultPackageManagerImpl#removePackage(java.lang.String, org.jboss.ejb3.packagemanager.option.UnInstallOptions)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void removePackage(String packageName, UnInstallOptions uninstallOptions)
         throws PackageNotInstalledException, PackageManagerException
   {
      super.removePackage(packageName, uninstallOptions);
   }

   /**
    * @see javax.transaction.Synchronization#afterCompletion(int)
    */
   @Override
   public void afterCompletion(int status)
   {
      if (this.getDatabaseManager() instanceof Synchronization)
      {
         Synchronization dbManager = (Synchronization) this.getDatabaseManager();
         dbManager.afterCompletion(status);
      }

   }

   /**
    * @see javax.transaction.Synchronization#beforeCompletion()
    */
   @Override
   public void beforeCompletion()
   {
      if (this.getDatabaseManager() instanceof Synchronization)
      {
         Synchronization dbManager = (Synchronization) this.getDatabaseManager();
         dbManager.beforeCompletion();
      }

   }

}
