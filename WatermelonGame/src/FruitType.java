// FruitType.java
public enum FruitType {
    CHERRY("base/00_cherry", 33/2),
    STRAWBERRY("base/01_strawberry", 48/2),
    GRAPE("base/02_grape", 61/2),
    GYOOL("base/03_gyool", 69/2),
    ORANGE("base/04_orange", 89/2),
    APPLE("base/05_apple", 114/2),
    PEAR("base/06_pear", 129/2),
    PEACH("base/07_peach", 156/2),
    PINEAPPLE("base/08_pineapple", 177/2),
    MELON("base/09_melon", 220/2),
    WATERMELON("base/10_watermelon", 259/2);

    private final String name;
    private final float radius;
    private final Fruit fruit;

    FruitType(String name, float radius) {
        this.name = name;
        this.radius = radius;
        this.fruit = new Fruit(name, radius, "/resources/" + name + ".png");
    }

    public Fruit getFruit() { return fruit; }
    public static Fruit getRandomFruit() {
        FruitType[] fruits = FruitType.values();
        int index = (int)(Math.random() * 5); // 처음 5개 과일 중에서만 랜덤
        return fruits[index].getFruit();
    }
}