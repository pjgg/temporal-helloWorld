package org.pjgg.temporal.examples.helloWorld;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import io.temporal.activity.Activity;

public class FormatActivityImpl implements FormatActivity {

    @Override
    public String composeGreeting(String name) {
        try {
            return "Hello " + name.trim() + "!";
        } catch (Exception e) {
            throw Activity.wrap(e);
        }
    }

    @Singleton
    @Produces
    @Named("format-activity")
    FormatActivity getActivity() {
        return new FormatActivityImpl();
    }
}
