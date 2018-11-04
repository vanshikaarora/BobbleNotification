package vanshika.example.com.notofication;

public class UserRule {
    public static final String TABLE_NAME = "userrule";


    public UserRule() {
    }

    public enum TextModifiers {
        equals, notequals, startswith, endswith, contains, notcontains
    }

    public String packagename;
    public String modifier;
    public String text;
    public String alternativeTTS;
    public boolean talk;

    /*flags*/
    public boolean ongoing;
    public boolean showLights;
}