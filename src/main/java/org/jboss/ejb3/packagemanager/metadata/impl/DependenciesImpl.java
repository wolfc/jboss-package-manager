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
package org.jboss.ejb3.packagemanager.metadata.impl;

import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.packagemanager.metadata.Dependencies;
import org.jboss.ejb3.packagemanager.metadata.Package;
import org.jboss.ejb3.packagemanager.metadata.PackagedDependency;
import org.jboss.ejb3.packagemanager.metadata.UnProcessedDependencies;

/**
 * 
 * DependenciesImpl
 *
 * @see Dependencies
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DependenciesImpl implements Dependencies
{

   /**
    * The package to which this dependency metadata corresponds
    */
   private Package pkg;

   /**
    * Unprocessed dependencies
    */
   private UnProcessedDependencies unProcessedDeps;

   /**
    * A list of packaged dependencies 
    */
   private List<PackagedDependency> packagedDeps;

   /**
    * Constructor
    * @param pkgMetadata The package to which this dependencies correspond
    */
   public DependenciesImpl(Package pkgMetadata)
   {
      this.pkg = pkgMetadata;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Dependencies#getPackagedDependencies()
    */
   @Override
   public List<PackagedDependency> getPackagedDependencies()
   {
      return this.packagedDeps;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Dependencies#getUnProcessedDependencies()
    */
   @Override
   public UnProcessedDependencies getUnProcessedDependencies()
   {
      return this.unProcessedDeps;
   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Dependencies#setPackagedDependencies(java.util.List)
    */
   @Override
   public void setPackagedDependencies(List<PackagedDependency> packagedDependencies)
   {
      this.packagedDeps = packagedDependencies;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Dependencies#setUnProcessedDependencies(org.jboss.ejb3.packagemanager.metadata.UnProcessedDependencies)
    */
   @Override
   public void setUnProcessedDependencies(UnProcessedDependencies unProcessedDependencies)
   {
      this.unProcessedDeps = unProcessedDependencies;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Dependencies#getPackage()
    */
   @Override
   public Package getPackage()
   {
      return this.pkg;

   }

   /**
    * @see org.jboss.ejb3.packagemanager.metadata.Dependencies#addPackagedDependency(org.jboss.ejb3.packagemanager.metadata.PackagedDependency)
    */
   @Override
   public void addPackagedDependency(PackagedDependency packagedDep)
   {
      if (packagedDep == null)
      {
         return;
      }
      if (this.packagedDeps == null)
      {
         this.packagedDeps = new ArrayList<PackagedDependency>();
      }
      this.packagedDeps.add(packagedDep);

   }

}
