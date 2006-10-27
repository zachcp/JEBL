/*	OSXAdapter.java */

package org.virion.jam.maconly;

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

import java.io.File;

public class OSXAdapter extends ApplicationAdapter {

    // pseudo-singleton model; no point in making multiple instances
    // of the EAWT application or our adapter
    private static OSXAdapter theAdapter;
    private static com.apple.eawt.Application theApplication;

    // reference to the app where the existing quit, about, prefs code is
    private org.virion.jam.framework.Application application;

    private OSXAdapter(org.virion.jam.framework.Application application) {
        this.application = application;
    }

    // implemented handler methods.  These are basically hooks into existing
    // functionality from the main app, as if it came over from another platform.
    public void handleAbout(ApplicationEvent ae) {
        if (application != null) {
            ae.setHandled(true);
            application.doAbout();
        } else {
            throw new IllegalStateException("handleAbout: MyApp instance detached from listener");
        }
    }

    public void handlePreferences(ApplicationEvent ae) {
        if (application != null) {
            application.doPreferences();
            ae.setHandled(true);
        } else {
            throw new IllegalStateException("handlePreferences: MyApp instance detached from listener");
        }
    }

    public void handleQuit(ApplicationEvent ae) {
        if (application != null) {
            /*
            /	You MUST setHandled(false) if you want to delay or cancel the quit.
            /	This is important for cross-platform development -- have a universal quit
            /	routine that chooses whether or not to quit, so the functionality is identical
            /	on all platforms.  This example simply cancels the AppleEvent-based quit and
            /	defers to that universal method.
            */
            ae.setHandled(false);
            application.doQuit();
        } else {
            throw new IllegalStateException("handleQuit: MyApp instance detached from listener");
        }
    }


    // The main entry-point for this functionality.  This is the only method
    // that needs to be called at runtime, and it can easily be done using
    // reflection.
    public static void registerMacOSXApplication(org.virion.jam.framework.Application application) {
        if (theApplication == null) {
            theApplication = new com.apple.eawt.Application();
        }

        if (theAdapter == null) {
            theAdapter = new OSXAdapter(application);
        }
        theApplication.addApplicationListener(theAdapter);
    }

    // Another static entry point for EAWT functionality.  Enables the
    // "Preferences..." menu item in the application menu.
    public static void enablePrefs(boolean enabled) {
        if (theApplication == null) {
            theApplication = new com.apple.eawt.Application();
        }
        theApplication.setEnabledPreferencesMenu(enabled);
    }

	public void handleOpenApplication(ApplicationEvent ae) {
		if (application != null) {
			System.err.println("handleOpenApplication: " + ae.getFilename());
		    application.doOpenFile(new File(ae.getFilename()));
		    ae.setHandled(true);
		} else {
		    throw new IllegalStateException("handleOpenFile: MyApp instance detached from listener");
		}
	}

	public void handleReOpenApplication(ApplicationEvent ae) {
		if (application != null) {
			System.err.println("handleReOpenApplication: " + ae.getFilename());
		    application.doOpenFile(new File(ae.getFilename()));
		    ae.setHandled(true);
		} else {
		    throw new IllegalStateException("handleOpenFile: MyApp instance detached from listener");
		}
	}

	public void handleOpenFile(ApplicationEvent ae) {
        if (application != null) {
	        System.err.println("handleOpenFile: " + ae.getFilename());

            application.doOpenFile(new File(ae.getFilename()));
            ae.setHandled(true);
        } else {
            throw new IllegalStateException("handleOpenFile: MyApp instance detached from listener");
        }
    }
}