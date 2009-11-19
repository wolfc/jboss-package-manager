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

import java.util.Comparator;

/**
 * PackageVersionComparator
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PackageVersionComparator implements Comparator<String>
{

   /**
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   @Override
   public int compare(String version1, String version2)
   {
      if (version1.equals(version2))
      {
         return 0;
      }
      // TODO: This is a copy of php version_compare algo. 
      // Change this and implement it on the lines of OSGi version numbering.
      // Instead of writing the version compare logic all over again, see if there's 
      // a reusable util
      String rev1 = version1.replaceAll("([a-zA-Z])(\\d)", "$1.$2");
      rev1 = rev1.replaceAll("(\\d)([a-zA-Z])", "$1.$2");
      String rev2 = version2.replaceAll("([a-zA-Z])(\\d)", "$1.$2");
      rev2 = rev2.replaceAll("(\\d)([a-zA-Z])", "$1.$2");

      String[] parts1 = rev1.split("[\\._\\-\\+]");
      String[] parts2 = rev2.split("[\\._\\-\\+]");

      int i = 0;
      for (; i < parts1.length && i < parts2.length; i++)
      {
         if (parts1[i].equals(parts2[i]))
         {
            continue;
         }
         boolean is1Number = isNumber(parts1[i]);
         boolean is2Number = isNumber(parts2[i]);
         if (is1Number && !is2Number)
         {
            return 1;
         }
         if (is2Number && !is1Number)
         {
            return -1;
         }
         if (is1Number && is2Number)
         {
            return Long.valueOf(parts1[i]).compareTo(Long.valueOf(parts2[i]));
         }
         // both are strings, we compare them
         return parts1[i].compareTo(parts2[i]);
      }
      if (i < parts1.length)
      {
         return isNumber(parts1[i]) ? 1 : -1;
      }
      if (i < parts2.length)
      {
         return isNumber(parts2[i]) ? -1 : 1;
      }
      return 0;
   }

   private boolean isNumber(String str)
   {
      return str.matches("\\d+");
   }
}
