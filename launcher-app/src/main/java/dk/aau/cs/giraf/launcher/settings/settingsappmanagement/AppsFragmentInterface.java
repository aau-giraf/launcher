package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import dk.aau.cs.giraf.dblib.models.Profile;

/**
 * This interface allows fragments to get the currently selected profile.
 * It is implemented by AppContainerFragment
 */
public interface AppsFragmentInterface {
    public Profile getCurrentUser();
    public Profile getLoggedInGuardian();
}
