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
package org.jboss.ejb3.packagemanager.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.logging.Logger;

/**
 * DBUtil
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class DBUtil
{

   private static Logger logger = Logger.getLogger(DBUtil.class);

   public static void runSql(Connection conn, File sqlFile) throws IOException, SQLException
   {
      logger.info("SQL being run from file " + sqlFile);
      InputStream sql = new FileInputStream(sqlFile);

      runSql(conn, sql);

   }

   public static void runSql(Connection conn, InputStream sql) throws IOException, SQLException
   {

      Reader reader = new InputStreamReader(sql);
      BufferedReader bufferedReader = new BufferedReader(reader);
      long lineNo = 0;
      Statement statement = conn.createStatement();
      try
      {
         StringBuffer trimmedSql = new StringBuffer();
         for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
         {
            try
            {
               lineNo++;
               line = line.trim();
               //String trimmedSql = line.trim();
               if (line.length() == 0 || line.startsWith("--") || line.startsWith("//")
                     || line.startsWith("/*"))
               {
                  continue;
               }
               else
               {
                  trimmedSql.append(" ");
                  trimmedSql.append(line);
                  if (trimmedSql.toString().endsWith(";"))
                  {
                     String sqlToRun = trimmedSql.substring(0, trimmedSql.length() - 1);
                     logger.info(sqlToRun);
                     statement.execute(sqlToRun);
                     trimmedSql = new StringBuffer();
                  }
                  
               }
            }
            catch (SQLException sqle)
            {
               logger.error("SQLException at line number " + lineNo, sqle);
               throw sqle;
            }
         }
      }
      finally 
      {
         if (statement != null)
         {
            statement.close();
         }
      }
   }
}
