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
package org.jboss.ejb3.packagemanager.retriever.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageSource;
import org.jboss.ejb3.packagemanager.exception.PackageRetrievalException;
import org.jboss.ejb3.packagemanager.retriever.PackageRetriever;
import org.jboss.ejb3.packagemanager.util.IOUtil;

/**
 * HttpPackageRetriever
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class HttpPackageRetriever implements PackageRetriever
{

   /**
    * @see org.jboss.ejb3.packagemanager.retriever.PackageRetriever#retrievePackage(PackageManager, URL)
    */
   public PackageSource retrievePackage(PackageManager pkgMgr, URL packagePath) throws PackageRetrievalException
   {
      if (!packagePath.getProtocol().equals("http"))
      {
         throw new PackageRetrievalException("Cannot handle " + packagePath);
      }
      HttpClient httpClient = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(packagePath.toExternalForm());
      HttpResponse httpResponse = null;
      try
      {
         httpResponse = httpClient.execute(httpGet);
      }
      catch (Exception e)
      {
         throw new PackageRetrievalException("Exception while retrieving package " + packagePath, e);
      }
      if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) 
      {
         throw new PackageRetrievalException("Http retrieval wasn't successful, returned status code  " + httpResponse.getStatusLine().getStatusCode());
      }
      HttpEntity httpEntity = httpResponse.getEntity();
      
      try
      {
         // TODO: should this tmp be deleted on exit?
         File tmpPkgFile = File.createTempFile("tmp", ".jar", pkgMgr.getPackageManagerEnvironment().getPackageManagerTmpDir());
         FileOutputStream fos = new FileOutputStream(tmpPkgFile);
         BufferedOutputStream bos = null;
         BufferedInputStream bis = null;
         try
         {
            bos = new BufferedOutputStream(fos);
            InputStream is = httpEntity.getContent();
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
   
         // package has been retrieved to tmp location, now unpack it to a subfolder in package-manager build folder
         File extractedPackageDir = new File(pkgMgr.getPackageManagerEnvironment().getPackageManagerBuildDir(), tmpPkgFile
               .getName());
         if (!extractedPackageDir.exists())
         {
            extractedPackageDir.mkdirs();
         }
         
            IOUtil.extractJarFile(extractedPackageDir, new JarFile(tmpPkgFile));
            // validate that it contains a package.xml
            File packageXml = new File(extractedPackageDir, "package.xml");
            if (!packageXml.exists())
            {
               throw new PackageRetrievalException(packagePath + " is not a valid package - it does not contain a package.xml");
            }
            // create a package source out of this
            return new PackageSource(extractedPackageDir);
      }
      catch (IOException ioe)
      {
         throw new PackageRetrievalException("Could not process the retrieved package", ioe);
      }
      // TODO: I need to read the HttpClient 4.x javadocs to figure out the API for closing the
      // Http connection

   }
}
