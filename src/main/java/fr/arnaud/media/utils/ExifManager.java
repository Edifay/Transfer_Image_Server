package fr.arnaud.media.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import fr.arnaud.media.ImageDescriptor;
import fr.arnaud.media.MediaManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class ExifManager {

    public static void manageExif(final MediaManager mediaManager, final ImageDescriptor descriptor) {
        try {
            editDates(mediaManager.getPathOf(descriptor), descriptor.createdAt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void editDates(final String path, final long epoch) throws IOException {
        BasicFileAttributeView attributes = Files.getFileAttributeView(Path.of(path), BasicFileAttributeView.class);
        FileTime time = null;
        Metadata metadata = null;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path);
            metadata = ImageMetadataReader.readMetadata(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try { // GET DATETAKEN FOR IMAGE
            if (metadata != null) {
                ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                if (directory != null) {
                    Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    time = FileTime.fromMillis(date.getTime());
                }
            }
        } catch (Exception ignored) {
        }

        if (time == null && metadata != null) // GET MEDIACREATION DATE FOR VIDEO
            try {
                Mp4Directory directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
                if (directory != null) {
                    Date date = directory.getDate(Mp4Directory.TAG_CREATION_TIME);
                    time = FileTime.fromMillis(date.getTime());
                }
            } catch (Exception ignored) {
            }

        if (time == null || time.toMillis() <= 0L)// IF NO ONE WAS FIND USE THE DEFAULT TIME GIVEN BY THE PHONE
            time = FileTime.fromMillis(epoch);

        if (epoch != 0L)
            attributes.setTimes(time, time, time);

        if (stream != null)
            stream.close();
    }

}
