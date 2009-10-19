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

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.jboss.ejb3.packagemanager.PackageManager;
import org.jboss.ejb3.packagemanager.PackageSource;
import org.jboss.ejb3.packagemanager.exception.ScriptProcessingException;
import org.jboss.ejb3.packagemanager.metadata.PackageInstallationPhase;
import org.jboss.ejb3.packagemanager.metadata.Script;
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
    * @see org.jboss.ejb3.packagemanager.script.ScriptProcessor#processScript(org.jboss.ejb3.packagemanager.PackageSource, org.jboss.ejb3.packagemanager.metadata.Script)
    */
   public void processScript(PackageManager pkgManager, PackageSource pkgSource, Script script)
         throws ScriptProcessingException
   {
      File root = pkgSource.getSource();
      File antBuildFile = new File(root, script.getFile());
      if (!antBuildFile.exists())
      {
         throw new ScriptProcessingException("Ant script " + script.getFile() + " does not exist in " + pkgSource);
      }
      Project antProject = new Project();
      // add our build listener to capture ant logging and other stuff
      antProject.addBuildListener(new AntBuildListener());
      // Set the basedir for the ant project to point to the 
      // root of the package.xml file of the package being installed 
      antProject.setBaseDir(pkgSource.getSource());
      // Also set the properties JBOSS_HOME and PM_HOME for the 
      // build scripts to use (if they find it necessary)
      antProject.setProperty("JBOSS_HOME", pkgManager.getServerHome());
      antProject.setProperty("PM_HOME",pkgManager.getPackageManagerEnvironment().getPackageManagerHome().getAbsolutePath());
      // init the project
      antProject.init();

      ProjectHelper antProjHelper = ProjectHelper.getProjectHelper();
      // parse the project from the build file
      antProjHelper.parse(antProject, antBuildFile);

      // now run the appropriate target
      String targetName = null;
      PackageInstallationPhase phase = script.getScriptExecutionPhase();
      if (phase == PackageInstallationPhase.PRE_INSTALL)
      {
         targetName = "pre-install";
      }
      else if (phase == PackageInstallationPhase.POST_INSTALL)
      {
         targetName = "post-install";
      }
      else
      {
         throw new ScriptProcessingException(
               "Ant script processor is only capable of running pre-install or post-install scripts. It cannot handle "
                     + phase + " for script " + script);
      }
      // check whether the target exists in the build file
      if (!antProject.getTargets().containsKey(targetName))
      {
         throw new ScriptProcessingException("Target " + targetName + " not present in Ant script " + antBuildFile
               + " for " + pkgSource);
      }
      logger.info("Running pre-install script " + antBuildFile + " ,target= " + targetName + " for " + pkgSource);
      try
      {
         antProject.executeTarget(targetName);
      }
      catch (Exception e)
      {
         throw new ScriptProcessingException("Exception while running target " + targetName + " in script "
               + antBuildFile + " for " + pkgSource);
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
