/*
 * TranslateAnimation.java Copyright (C) 2020. Daniel H. Huson
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

package tegula.tilingpane;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;

/**
 * translation animation
 * Daniel HUson, 11.2016
 */
public class TranslateAnimation {
    private final Timeline timeline;
    private BooleanProperty playing = new SimpleBooleanProperty(false);
    private double dx;
    private double dy;

    private final TilingPane tilingPane;

    /**
     * setup animation
     *
     * @param tilingPane
     */
    public TranslateAnimation(TilingPane tilingPane) {
        this.tilingPane = tilingPane;
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void set(final double dx0, final double dy0, final long millis) {
        double factor = 100.0 / millis;
        this.dx = factor * dx0;
        this.dy = factor * dy0;
        timeline.stop();
        timeline.getKeyFrames().clear();
        final KeyFrame keyFrame = new KeyFrame(Duration.millis(10), (e) -> {
            tilingPane.translateTiling(dx, dy);
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.playFromStart();
    }

    public void play() {
        if (dx != 0 || dy != 0) {
            timeline.play();
            playing.set(true);
        }
    }

    public void pause() {
        timeline.pause();
        playing.set(false);
    }

    public void stop() {
        timeline.stop();
        playing.set(false);
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public ReadOnlyBooleanProperty playingProperty() {
        return playing;
    }
}
