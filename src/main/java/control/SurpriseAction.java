package control;

public class SurpriseAction extends CellActionTemplate {

    public SurpriseAction(MultiPlayerGameController game) {
        super(game);
    }

    @Override
    protected boolean preconditions() {
        return !game.isGameOver() && game.getSharedScore() >= game.getActivationCost();
    }

    @Override
    protected MultiPlayerGameController.CellActionResult doAction() {
        return game.activateSurprise();
    }
}