package fuse;

import jnr.ffi.Pointer;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import mp3.MusicDirectory;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;
import mp3.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class MusicFuse extends FuseStubFS {

    private static final String ROOT_PATH = "/";
    private final MusicDirectory root;

    public MusicFuse(String path) {
        root = new MusicDirectory();
        root.setPath(ROOT_PATH);
        initializeDirectories(path);
    }

    private void initializeDirectories(String path) {
        MusicDirectory originalDirectory = createDirectory("/original_dir", path);
        root.getContent().add(originalDirectory);

        MusicDirectory customDirectory = createDirectory("/grouped_mp3");
        MusicDirectory artistDirectory = createDirectory("/grouped_mp3/artist");
        MusicDirectory genreDirectory = createDirectory("/grouped_mp3/genre");
        MusicDirectory yearDirectory = createDirectory("/grouped_mp3/year");

        customDirectory.getContent().add(artistDirectory);
        customDirectory.getContent().add(genreDirectory);
        customDirectory.getContent().add(yearDirectory);

        root.getContent().add(customDirectory);
    }

    private MusicDirectory createDirectory(String path) {
        MusicDirectory directory = new MusicDirectory();
        directory.setPath(path);
        return directory;
    }

    private MusicDirectory createDirectory(String path, String originalPath) {
        MusicDirectory directory = createDirectory(path);
        directory.getOriginalFileTree(originalPath);
        return directory;
    }

    private void populateMaps(List<MusicFile> fileList,
                              HashMap<String, MusicDirectory> artistMap,
                              HashMap<String, MusicDirectory> genreMap,
                              HashMap<String, MusicDirectory> yearMap) {
        for (var file : fileList) {
            artistMap.putIfAbsent(file.getArtist(), createDirectory("/grouped_mp3/artist/" + file.getArtist()));
            genreMap.putIfAbsent(file.getGenre(), createDirectory("/grouped_mp3/genre/" + file.getGenre()));
            yearMap.putIfAbsent(file.getYear(), createDirectory("/grouped_mp3/year/" + file.getYear()));
        }
    }

    private void setDirectoryStat(FileStat stat, int permissions) {
        stat.st_mode.set(FileStat.S_IFDIR | permissions);
        stat.st_nlink.set(2);
    }

    private void setFileStat(FileStat stat, MusicFile file) {
        stat.st_mode.set(FileStat.S_IFREG | 0666);
        stat.st_size.set(file.getData().length);
        stat.st_nlink.set(1);
    }

    private void setOwnership(FileStat stat) {
        stat.st_uid.set(getContext().uid.get());
        stat.st_gid.set(getContext().gid.get());
    }

    private void createGroupedDirectories(HashMap<String, MusicDirectory> artistMap,
                                          HashMap<String, MusicDirectory> genreMap,
                                          HashMap<String, MusicDirectory> yearMap) {
        MusicDirectory groupedDirectory = (MusicDirectory) root.getContent().get(1);
        MusicDirectory artistDirectory = (MusicDirectory) groupedDirectory.getContent().get(0);
        MusicDirectory genreDirectory = (MusicDirectory) groupedDirectory.getContent().get(1);
        MusicDirectory yearDirectory = (MusicDirectory) groupedDirectory.getContent().get(2);

        for (MusicDirectory dir : artistMap.values()) {
            artistDirectory.getContent().add(dir);
        }

        for (MusicDirectory dir : genreMap.values()) {
            genreDirectory.getContent().add(dir);
        }

        for (MusicDirectory dir : yearMap.values()) {
            yearDirectory.getContent().add(dir);
        }
    }

    private void addFilesToGroupedDirectories(List<MusicFile> fileList,
                                              HashMap<String, MusicDirectory> artistMap,
                                              HashMap<String, MusicDirectory> genreMap,
                                              HashMap<String, MusicDirectory> yearMap) {
        for (var file : fileList) {
            addFileToDirectory(file, artistMap.get(file.getArtist()));
            addFileToDirectory(file, genreMap.get(file.getGenre()));
            addFileToDirectory(file, yearMap.get(file.getYear()));
        }
    }

    private void addFileToDirectory(MusicFile file, MusicDirectory directory) {
        MusicFile newFile = new MusicFile();
        newFile.setOriginalPath(file.getOriginalPath());
        newFile.setPath(directory.getPath() + "/" + extractFileName(file.getOriginalPath()));
        directory.getContent().add(newFile);
    }

    private String extractFileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public void init() {
        MusicDirectory originalDirectory = (MusicDirectory) root.getContent().get(0);
        var fileList = originalDirectory.getAllFileContent();

        var artistMap = new HashMap<String, MusicDirectory>();
        var genreMap = new HashMap<String, MusicDirectory>();
        var yearMap = new HashMap<String, MusicDirectory>();

        populateMaps(fileList, artistMap, genreMap, yearMap);
        createGroupedDirectories(artistMap, genreMap, yearMap);
        addFilesToGroupedDirectories(fileList, artistMap, genreMap, yearMap);
    }

    @Override
    public int getattr(String path, FileStat stat) {
        System.out.println("getattr called for path: " + path);

        if (path.equals(ROOT_PATH)) {
            setDirectoryStat(stat, 0755);
            return 0;
        }

        FSElement found = root.find(path);

        if (found == null) {
            System.out.println("getattr: No such file or directory: " + path);
            return -ErrorCodes.ENOENT();
        }

        if (found instanceof MusicDirectory) {
            setDirectoryStat(stat, 0777);
        } else if (found instanceof MusicFile file) {
            setFileStat(stat, file);
        } else {
            return -ErrorCodes.ENOENT();
        }

        setOwnership(stat);
        return 0;
    }

    @Override
    public int open(String path, FuseFileInfo fi) {
        System.out.println("open called for path: " + path);

        FSElement element = root.find(path);
        if (!(element instanceof MusicFile)) {
            System.out.println("open: No such file: " + path);
            return -ErrorCodes.ENOENT();
        }

        return 0;
    }

    @Override
    public int read(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        System.out.println("read called for path: " + path + ", size: " + size + ", offset: " + offset);

        FSElement element = root.find(path);
        if (!(element instanceof MusicFile file)) {
            System.out.println("read: No such file: " + path);
            return -ErrorCodes.ENOENT();
        }

        return readFile(buf, size, offset, file.getData());
    }

    private int readFile(Pointer buf, long size, long offset, byte[] data) {
        int dataLength = data.length;

        if (offset >= dataLength) {
            return 0;
        }

        int bytesToRead = (int) Math.min(size, dataLength - offset);
        buf.put(0, data, (int) offset, bytesToRead);

        return bytesToRead;
    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filler, @off_t long offset, FuseFileInfo fi) {
        System.out.println("readdir called for path: " + path);

        FSElement element = root.find(path.equals(ROOT_PATH) ? ROOT_PATH : path);
        if (!(element instanceof MusicDirectory)) {
            System.out.println("readdir: No such directory: " + path);
            return -ErrorCodes.ENOENT();
        }

        fillDirectory(buf, filler, (MusicDirectory) element);
        return 0;
    }

    @Override
    public int readlink(String path, Pointer buf, long bufsize) {
        System.out.println("readlink called for path: " + path);

        FSElement element = root.find(path);
        if (!(element instanceof MusicFile file)) {
            System.out.println("readlink: No such file: " + path);
            return -ErrorCodes.ENOENT();
        }

        String originalPath = file.getOriginalPath();
        byte[] bytes = originalPath.getBytes(StandardCharsets.UTF_8);

        if (bytes.length >= bufsize) {
            return -ErrorCodes.ENAMETOOLONG();
        }

        buf.put(0, bytes, 0, bytes.length);
        buf.putByte(bytes.length, (byte) 0);

        return 0;
    }

    private void fillDirectory(Pointer buf, FuseFillDir filler, MusicDirectory dir) {
        filler.apply(buf, ".", null, 0);
        filler.apply(buf, "..", null, 0);

        for (FSElement child : dir.getContent()) {
            String childName = child.getName();
            System.out.println("Adding child: " + childName);
            filler.apply(buf, childName, null, 0);
        }
    }
}
