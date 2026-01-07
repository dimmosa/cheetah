package control;

public class RevealEmptyAction extends CellActionTemplate {

    public RevealEmptyAction(MultiPlayerGameController game) {
        super(game);
    }

    @Override
    protected MultiPlayerGameController.CellActionResult doAction() {
        return game.revealEmptyCell();
    }
}
