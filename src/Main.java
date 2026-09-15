import service.AppContext;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        AppContext ctx = new AppContext();
        ConsoleUI ui = new ConsoleUI(ctx);
        ui.run();
    }
}