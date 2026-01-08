package control;

public class RevealMineAction extends CellActionTemplate {

    public RevealMineAction(MultiPlayerGameController game) {
        super(game);
    }

    @Override
    protected MultiPlayerGameController.CellActionResult doAction() {
        return game.revealMine();
    }
}