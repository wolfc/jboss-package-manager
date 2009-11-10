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

import java.util.Properties;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 * JBossJTABasedTransactionManagerLookup
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class JBossJTABasedTransactionManagerLookup implements TransactionManagerLookup
{

  /**
   * @see org.hibernate.transaction.TransactionManagerLookup#getTransactionIdentifier(javax.transaction.Transaction)
   */
   @Override
   public Object getTransactionIdentifier(Transaction transaction)
   {
      // we just return back the transaction (and that's allowed as per the javadocs of this method)
      return transaction;
   }

   /**
    * @see org.hibernate.transaction.TransactionManagerLookup#getTransactionManager(java.util.Properties)
    */
   @Override
   public TransactionManager getTransactionManager(Properties props) throws HibernateException
   {
      // send back our transaction manager 
      return TransactionManagerImpl.getInstance();
   }

   /**
    * @see org.hibernate.transaction.TransactionManagerLookup#getUserTransactionName()
    */
   @Override
   public String getUserTransactionName()
   {
      // we return null
      return null;
   }

}
