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
import org.jboss.ejb3.packagemanager.metadata.DependenciesType;
import org.jboss.ejb3.packagemanager.metadata.InstallFileType;
import org.jboss.ejb3.packagemanager.metadata.PackageType;
import org.jboss.ejb3.packagemanager.metadata.PackagedDependency;
import org.jboss.ejb3.packagemanager.metadata.PostInstallType;
import org.jboss.ejb3.packagemanager.metadata.PreInstallType;
import org.jboss.ejb3.packagemanager.metadata.ScriptType;
import org.jboss.ejb3.packagemanager.metadata.SystemRequirementsType;
import org.jboss.ejb3.packagemanager.metadata.UnProcessedDependenciesType;
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
    * Creates {@link PackageType} out of the URL pointing to a package.xml file
    * 
    * @param packageXml package.xml URL 
    * @return Returns the {@link PackageType} corresponding to the package.xml
    * @throws Exception If any exceptions occur during processing the package.xml
    */
   public PackageType unmarshal(URL packageXml) throws Exception
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
      PackageType pkgMetadata = null;
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

   private PackageType processPackage(XMLStreamReader2 xmlStreamReader) throws Exception
   {
      PackageType pkgMeta = new PackageImpl();
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
                  SystemRequirementsType sysReqs = processSystemRequirements(pkgMeta, xmlStreamReader);
                  pkgMeta.setSystemRequirements(sysReqs);
               }
               else if (childElement.equals("file"))
               {
                  InstallFileType file = processFiles(pkgMeta, xmlStreamReader);
                  pkgMeta.addFile(file);
               }
               else if (childElement.equals("pre-install"))
               {
                  PreInstallType preInstall = processPreInstall(pkgMeta, xmlStreamReader);
                  pkgMeta.setPreInstall(preInstall);
               }
               else if (childElement.equals("post-install"))
               {
                  PostInstallType postInstall = processPostInstall(pkgMeta, xmlStreamReader);
                  pkgMeta.setPostInstall(postInstall);
               }
               else if (childElement.equals("dependencies"))
               {
                  DependenciesType dependencies = this.processDependencies(pkgMeta, xmlStreamReader);
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
   private SystemRequirementsType processSystemRequirements(PackageType pkgMeta,
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
   private InstallFileType processFiles(PackageType pkgMeta, XMLStreamReader2 xmlStreamReader) throws Exception
   {
      InstallFileType fileMeta = new InstallFileImpl(pkgMeta);
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
   private PreInstallType processPreInstall(PackageType pkgMeta, XMLStreamReader2 xmlStreamReader)
         throws Exception
   {
      PreInstallType preInstall = new PreInstallImpl(pkgMeta);
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         switch (event)
         {
            case XMLEvent.START_ELEMENT :
               String childElement = xmlStreamReader.getLocalName();
               if (childElement.equals("script"))
               {
                  ScriptType script = processPreInstallScript(preInstall, xmlStreamReader);
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
   private PostInstallType processPostInstall(PackageType pkgMeta, XMLStreamReader2 xmlStreamReader)
         throws Exception
   {
      PostInstallType postInstall = new PostInstallImpl(pkgMeta);
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         switch (event)
         {
            case XMLEvent.START_ELEMENT :
               String childElement = xmlStreamReader.getLocalName();
               if (childElement.equals("script"))
               {
                  ScriptType script = processPostInstallScript(postInstall, xmlStreamReader);
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
   private PreInstallScript processPreInstallScript(PreInstallType preInstallMeta, XMLStreamReader2 xmlStreamReader)
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
   private PostInstallScript processPostInstallScript(PostInstallType postInstallMeta,
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
   private ScriptType processScript(ScriptType script, XMLStreamReader2 xmlStreamReader) throws Exception
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
   
   private DependenciesType processDependencies(PackageType pkgMetadata, XMLStreamReader2 xmlStreamReader) throws Exception
   {
      DependenciesType depMetadata = new DependenciesImpl(pkgMetadata);
      int event = xmlStreamReader.next();
      while (event != XMLEvent.END_ELEMENT)
      {
         switch (event)
         {
            case XMLEvent.START_ELEMENT :
               String childElement = xmlStreamReader.getLocalName();
               if (childElement.equals("unprocessed-dependencies"))
               {
                  UnProcessedDependenciesType unProcessedDeps = processUnProcessedDependencies(pkgMetadata, xmlStreamReader);
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
   
   private UnProcessedDependenciesType processUnProcessedDependencies(PackageType pkgMeta, XMLStreamReader2 xmlStreamReader) throws Exception
   {
      UnProcessedDependenciesType unProcessedDep = new UnProcessedDependenciesImpl(pkgMeta);
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
   
   private PackagedDependency processPackagedDependency(PackageType pkgMeta, XMLStreamReader2 xmlStreamReader) throws Exception
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
