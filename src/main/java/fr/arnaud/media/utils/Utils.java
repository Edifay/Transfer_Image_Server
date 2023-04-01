package fr.arnaud.media.utils;

import fr.arnaud.media.ImageDescriptor;

import java.io.File;
import java.util.List;

public class Utils {

    public static boolean isFileIn(final File file, final List<ImageDescriptor> imageDescriptors) {
        final String name = file.getName();
        final int size = (int) file.length();

        for (ImageDescriptor descriptor : imageDescriptors)
            if (descriptor.getName().equals(name) && size == descriptor.size)
                return true;

        return false;
    }
}
