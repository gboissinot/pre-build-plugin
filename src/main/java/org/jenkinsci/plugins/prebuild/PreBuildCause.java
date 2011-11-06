package org.jenkinsci.plugins.prebuild;

import hudson.model.Cause;

/**
 * @author Gregory Boissinot
 */
public class PreBuildCause extends Cause {

    @Override
    public String getShortDescription() {
        return "[Pre Build]";
    }
}
