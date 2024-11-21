// FruitType.java
public enum FruitType {
    CHERRY("base/00_cherry", 33 / 2),
    STRAWBERRY("base/01_strawberry", 48 / 2),
    GRAPE("base/02_grape", 61 / 2),
    GYOOL("base/03_gyool", 69 / 2),
    ORANGE("base/04_orange", 89 / 2),
    APPLE("base/05_apple", 114 / 2),
    PEAR("base/06_pear", 129 / 2),
    PEACH("base/07_peach", 156 / 2),
    PINEAPPLE("base/08_pineapple", 177 / 2),
    MELON("base/09_melon", 220 / 2),
    WATERMELON("base/10_watermelon", 260 / 2);

    private final Fruit fruit;

    FruitType(String name, float radius) {
        this.fruit = new Fruit(name, radius, "/resources/" + name + ".png");
    }

    public Fruit getFruit() { return fruit; }

    public static Fruit getRandomFruit() {
        FruitType[] fruits = FruitType.values();
        int index = 10;//(int)(Math.random() * 5); // 처음 5개 과일 중에서만 랜덤
        return fruits[index].getFruit();
    }

    public static Fruit getNextFruit(Fruit currentFruit) {
        switch (currentFruit.getName()) {
            case "base/00_cherry":
                return STRAWBERRY.getFruit();
            case "base/01_strawberry":
                return GRAPE.getFruit();
            case "base/02_grape":
                return GYOOL.getFruit();
            case "base/03_gyool":
                return ORANGE.getFruit();
            case "base/04_orange":
                return APPLE.getFruit();
            case "base/05_apple":
                return PEAR.getFruit();
            case "base/06_pear":
                return PEACH.getFruit();
            case "base/07_peach":
                return PINEAPPLE.getFruit();
            case "base/08_pineapple":
                return MELON.getFruit();
            case "base/09_melon":
                return WATERMELON.getFruit();
            case "base/10_watermelon":
                return null; 
            default:
                return null;
        }
    }
}