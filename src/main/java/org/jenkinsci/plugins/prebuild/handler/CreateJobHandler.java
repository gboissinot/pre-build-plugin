package org.jenkinsci.plugins.prebuild.handler;

import com.thalesgroup.dtkit.util.converter.ConversionService;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.TopLevelItem;
import org.apache.commons.fileupload.FileItem;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Gregory Boissinot
 */
public class CreateJobHandler {

    /**
     * Map the '/' base request
     */

    /**
     * curl -i -X POST  -F "xslFile=@config.xsl"  http://localhost:8080/plugin/prebuild/createJob/jobs/toto?newJobName=tata
     */
    public void doJobs(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {

        checkPOSTRequest(request, response);

        Hudson hudson = Hudson.getInstance();

        //Retrieve the existing job
        String refJobName = request.getRestOfPath();
        if (refJobName == null) {
            throw new ServletException("A 'refJobName' is required.");
        }
        if (refJobName.startsWith("/")) {
            refJobName = refJobName.substring(1);
        }
        TopLevelItem item = hudson.getItem(refJobName);
        if (item == null) {
            throw new ServletException(String.format("The given job '%s' doesn't exist.", refJobName));
        }
        if (!Project.class.isAssignableFrom(item.getClass())) {
            throw new ServletException(String.format("The given element '%s' is not a project.", refJobName));
        }

        Project project = (Project) item;

        //Retrieve the target job name
        String jobName = request.getParameter("newJobName");
        if (jobName == null) {
            throw new ServletException("A 'newJobName' is required.");
        }

        //XSL
        FileItem xslFile = request.getFileItem("xslFile");

        //-- Conversion to create new JOB XML
        File out = File.createTempFile("createJob", "xml");
        ConversionService conversionService = new ConversionService();
        conversionService.convert(new StreamSource(xslFile.getInputStream()), project.getConfigFile().getFile(), out, null);


        FileInputStream fis = new FileInputStream(out);
        Hudson.getInstance().createProjectFromXML(jobName, fis);
        fis.close();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.getWriter().append("Job created.");

        out.delete();
    }

    private void checkPOSTRequest(StaplerRequest request, StaplerResponse response) throws ServletException {
        if (!"POST".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new ServletException("Must be POST, Can't be " + request.getMethod());
        }
    }

}

