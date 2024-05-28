package mp3;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MusicFile extends FSElement{

    private ID3v2 file;
    private String originalPath;

    public MusicFile() { }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    public String getArtist() {
        return file.getArtist();
    }

    public String getGenre() {
        return file.getGenreDescription();
    }

    public String getYear() {
        return file.getYear();
    }

    public void setOriginalPath(String original) {
        originalPath = original;
        try {
            file = new Mp3File(originalPath).getId3v2Tag();
        } catch (Exception e) {
            System.out.println("Error in parsing tags of" + originalPath);
        }
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public byte[] getData() {
        try {
            return Files.readAllBytes(Paths.get(originalPath));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read file: " + originalPath, e);
        }
    }

    @Override
    public FSElement find(String path) {
        if (path.equals(this.path)) {
            return this;
        }
        return null;
    }

}
