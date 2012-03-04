package org.jenkinsci.plugins.prebuild;


import hudson.Extension;
import hudson.Plugin;
import org.jenkinsci.plugins.prebuild.handler.BuildHandler;
import org.jenkinsci.plugins.prebuild.handler.CreateJobHandler;

/**
 * @author Gregory Boissinot
 */
@Extension
public class PreBuildPlugin extends Plugin {

    public CreateJobHandler getCreateJob() {
        return new CreateJobHandler();
    }

    public BuildHandler getBuild() {
        return new BuildHandler();
    }
    
    @SuppressWarnings("serial")
    public static class BadRequestException extends Exception {

        public BadRequestException(String message) {
            super(message);
        }
        
    }
}
