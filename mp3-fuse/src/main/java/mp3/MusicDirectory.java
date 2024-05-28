package mp3;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MusicDirectory extends FSElement {

    private final List<FSElement> content = new ArrayList<>();

    public MusicDirectory() { }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    public void getOriginalFileTree(String musicPath) {
        Path startingDir = FileSystems.getDefault().getPath(musicPath);
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(startingDir)) {
            for (Path p : paths) {
                if (Files.isDirectory(p)) {
                    processDirectory(p);
                } else {
                    processFile(p);
                }
            }
        } catch (IOException e) {
            System.out.println("Input/Output exception: " + e.getMessage());
        }
    }

    private void processDirectory(Path path) throws IOException {
        MusicDirectory directory = new MusicDirectory();
        directory.setPath(computeNewPath(path));
        directory.getOriginalFileTree(path.toString());
        content.add(directory);
    }

    private void processFile(Path path) {
        try {
            MusicFile file = new MusicFile();
            file.setOriginalPath(path.toString());
            file.setPath(computeNewPath(path));
            content.add(file);
        } catch (Exception e) {
            System.out.println("File processing exception: " + e.getMessage());
        }
    }

    private String computeNewPath(Path path) {
        return this.path.equals("/") ? "/" + path.getFileName().toString() : this.path + "/" + path.getFileName().toString();
    }

    public List<FSElement> getContent() {
        return content;
    }

    public List<MusicFile> getAllFileContent() {
        List<MusicFile> result = new ArrayList<>();
        for (FSElement e : content) {
            if (e instanceof MusicFile file) {
                result.add(file);
            } else if (e instanceof MusicDirectory directory) {
                result.addAll(directory.getAllFileContent());
            }
        }
        return result;
    }

    @Override
    public FSElement find(String path) {
        if (this.path.equals(path)) {
            return this;
        }
        for (FSElement element : content) {
            FSElement found = element.find(path);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
