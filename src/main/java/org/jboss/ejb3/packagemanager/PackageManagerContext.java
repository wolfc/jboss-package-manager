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

package org.jboss.ejb3.packagemanager;

import javax.transaction.TransactionManager;

import org.jboss.ejb3.packagemanager.db.PackageDatabaseManager;


/**
 * Author: Jaikiran Pai
 */
public interface PackageManagerContext
{
    /**
     * Returns the package manager environment
     * associated with this context
     * 
     * @return
     */
    PackageManagerEnvironment getPackageManagerEnvironment();

    /**
     * Returns the JBoss Server Home absolute path
      * @return
     */
    String getJBossServerHome();
    
    /**
     * Returns the location (relative to the package manager home) where the script files
     * of the package corresponding to the <code>pkgContext</code> is persisted. 
     * Persistent script files can be used during uninstallation of packages.
     * 
     * @param pkgContext Package context, whose script location is required 
     * @return
     */
    String getScriptStoreLocation(PackageContext pkgContext);
    
    TransactionManager getTransactionManager();
    
    PackageDatabaseManager getDatabaseManager();
    
}
