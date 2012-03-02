package org.jenkinsci.plugins.prebuild.handler;

import hudson.model.TopLevelItem;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.Run;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.jenkinsci.plugins.prebuild.PreBuildCause;
import org.jenkinsci.plugins.prebuild.PreBuildPlugin.BadRequestException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @author Gregory Boissinot
 */
public class BuildHandler {

    public void doJobs(StaplerRequest request, StaplerResponse response) throws IOException,
            ServletException
    {
        try {
            // Retrieve the existing job
            String refJobName = request.getRestOfPath();
            if (refJobName == null) {
                throw new BadRequestException("Missing reference 'jobName' in url.");
            }
            if (refJobName.startsWith("/")) {
                refJobName = refJobName.substring(1);
            }

            TopLevelItem item = Hudson.getInstance().getItem(refJobName);
            if (item == null) {
                throw new BadRequestException(String.format("The job '%s' doesn't exist.",
                        refJobName));
            }
            if (!Project.class.isAssignableFrom(item.getClass())) {
                throw new BadRequestException(String.format("The element '%s' is not a project.",
                        refJobName));
            }

            Project<?, ?> project = (Project<?, ?>) item;

            boolean scheduled = project.scheduleBuild(0, new PreBuildCause());

            if (scheduled) {
                Run<?, ?> build = project.getLastBuild();
                int checks = 0;
                while (build == null && checks < 5) {
                    Thread.sleep(500);
                    build = project.getLastBuild();
                    checks++;
                }
                response.setContentType("text/plain");
                response.getWriter().append(build.getUrl());
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }

        } catch (BadRequestException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().append(e.toString());
        } catch (InterruptedException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(response.getWriter());
        }
    }
}
