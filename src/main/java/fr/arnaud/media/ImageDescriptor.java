package fr.arnaud.media;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class ImageDescriptor {

    public static final ImageDescriptor EMPTY = new ImageDescriptor("-1", -1, -1);

    public String path;
    public long size;
    public long createdAt = 0;

    public ImageDescriptor() {

    }

    public ImageDescriptor(final String path, final long size, final long createdAt) {
        this.path = path;
        this.size = size;
        this.createdAt = createdAt;
    }

    @JsonIgnore
    public String getName() {
        return new File(path).getName();
    }


}
