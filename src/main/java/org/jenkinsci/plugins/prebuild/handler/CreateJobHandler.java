package org.jenkinsci.plugins.prebuild.handler;

import hudson.model.TopLevelItem;
import hudson.model.Hudson;
import hudson.model.Project;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;

import org.jenkinsci.plugins.prebuild.PreBuildPlugin.BadRequestException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.thalesgroup.dtkit.util.converter.ConversionService;

/**
 * @author Gregory Boissinot
 */
public class CreateJobHandler {

    /**
     * curl -i -X POST -F "xslFile=@config.xsl"
     * http://localhost:8080/plugin/prebuild/createJob/jobs/toto?newJobName=tata
     */
    public void doJobs(StaplerRequest request, StaplerResponse response) throws IOException,
            ServletException
    {

        try {
            if (!"POST".equals(request.getMethod())) {
                throw new BadRequestException("Must be POST, Can't be " + request.getMethod());
            }

            Hudson hudson = Hudson.getInstance();

            // Retrieve the existing job
            String refJobName = request.getRestOfPath();
            if (refJobName == null) {
                throw new BadRequestException("Missing reference 'jobName' in url.");
            }
            if (refJobName.startsWith("/")) {
                refJobName = refJobName.substring(1);
            }
            TopLevelItem item = hudson.getItem(refJobName);
            if (item == null) {
                throw new BadRequestException(String.format("The job '%s' does not exist.",
                        refJobName));
            }
            if (!Project.class.isAssignableFrom(item.getClass())) {
                throw new BadRequestException(String.format("The element '%s' is not a project.",
                        refJobName));
            }

            Project<?, ?> project = (Project<?, ?>) item;

            // Retrieve the target job name
            String jobName = request.getParameter("newJobName");
            if (jobName == null) {
                throw new BadRequestException("Missing 'newJobName' parameter.");
            }

            // XSL
            String xslSheet = request.getParameter("xslSheet");
            if (xslSheet == null) {
                throw new BadRequestException("Missing 'xslSheet' parameter.");
            }

            // -- Conversion to create new JOB XML
            ConversionService conversionService = new ConversionService();
            ByteArrayInputStream bis = new ByteArrayInputStream(xslSheet.getBytes("UTF-8"));
            String xml = conversionService.convertAndReturn(new StreamSource(bis), project
                    .getConfigFile().getFile(), null);
            
            if (projectExists(jobName)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                response.setContentType("text/plain");
                response.getWriter().append("Job '" + jobName + "' already exists.");
            } else {
                ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
                Hudson.getInstance().createProjectFromXML(jobName, in);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setContentType("text/plain");
                response.getWriter().append("Job '" + jobName + "' created.");
            }
        } catch (BadRequestException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().append(e.toString());
        }
    }

    private boolean projectExists(String jobName) {
        for (Project<?, ?> p : Hudson.getInstance().getProjects()) {
            if (jobName.equals(p.getName())) {
                return true;
            }
        }
        return false;
    }

}
