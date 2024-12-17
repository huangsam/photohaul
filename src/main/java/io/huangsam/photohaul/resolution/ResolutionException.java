package io.huangsam.photohaul.resolution;

/**
 * This resembles an issue with methods called in {@link PhotoResolver}.
 */
public class ResolutionException extends RuntimeException {
    public ResolutionException(String message) {
        super(message);
    }
}
