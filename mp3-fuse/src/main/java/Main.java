import fuse.MusicFuse;
import jnr.ffi.Platform;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {

        Properties properties = new Properties();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("config.properties"), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        MusicFuse musicFuse = new MusicFuse(properties.getProperty("ORIGINAL_DIR"));
        musicFuse.init();
        try {
            String path;
            switch (Platform.getNativePlatform().getOS()) {
                case WINDOWS:
                    path = "J:\\";
                    break;
                default:
                    path = properties.getProperty("MOUNT_DIR");
            }
            musicFuse.mount(Paths.get(path), true, true);
        } finally {
            musicFuse.umount();
        }
    }
}
