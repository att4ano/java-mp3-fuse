package mp3;

public abstract class FSElement {

    protected String path;

    protected FSElement() { }

    protected FSElement(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public abstract void setPath(String path);

    public String getName() {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public abstract FSElement find(String path);
}
