package org.jenkinsci.plugins.prebuild.handler;

import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.TopLevelItem;
import org.jenkinsci.plugins.prebuild.PreBuildCause;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Gregory Boissinot
 */
public class BuildHandler {

    public void doJobs(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
        //Retrieve the existing job
        String refJobName = request.getRestOfPath();
        if (refJobName == null) {
            throw new ServletException("A 'refJobName' is required.");
        }
        if (refJobName.startsWith("/")) {
            refJobName = refJobName.substring(1);
        }

        TopLevelItem item = Hudson.getInstance().getItem(refJobName);
        if (item == null) {
            throw new ServletException(String.format("The given job '%s' doesn't exist.", refJobName));
        }
        if (!Project.class.isAssignableFrom(item.getClass())) {
            throw new ServletException(String.format("The given element '%s' is not a project.", refJobName));
        }

        Project project = (Project) item;

        project.scheduleBuild(0, new PreBuildCause());
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
