package io.github.jameswolfeoliver.library.Managers;

public class PermissionCallbacks {
    public interface Callbacks {
        void onPermissionDenied(String[] permissions);
        void onPermissionAlwaysDenied(String[] permissions);
        void onPermissionGranted(String[] permissions);
    }
}



