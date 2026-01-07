package control;

public abstract class CellActionTemplate {

    protected final MultiPlayerGameController game;

    protected CellActionTemplate(MultiPlayerGameController game) {
        this.game = game;
    }

    // TEMPLATE METHOD: שלד קבוע
    public final MultiPlayerGameController.CellActionResult execute() {
        if (!preconditions()) {
            return new MultiPlayerGameController.CellActionResult(false, 0, 0, "Action not allowed.");
        }

        before();
        MultiPlayerGameController.CellActionResult res = doAction(); // משתנה
        after(res);

        return res;
    }

    protected boolean preconditions() { return !game.isGameOver(); }
    protected void before() {}
    protected abstract MultiPlayerGameController.CellActionResult doAction();
    protected void after(MultiPlayerGameController.CellActionResult res) {}
}