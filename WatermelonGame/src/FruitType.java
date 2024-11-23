public enum FruitType {
    CHERRY("base/00_cherry", 30 / 2, 33, 46),
    STRAWBERRY("base/01_strawberry", 40 / 2, 43, 48),
    GRAPE("base/02_grape", 60 / 2, 61, 61),
    GYOOL("base/03_gyool", 70 / 2, 69, 76),
    ORANGE("base/04_orange", 90 / 2, 89, 95),
    APPLE("base/05_apple", 110 / 2, 114, 117),
    PEAR("base/06_pear", 130 / 2, 129, 137),
    PEACH("base/07_peach", 160 / 2, 161, 156),
    PINEAPPLE("base/08_pineapple", 180 / 2, 177, 204),
    MELON("base/09_melon", 220 / 2, 220, 220),
    WATERMELON("base/10_watermelon", 260 / 2, 259, 259);

    private final Fruit fruit;

    FruitType(String name, float radius, int picWidth, int picHeight) {
        this.fruit = new Fruit(name, radius, "/resources/" + name + ".png", picWidth, picHeight);
    }

    public Fruit getFruit() { return fruit; }

    public static Fruit getRandomFruit() {
        FruitType[] fruits = FruitType.values();
        int index = (int)(Math.random() * 5); // 처음 5개 과일 중에서만 랜덤
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