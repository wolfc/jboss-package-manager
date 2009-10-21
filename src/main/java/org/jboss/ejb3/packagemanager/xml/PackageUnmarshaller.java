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
package org.jboss.ejb3.packagemanager.xml;

import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;
import org.jboss.ejb3.packagemanager.metadata.Dependencies;
import org.jboss.ejb3.packagemanager.metadata.InstallFile;
import org.jboss.ejb3.packagemanager.metadata.Package;
import org.jboss.ejb3.packagemanager.metadata.PackagedDependency;
import org.jboss.ejb3.packagemanager.metadata.PostInstall;
import org.jboss.ejb3.packagemanager.metadata.PreInstall;
import org.jboss.ejb3.packagemanager.metadata.Script;
import org.jboss.ejb3.packagemanager.metadata.SystemRequirements;
import org.jboss.ejb3.packagemanager.metadata.UnProcessedDependencies;
import org.jboss.ejb3.packagemanager.metadata.impl.DependenciesImpl;
import org.jboss.ejb3.packagemanager.metadata.impl.InstallFileImpl;
import org.jboss.ejb3.packagemanager.metadata.impl.PackageImpl;
import org.jboss.ejb3.packagemanager.metadata.impl.PackagedDependencyImpl;
import org.jboss.ejb3.packagemanager.metadata.impl.PostInstallImpl;
import org.jboss.ejb3.packagemanager.metadata.impl.PostInstallScript;
import org.jboss.ejb3.packagemanager.metadata.impl.PreInstallImpl;
import org.jboss.ejb3.packagemanager.metadata.impl.PreInstallScript;
import org.jboss.ejb3.packagemanager.metadata.impl.UnProcessedDependenciesImpl;

/**
 * PackageXMLParser
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PackageUnmarshaller
{

   /**
    * Creates {@link Package} out of the URL pointing to a package.xml file
    * 
    * @param packageXml package.xml URL 
    * @return Returns the {@link Package} corresponding to the package.xml
    * @throws Exception If any exceptions occur during processing the package.xml
    */
   public Package unmarshal(URL packageXml) throws Exception
   {


      XMLInputFactory2 xmlFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();
      XMLStreamReader2 xmlStreamReader = (XMLStreamReader2) xmlFactory.createXMLStreamReader(packageXml.openStream());

      // create a validator for the package.xml
      XMLValidationSchemaFactory validationSchemaFactory = XMLValidationSchemaFactory
            .newInstance(XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA);
      // TODO: Is this good enough to get hold of package.xsd? Need to think about this.
      URL packageXsd = Thread.currentThread().getContextClassLoader().getResource("package.xsd");
      XMLValidationSchema schema = validationSchemaFactory.createSchema(packageXsd);;
      // enable validation (note: validation will happen during parse)
      xmlStreamReader.validateAgainst(schema);
      
      // parse the xml
      Package pkgMetadata = null;
      while (xmlStreamReader.hasNext())
      {
         int event = xmlStreamReader.next();
         if (event == XMLEvent.START_ELEMENT && xmlStreamReader.getLocalName().equals("package"))
         {
            pkgMetadata = processPackage(xmlStreamReader);
         }
      }
      return pkgMetadata;
   }

   private Package processPackage(XMLStreamReader2 xmlStreamReader) throws Exception
   {
      Package pkgMeta = new PackageImpl();
      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("name".equals(name))
         {
            pkgMeta.setName(xmlStreamReader.getAttributeValue(i));
         }
         else if ("version".equals(name))
         {
            pkgMeta.setVersion(xmlStreamReader.getAttributeValue(i));
         }

      }
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         switch (event)
         {
            case XMLEvent.START_ELEMENT :
               String childElement = xmlStreamReader.getLocalName();
               if (childElement.equals("system-requirements"))
               {
                  SystemRequirements sysReqs = processSystemRequirements(pkgMeta, xmlStreamReader);
                  pkgMeta.setSystemRequirements(sysReqs);
               }
               else if (childElement.equals("file"))
               {
                  InstallFile file = processFiles(pkgMeta, xmlStreamReader);
                  pkgMeta.addFile(file);
               }
               else if (childElement.equals("pre-install"))
               {
                  PreInstall preInstall = processPreInstall(pkgMeta, xmlStreamReader);
                  pkgMeta.setPreInstall(preInstall);
               }
               else if (childElement.equals("post-install"))
               {
                  PostInstall postInstall = processPostInstall(pkgMeta, xmlStreamReader);
                  pkgMeta.setPostInstall(postInstall);
               }
               else if (childElement.equals("dependencies"))
               {
                  Dependencies dependencies = this.processDependencies(pkgMeta, xmlStreamReader);
                  pkgMeta.setDependencies(dependencies);
                  
               }
               break;

         }
         event = xmlStreamReader.next();
      }
      return pkgMeta;

   }

   /**
    * 
    * @param pkgMeta
    * @param xmlStreamReader
    * @return
    * @throws Exception
    */
   private SystemRequirements processSystemRequirements(Package pkgMeta,
         XMLStreamReader2 xmlStreamReader) throws Exception
   {
      // TODO Implement
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         event = xmlStreamReader.next();

      }
      return null;
   }

   /**
    * 
    * @param pkgMeta
    * @param xmlStreamReader
    * @return
    * @throws Exception
    */
   private InstallFile processFiles(Package pkgMeta, XMLStreamReader2 xmlStreamReader) throws Exception
   {
      InstallFile fileMeta = new InstallFileImpl(pkgMeta);
      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("src-path".equals(name))
         {
            fileMeta.setSrcPath(xmlStreamReader.getAttributeValue(i));
         }
         else if ("name".equals(name))
         {
            fileMeta.setName(xmlStreamReader.getAttributeValue(i));
         }
         else if ("dest-path".equals(name))
         {
            fileMeta.setDestPath(xmlStreamReader.getAttributeValue(i));
         }
         else if ("type".equals(name))
         {
            fileMeta.setType(xmlStreamReader.getAttributeValue(i));
         }

      }
      // consume the end event of file
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         event = xmlStreamReader.next();

      }
      
      return fileMeta;
   }

   /**
    * 
    * @param pkgMeta
    * @param xmlStreamReader
    * @return
    * @throws Exception
    */
   private PreInstall processPreInstall(Package pkgMeta, XMLStreamReader2 xmlStreamReader)
         throws Exception
   {
      PreInstall preInstall = new PreInstallImpl(pkgMeta);
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         switch (event)
         {
            case XMLEvent.START_ELEMENT :
               String childElement = xmlStreamReader.getLocalName();
               if (childElement.equals("script"))
               {
                  Script script = processPreInstallScript(preInstall, xmlStreamReader);
                  preInstall.addScript(script);
               }
               break;
         }
         event = xmlStreamReader.next();
      }
      return preInstall;
   }

   /**
    * 
    * @param pkgMeta
    * @param xmlStreamReader
    * @return
    * @throws Exception
    */
   private PostInstall processPostInstall(Package pkgMeta, XMLStreamReader2 xmlStreamReader)
         throws Exception
   {
      PostInstall postInstall = new PostInstallImpl(pkgMeta);
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         switch (event)
         {
            case XMLEvent.START_ELEMENT :
               String childElement = xmlStreamReader.getLocalName();
               if (childElement.equals("script"))
               {
                  Script script = processPostInstallScript(postInstall, xmlStreamReader);
                  postInstall.addScript(script);
               }
               break;
         }
         event = xmlStreamReader.next();
      }
      return postInstall;
   }

   /**
    * 
    * @param preInstallMeta
    * @param xmlStreamReader
    * @return
    * @throws Exception
    */
   private PreInstallScript processPreInstallScript(PreInstall preInstallMeta, XMLStreamReader2 xmlStreamReader)
         throws Exception
   {
      PreInstallScript preInstallScript = new PreInstallScript(preInstallMeta);
      processScript(preInstallScript, xmlStreamReader);
      return preInstallScript;
   }

   /**
    * 
    * @param preInstallMeta
    * @param xmlStreamReader
    * @return
    * @throws Exception
    */
   private PostInstallScript processPostInstallScript(PostInstall postInstallMeta,
         XMLStreamReader2 xmlStreamReader) throws Exception
   {
      PostInstallScript postInstallScript = new PostInstallScript(postInstallMeta);
      processScript(postInstallScript, xmlStreamReader);
      return postInstallScript;
   }

   /**
    * 
    * @param xmlStreamReader
    * @return
    */
   private Script processScript(Script script, XMLStreamReader2 xmlStreamReader) throws Exception
   {

      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("file".equals(name))
         {
            script.setFile(xmlStreamReader.getAttributeValue(i));
         }

      }
      // consume the end event of file
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         event = xmlStreamReader.next();

      }
      
      return script;
   }
   
   private Dependencies processDependencies(Package pkgMetadata, XMLStreamReader2 xmlStreamReader) throws Exception
   {
      Dependencies depMetadata = new DependenciesImpl(pkgMetadata);
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         switch (event)
         {
            case XMLEvent.START_ELEMENT :
               String childElement = xmlStreamReader.getLocalName();
               if (childElement.equals("unprocessed-dependencies"))
               {
                  UnProcessedDependencies unProcessedDeps = processUnProcessedDependencies(pkgMetadata, xmlStreamReader);
                  depMetadata.setUnProcessedDependencies(unProcessedDeps);
               }
               else if (childElement.equals("packaged-dependency"))
               {
                  PackagedDependency packagedDep = processPackagedDependency(pkgMetadata, xmlStreamReader);
                  depMetadata.addPackagedDependency(packagedDep);
               }
               break;
               
         }
         event = xmlStreamReader.next();
      }
      return depMetadata;
   }
   
   private UnProcessedDependencies processUnProcessedDependencies(Package pkgMeta, XMLStreamReader2 xmlStreamReader) throws Exception
   {
      UnProcessedDependencies unProcessedDep = new UnProcessedDependenciesImpl(pkgMeta);
      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("file".equals(name))
         {
            unProcessedDep.setFile(xmlStreamReader.getAttributeValue(i));
         }
         else if ("manager".equals(name))
         {
            unProcessedDep.setManager(xmlStreamReader.getAttributeValue(i));
         }

      }
      // consume the end event of file
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         event = xmlStreamReader.next();

      }
      return unProcessedDep;
   }
   
   private PackagedDependency processPackagedDependency(Package pkgMeta, XMLStreamReader2 xmlStreamReader) throws Exception
   {
      PackagedDependency packagedDep = new PackagedDependencyImpl(pkgMeta);
      for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
      {
         String name = xmlStreamReader.getAttributeLocalName(i);
         if ("file".equals(name))
         {
            packagedDep.setFile(xmlStreamReader.getAttributeValue(i));
         }
      }
      // consume the end event of file
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         event = xmlStreamReader.next();

      }
      return packagedDep;
   }
}
