public enum Difficulty {
    EASY(15, "Easy", 1.0f), 
    MEDIUM(25, "Medium", 1.2f), 
    HARD(40, "Hard", 1.5f);
    
    public final int length;
    public final String name;
    public final float multiplier;
    
    Difficulty(int length, String name, float multiplier) {
        this.length = length;
        this.name = name;
        this.multiplier = multiplier;
    }
}
