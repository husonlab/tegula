/*
 * IntegerChooser.java Copyright (C) 2020. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.tilingeditor;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import jloda.util.Basic;

/**
 * simple integer value control
 * Daniel Huson, 4.2019
 */
public class IntegerChooser extends HBox {
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    private final IntegerProperty min = new SimpleIntegerProperty(1);
    private final IntegerProperty max = new SimpleIntegerProperty(100);

    /**
     * constructor
     */
    public IntegerChooser() {
        setPrefWidth(130);

        setSpacing(3);

        final TextField textField = new TextField();
        textField.setPrefWidth(40);

        textField.setMinWidth(TextField.USE_PREF_SIZE);
        textField.setMaxWidth(TextField.USE_PREF_SIZE);
        textField.setStyle("-fx-font-size: 12;");
        final Button downButton = new Button("--");
        downButton.setStyle("-fx-font-size: 12;");
        final Button upButton = new Button("++");
        upButton.setStyle("-fx-font-size: 12;");
        getChildren().addAll(textField, downButton, upButton);

        upButton.setOnAction((e) -> {
            if (getValue() < getMax())
                setValue(getValue() + 1);
        });
        downButton.setOnAction((e) -> {
            if (getValue() > getMin())
                setValue(getValue() - 1);
        });

        value.addListener((c, o, n) -> {
            if (n != null)
                textField.setText("" + n);
            else
                textField.setText("0");
        });
        textField.setOnAction((e) -> {
            if (Basic.isInteger(textField.getText()))
                setValue(Basic.parseInt(textField.getText()));
            else Platform.runLater(() -> textField.setText("" + getValue()));
        });
    }

    public int getValue() {
        return value.get();
    }

    public IntegerProperty valueProperty() {
        return value;
    }

    public void setValue(int value) {
        this.value.set(value);
    }

    public int getMin() {
        return min.get();
    }

    public IntegerProperty minProperty() {
        return min;
    }

    public void setMin(int min) {
        this.min.set(min);
    }

    public int getMax() {
        return max.get();
    }

    public IntegerProperty maxProperty() {
        return max;
    }

    public void setMax(int max) {
        this.max.set(max);
    }
}
