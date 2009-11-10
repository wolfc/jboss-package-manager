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
package org.jboss.ejb3.packagemanager.script.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.jboss.ejb3.packagemanager.PackageContext;
import org.jboss.ejb3.packagemanager.PackageManagerContext;
import org.jboss.ejb3.packagemanager.entity.InstalledPackage;
import org.jboss.ejb3.packagemanager.entity.PackageManagerEntity;
import org.jboss.ejb3.packagemanager.exception.ScriptProcessingException;
import org.jboss.ejb3.packagemanager.script.ScriptProcessor;
import org.jboss.logging.Logger;

/**
 * AntScriptProcessor
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class AntScriptProcessor implements ScriptProcessor
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(AntScriptProcessor.class);

   /**
    * @see org.jboss.ejb3.packagemanager.script.ScriptProcessor#processPostInstallScript(org.jboss.ejb3.packagemanager.PackageManagerContext, org.jboss.ejb3.packagemanager.PackageContext, java.io.File)
    */
   @Override
   public void processPostInstallScript(PackageManagerContext pkgManagerCtx, PackageContext pkgCtx, File script)
         throws ScriptProcessingException
   {
      // Set the properties JBOSS_HOME and PM_HOME for the 
      // build scripts to use (if they find it necessary)
      Map<String, String> props = new HashMap<String, String>();
      props.put("JBOSS_HOME", pkgManagerCtx.getJBossServerHome());
      props.put("PM_HOME", pkgManagerCtx.getPackageManagerEnvironment().getPackageManagerHome().getAbsolutePath());
      Project antProject = this.buildProject(script, pkgCtx.getPackageRoot(), props);
      this.runTarget(antProject, "post-install");
      
   }

   /**
    * @see org.jboss.ejb3.packagemanager.script.ScriptProcessor#processPostUnInstallScript(PackageManagerContext, InstalledPackage, File)
    */
   @Override
   public void processPostUnInstallScript(PackageManagerContext pkgManagerCtx, InstalledPackage installedPackage, File script)
         throws ScriptProcessingException
   {
      // Set the properties JBOSS_HOME and PM_HOME for the 
      // build scripts to use (if they find it necessary)
      Map<String, String> props = new HashMap<String, String>();
      props.put("JBOSS_HOME", pkgManagerCtx.getJBossServerHome());
      props.put("PM_HOME", pkgManagerCtx.getPackageManagerEnvironment().getPackageManagerHome().getAbsolutePath());
      
      // TODO: What should basedir point to? Let's right now point it to the folder containing the
      // script file
      Project antProject = this.buildProject(script, script.getParentFile(), props);
      this.runTarget(antProject, "post-uninstall");

   }

   /**
    * @see org.jboss.ejb3.packagemanager.script.ScriptProcessor#processPreInstallScript(org.jboss.ejb3.packagemanager.PackageManagerContext, org.jboss.ejb3.packagemanager.PackageContext, java.io.File)
    */
   @Override
   public void processPreInstallScript(PackageManagerContext pkgManagerCtx, PackageContext pkgCtx, File script)
         throws ScriptProcessingException
   {
      // Set the properties JBOSS_HOME and PM_HOME for the 
      // build scripts to use (if they find it necessary)
      Map<String, String> props = new HashMap<String, String>();
      props.put("JBOSS_HOME", pkgManagerCtx.getJBossServerHome());
      props.put("PM_HOME", pkgManagerCtx.getPackageManagerEnvironment().getPackageManagerHome().getAbsolutePath());
      Project antProject = this.buildProject(script, pkgCtx.getPackageRoot(), props);
      this.runTarget(antProject, "pre-install");

   }

   /**
    * @see org.jboss.ejb3.packagemanager.script.ScriptProcessor#processPreUnInstallScript(PackageManagerContext, InstalledPackage, File)
    */
   @Override
   public void processPreUnInstallScript(PackageManagerContext pkgManagerCtx, InstalledPackage installedPackage, File script)
         throws ScriptProcessingException
   {
      // Set the properties JBOSS_HOME and PM_HOME for the 
      // build scripts to use (if they find it necessary)
      Map<String, String> props = new HashMap<String, String>();
      props.put("JBOSS_HOME", pkgManagerCtx.getJBossServerHome());
      props.put("PM_HOME", pkgManagerCtx.getPackageManagerEnvironment().getPackageManagerHome().getAbsolutePath());
      
      // TODO: What should basedir point to? Let's right now point it to the folder containing the
      // script file
      Project antProject = this.buildProject(script, script.getParentFile(), props);
      this.runTarget(antProject, "pre-uninstall");


   }

   private Project buildProject(File scriptFile, File baseDir, Map<String, String> antProperties) throws ScriptProcessingException
   {
      if (!scriptFile.exists())
      {
         throw new ScriptProcessingException("Ant script file " + scriptFile + " does not exist");
      }
      Project antProject = new Project();
      // add our build listener to capture ant logging and other stuff
      antProject.addBuildListener(new AntBuildListener());
      // Set the basedir for the ant project  
      antProject.setBaseDir(baseDir);
      
      if (antProperties != null)
      {
         Set<Entry<String, String>> entries = antProperties.entrySet();
         for (Entry<String, String> entry : entries)
         {
            String propName = entry.getKey();
            String propVal = entry.getValue();
            antProject.setProperty(propName, propVal);
         }

      }
      //      antProject.setProperty("JBOSS_HOME", pkgManagerCtx.getJBossServerHome());
      //      antProject.setProperty("PM_HOME", pkgManagerCtx.getPackageManagerEnvironment().getPackageManagerHome()
      //            .getAbsolutePath());
      // init the project
      antProject.init();

      ProjectHelper antProjHelper = ProjectHelper.getProjectHelper();
      // parse the project from the build file
      antProjHelper.parse(antProject, scriptFile);

      return antProject;
   }

   private void runTarget(Project antProject, String targetName) throws ScriptProcessingException
   {
      // check whether the target exists in the build file
      if (!antProject.getTargets().containsKey(targetName))
      {
         throw new ScriptProcessingException("Target " + targetName + " not present in Ant script file");
      }
      try
      {
         antProject.executeTarget(targetName);
      }
      catch (Exception e)
      {
         throw new ScriptProcessingException("Exception while running target " + targetName + " in Ant script");
      }

   }

   private class AntBuildListener implements BuildListener
   {
      /**
       * 
       */
      public AntBuildListener()
      {

      }

      /**
       * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
       */
      public void buildFinished(BuildEvent buildEvent)
      {

      }

      /**
       * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
       */
      public void buildStarted(BuildEvent buildEvent)
      {

      }

      /**
       * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
       */
      public void messageLogged(BuildEvent buildEvent)
      {

         int logLevel = buildEvent.getPriority();
         switch (logLevel)
         {
            case Project.MSG_VERBOSE :
               logger.trace(buildEvent.getMessage());
               break;
            case Project.MSG_DEBUG :
               logger.debug(buildEvent.getMessage());
               break;
            case Project.MSG_INFO :
               logger.info(buildEvent.getMessage());
               break;
            case Project.MSG_WARN :
               logger.warn(buildEvent.getMessage());
               break;
            case Project.MSG_ERR :
               logger.error(buildEvent.getMessage());
               break;
            default :
               logger.debug(buildEvent.getMessage());

         }

      }

      /**
       * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
       */
      public void targetFinished(BuildEvent buildEvent)
      {
         Throwable failure = buildEvent.getException();
         if (failure != null)
         {
            logger.error(buildEvent.getTarget() + " failed", failure);
         }
         else
         {
            logger.info(buildEvent.getTarget() + " completed successfully");
         }

      }

      /**
       * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
       */
      public void targetStarted(BuildEvent buildEvent)
      {

      }

      /**
       * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
       */
      public void taskFinished(BuildEvent buildEvent)
      {

      }

      /**
       * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
       */
      public void taskStarted(BuildEvent buildEvent)
      {

      }
   }

}
