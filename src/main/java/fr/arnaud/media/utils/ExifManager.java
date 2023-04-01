package fr.arnaud.media.utils;

import fr.arnaud.media.ImageDescriptor;
import fr.arnaud.media.MediaManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;

public class ExifManager {

    public static void manageExif(final MediaManager mediaManager, final ImageDescriptor descriptor) {
        try {
            if (descriptor.createdAt != 0)
                editDates(mediaManager.getPathOf(descriptor), descriptor.createdAt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void editDates(final String path, final long epoch) throws IOException {
        BasicFileAttributeView attributes = Files.getFileAttributeView(Path.of(path), BasicFileAttributeView.class);
        FileTime time = FileTime.fromMillis(epoch);
        attributes.setTimes(time, time, time);
    }

}
