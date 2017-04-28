package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import dk.aau.cs.giraf.models.core.User;

/**
 * This interface allows fragments to get the currently selected profile.
 * It is implemented by AppContainerFragment
 */
public interface AppsFragmentInterface {
    public User getCurrentUser();

    public User getLoggedInGuardian();
}
