package dk.aau.cs.giraf.launcher.settings.settingsappmanagement;

import dk.aau.cs.giraf.oasis.lib.models.Profile;

/**
 * This interface allows fragments to get the currently selected profile.
 * It is implemented by GirafFragment and AndroidFragment
 */
public interface AppsFragmentInterface {
    public Profile getSelectedProfile();
}
