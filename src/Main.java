public class Main {
    public static void main(String[] args) {
        Renderer ui= new Renderer(new Game());
        Game.setRenderer(ui);
    }
}