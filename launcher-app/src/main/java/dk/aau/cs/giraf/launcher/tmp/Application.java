package dk.aau.cs.giraf.launcher.tmp;

public class Application implements Comparable{
    private long id;
    private String name;
    private String version;
    private String pack;
    private String activity;
    private String description;
    private long author;
    private final int DB_STRLEN_CONSTRAINT = 1024; // TODO move to a common place?
    /**
     * Constructor with arguments
     * @param name The name of the application
     * @param version The version of the application
     * @param pack The package of the application
     * @param activity
     * @param description The description of the application
     * @param author
     */
    public Application(String name, String version, String pack, String activity, String description, long author) {
        this.name = name;
        this.version = version;
        this.pack = pack;
        this.activity = activity;
        this.author = author;
        if(description != null && description.length() > DB_STRLEN_CONSTRAINT)
            description = description.substring(0, DB_STRLEN_CONSTRAINT);
        this.description = description;
    }
    public Application(String name, String version, String pack, String activity, String description) {
        this(name,version, pack,activity,description,0);
    }
    /**
     * Empty constructor
     */
    public Application() {
    }
    /**
     * @return the id
     */
    public long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }
    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
    /**
     * @return the package
     */
    public String getPackage() {
        return pack;
    }
    /**
     * @param pack
     */
    public void setPackage(String pack) {
        this.pack = pack;
    }
    /**
     * @return the activity
     */
    public String getActivity() {
        return activity;
    }
    /**
     * @param activity
     */
    public void setActivity(String activity) {
        this.activity = activity;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description
     */
    public void setDescription(String description) {
        if(description == null) return;
        if(description.length() > DB_STRLEN_CONSTRAINT)
            description = description.substring(0, DB_STRLEN_CONSTRAINT);
        this.description = description;
    }
    /**
     * @return the author
     */
    public long getAuthor() {
        return author;
    }
    /**
     * @param author
     */
    public void setAuthor(long author) {
        this.author = author;
    }
    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%s,%d", getId(), getName(), getVersion(), getPackage(), getActivity(), getDescription(), getAuthor());
    }
    @Override public boolean equals(Object aApplication) {
        if ( this == aApplication ) return true;
        if ( !(aApplication instanceof Application) ) return false;
        Application application = (Application)aApplication;
        return
                this.getId() == application.getId();
    }
    public int compareTo(Object o1) {
        if (o1 instanceof  Application){
            Application app = (Application)o1;
            return app.name.compareTo(this.name);
        }
        return -1;
    }
}
