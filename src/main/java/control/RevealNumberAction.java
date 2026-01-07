package control;

public class RevealNumberAction extends CellActionTemplate {

    public RevealNumberAction(MultiPlayerGameController game) {
        super(game);
    }

    @Override
    protected MultiPlayerGameController.CellActionResult doAction() {
        return game.revealNumberCell();
    }
}
