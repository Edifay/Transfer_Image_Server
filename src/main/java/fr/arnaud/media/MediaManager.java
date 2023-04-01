package fr.arnaud.media;

import java.io.*;
import java.util.ArrayList;

public class MediaManager {

    private final String destination;

    public MediaManager(final String destination) {
        this.destination = destination;
    }

    public File[] getChildsFiles() {
        File[] files = new File(destination).listFiles();
        return files == null ? new File[0] : files;
    }

    public OutputStream getOutputStream(final ImageDescriptor descriptor) throws FileNotFoundException {
        File file = new File(destination + descriptor.getName());
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    public ArrayList<ImageDescriptor> getMissingImagesFrom(final ArrayList<ImageDescriptor> given) {
        final ArrayList<ImageDescriptor> missing = new ArrayList<>();

        for (ImageDescriptor descriptor : given) {
            final File localFile = new File(destination + descriptor.getName());

            if (!localFile.exists() || localFile.length() != descriptor.size)
                missing.add(descriptor);
        }

        return missing;
    }

    public String getPathOf(final ImageDescriptor descriptor) {
        return destination + descriptor.getName();
    }


}
