class Entry {
    private int id;
    private String name;
    private String description;

    public Entry(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }


    /**
     * @return int return the id
     */
    public int getId() {
        return id;
    }


    /**
     * @return String return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return String return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString () {
        return this.name + ": " + this.description;
    }

}