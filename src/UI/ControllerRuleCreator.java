package UI;

import game.Game;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import presets.FileProcessor;
import rules.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerRuleCreator implements Initializable {
    private int N = 5;
    private CustomGridButton[][] buttons;
    private CustomGridButton result;
    private Game g;
    private ControllerMain mainWindow;
    @FXML private GridPane g5x5;
    @FXML private StackPane resultPane;
    @FXML private ChoiceBox cbStateCenter;
    @FXML private ChoiceBox cbComparison;
    @FXML private ChoiceBox cbStateCount;
    @FXML private ChoiceBox cbStateResult;
    @FXML private ChoiceBox cbWhere;
    @FXML private FlowPane fpFirstRuleSet;

    public ControllerRuleCreator(Game game, ControllerMain cm){
        this.g = game;
        this.mainWindow = cm;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //create 5x5 grid of buttons, last one for rule output
        buttons = new CustomGridButton[N][N];
        for ( int x = 0; x < N; x++){
            for ( int y = 0; y < N; y++){
                CustomGridButton b = new CustomGridButton();
                b.setText("");
                b.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ae) {
                        CustomGridButton b = (CustomGridButton) ae.getSource();
                        b.switchState();
                    }
                });
                buttons[x][y]= b;
                g5x5.add(b,x,y);
            }
        }
        CustomGridButton b = new CustomGridButton();
        b.setText("");
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                CustomGridButton b = (CustomGridButton) ae.getSource();
                b.switchState();
            }
        });
        resultPane.getChildren().add(b);
        result = b;
        //initialize 2nd method choices
        cbStateCenter.setItems(FXCollections.observableArrayList("alive", "dead", "empty"));
        cbStateCount.setItems(FXCollections.observableArrayList("alive", "dead", "empty"));
        cbStateResult.setItems(FXCollections.observableArrayList("live", "die", "disappear"));
        cbComparison.setItems(FXCollections.observableArrayList(
                "exactly", "less than", "less than or equal", "more than", "more than or equal"));
        cbWhere.setItems(FXCollections.observableArrayList(
                "1st row", "2nd row", "3rd row", "4th row", "5th row",
                "1st column", "2nd column", "3rd column","4th column","5th column",
                "neighborhood", "inner border", "outer border"));
    }

    @FXML private void bDiscreteSave(ActionEvent event) {
        List<List<eState>> n = new ArrayList<>(N);
        int s;
        for (int x = 0; x < N; x++) {
            n.add(x, new ArrayList<>(N));
            for (int y = 0; y < N; y++) {
                s = buttons[x][y].getState();
                if (s == 0)
                    n.get(x).add(y, eState.EMPTY);
                else if (s == 1)
                    n.get(x).add(y, eState.ALIVE);
                else
                    n.get(x).add(y, eState.DEAD);
            }
        }
        s = result.getState();
        if (s == 0)
            g.addDiscreteRule(n, eState.EMPTY);
        else if (s == 1)
            g.addDiscreteRule(n, eState.ALIVE);
        else
            g.addDiscreteRule(n, eState.DEAD);
        mainWindow.putCurrentRules();
    }

    @FXML private void bQuantifierSave(ActionEvent event) {
        final ObservableList<Node> children = fpFirstRuleSet.getChildren();
        String values[] = new String[5];
        int i = 0;
        int howMany = 0;
        for (Node node : children) {
            if (node instanceof ChoiceBox) {
                ChoiceBox cb = (ChoiceBox) node;
                values[i] = cb.getValue().toString();
                i++;
            }
            if (node instanceof TextField) {
                TextField tf = (TextField) node;
                howMany = Integer.parseInt(tf.getText());
            }
        }
        eState stateCenter = FileProcessor.getStateFromString(values[0]);
        eComparison comp = FileProcessor.getCompFromString(values[1]);
        eState stateCount = FileProcessor.getStateFromString(values[2]);
        eState stateResult = FileProcessor.getStateFromString(values[4]);
        String where = values[3];

        AbstractExpression exp = null;
        String lastWord = where.substring(where.lastIndexOf(" ") + 1);
        if (lastWord.equals("row")){
            int row = Integer.parseInt(where.substring(0, 1));
            exp = new ExpressionRow(row-1, stateCount, comp, howMany, stateCenter);
        }
        else if (lastWord.equals("column")){
            int column = Integer.parseInt(where.substring(0, 1));
            exp = new ExpressionCol(column-1, stateCount, comp, howMany, stateCenter);
        }
        else if (where.equals("neighborhood")){
            exp = new ExpressionAround(stateCount, comp, howMany, stateCenter);
        }
        else if (where.split(" ")[0].equals("inner")){
            exp = new ExpressionBorder(stateCount, comp, true, howMany, stateCenter);
        }
        else if (where.split(" ")[0].equals("outer")){
            exp = new ExpressionBorder(stateCount, comp, false, howMany, stateCenter);
        }

        RuleQuantifier qr = new RuleQuantifier(exp, stateResult);
        g.addQuantifierRule(qr);
        mainWindow.putCurrentRules();
    }
}