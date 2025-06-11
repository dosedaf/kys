package src.model;

public class Category {
    private int id;
    private String name;
    private String description;
    private String type;

    public Category() {
    }

    public Category(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public Category(int id, String name, String description, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getType() {
        return this.type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }
}