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
package org.jboss.ejb3.packagemanager.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.annotation.TransactionAttribute;
import org.jboss.ejb3.packagemanager.annotation.TransactionAttributeType;
import org.jboss.logging.Logger;

/**
 * PackageManagerInvocationHandler
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PackageManagerInvocationHandler implements InvocationHandler
{

   private PackageManager packageManager;

   private static Logger logger = Logger.getLogger(PackageManagerInvocationHandler.class);

   public PackageManagerInvocationHandler(PackageManager packageManager)
   {
      this.packageManager = packageManager;
   }

   /**
    * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    */
   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      TransactionAttribute txAnnotation = this.getTxAttribute(method);
      // nothing special to do, just invoke 
      if (txAnnotation == null)
      {
         return method.invoke(this.packageManager, args);
      }
      TransactionAttributeType txType = txAnnotation.value();
      switch (txType)
      {
         case REQUIRED :
            return this.invokeInCurrentTx(proxy, method, args);
         case REQUIRES_NEW :
            return this.invokeInNewTx(proxy, method, args);
         default :
            throw new RuntimeException("Unknow tx type " + txType);
      }
   }

   private Object invokeInCurrentTx(Object proxy, Method method, Object[] args) throws Throwable
   {
      TransactionManager txManager = this.packageManager.getTransactionManager();
      Transaction currentTx = txManager.getTransaction();
      boolean txInitiator = false;
      if (currentTx == null)
      {
         // start new tx
         txManager.begin();
         logger.debug("Started in invocation handler " + txManager.getTransaction() + " for method " + method);
         currentTx = txManager.getTransaction();
         registerForSynchronization(txManager.getTransaction());
         txInitiator = true;
      }
      try
      {

         Object result = method.invoke(this.packageManager, args);
         if (txInitiator)
         {
            if (txManager.getStatus() == Status.STATUS_MARKED_ROLLBACK)
            {
               txManager.rollback();
            }
            else
            {
               logger.debug("Commiting in invocation handler " + txManager.getTransaction() + " for method " + method);
               txManager.commit();
               logger.debug("Commited in invocation handler " + txManager.getTransaction() + " for method " + method);
            }

         }
         return result;
      }
      catch (InvocationTargetException ite)
      {
         Throwable cause = ite.getCause();
         if (!txInitiator)
         {
            txManager.setRollbackOnly();
         }
         else
         {
            txManager.rollback();
         }

         throw cause;
      }

   }

   private Object invokeInNewTx(Object proxy, Method method, Object[] args) throws Throwable
   {
      TransactionManager txManager = this.packageManager.getTransactionManager();
      Transaction currentTx = txManager.getTransaction();
      try
      {
         if (currentTx != null)
         {
            // suspend current tx
            txManager.suspend();
         }
         // start new tx and invoke the method
         txManager.begin();
         Transaction newTx = txManager.getTransaction();
         registerForSynchronization(newTx);
         try
         {
            Object result = method.invoke(this.packageManager, args);
            if (newTx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
            {
               // rollback
               newTx.rollback();
            }
            else
            {
               // method successfully completed, so commit tx
               newTx.commit();
            }
            return result;
         }
         catch (InvocationTargetException ite)
         {
            Throwable cause = ite.getCause();
            // rollback the tx in which this method was invoked
            newTx.rollback();
            throw cause;
         }
      }
      finally
      {
         if (currentTx != null)
         {
            txManager.resume(currentTx);
         }
      }

   }

   private TransactionAttribute getTxAttribute(Method method) throws SecurityException, NoSuchMethodException
   {
      Method methodOnPackageManagerImpl = this.packageManager.getClass().getMethod(method.getName(),
            method.getParameterTypes());
      return methodOnPackageManagerImpl.getAnnotation(TransactionAttribute.class);
   }

   private void registerForSynchronization(Transaction tx) throws IllegalStateException, RollbackException,
         SystemException
   {
      if (this.packageManager instanceof Synchronization)
      {
         tx.registerSynchronization((Synchronization) this.packageManager);
      }
   }
}
