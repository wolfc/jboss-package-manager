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
package org.jboss.ejb3.packagemanager.tx;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * TransactionManagerImpl
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class TransactionManagerImpl implements TransactionManager
{

   private TransactionManager delegate = com.arjuna.ats.jta.TransactionManager.transactionManager();
   
   private static TransactionManagerImpl singleInstance;
   
   private TransactionManagerImpl()
   {
      
   }
   
   public synchronized static TransactionManager getInstance()
   {
      if (singleInstance == null)
      {
         singleInstance = new TransactionManagerImpl();
      }
      return singleInstance;
   }

   /* (non-Javadoc)
    * @see javax.transaction.TransactionManager#begin()
    */
   @Override
   public void begin() throws NotSupportedException, SystemException
   {
      delegate.begin();

   }

   /* (non-Javadoc)
    * @see javax.transaction.TransactionManager#commit()
    */
   @Override
   public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
         SecurityException, IllegalStateException, SystemException
   {
      delegate.commit();

   }

   /* (non-Javadoc)
    * @see javax.transaction.TransactionManager#getStatus()
    */
   @Override
   public int getStatus() throws SystemException
   {
      return delegate.getStatus();
   }

   /* (non-Javadoc)
    * @see javax.transaction.TransactionManager#getTransaction()
    */
   @Override
   public Transaction getTransaction() throws SystemException
   {
      return delegate.getTransaction();
   }

   /* (non-Javadoc)
    * @see javax.transaction.TransactionManager#resume(javax.transaction.Transaction)
    */
   @Override
   public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException
   {
      delegate.resume(tobj);

   }

   /* (non-Javadoc)
    * @see javax.transaction.TransactionManager#rollback()
    */
   @Override
   public void rollback() throws IllegalStateException, SecurityException, SystemException
   {
      delegate.rollback();

   }

   /* (non-Javadoc)
    * @see javax.transaction.TransactionManager#setRollbackOnly()
    */
   @Override
   public void setRollbackOnly() throws IllegalStateException, SystemException
   {
      delegate.setRollbackOnly();

   }

   /* (non-Javadoc)
    * @see javax.transaction.TransactionManager#setTransactionTimeout(int)
    */
   @Override
   public void setTransactionTimeout(int seconds) throws SystemException
   {
      delegate.setTransactionTimeout(seconds);

   }

   /* (non-Javadoc)
    * @see javax.transaction.TransactionManager#suspend()
    */
   @Override
   public Transaction suspend() throws SystemException
   {
      return delegate.suspend();
   }

}
