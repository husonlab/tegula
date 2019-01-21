/*
 *  Copyright (C) 2018 University of Tuebingen
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

/*
 *  Copyright (C) 2018 University of Tuebingen
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

package tiler.main;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * translation animation
 * Created by huson on 11/11/16.
 */
public class TranslationAnimation {
    private final Document document;
    private final Timeline timeline;
    private boolean isPlaying;
    private double dx;
    private double dy;

    /**
     * setup animation
     *
     * @param document
     */
    public TranslationAnimation(Document document) {
        this.document = document;
        timeline = new Timeline(Timeline.INDEFINITE);
        timeline.setCycleCount(10000);
    }

    public void set(final double dx0, final double dy0, final long millis) {
        double factor = 100.0 / millis;
        this.dx = factor * dx0;
        this.dy = factor * dy0;
        timeline.stop();
        timeline.getKeyFrames().clear();
        final KeyFrame keyFrame = new KeyFrame(Duration.millis(10), (e) -> {
            document.translateTiling(dx, dy);
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.playFromStart();
    }

    public void play() {
        if (dx != 0 || dy != 0) {
            timeline.play();
            isPlaying = true;
        }
    }

    public void pause() {
        timeline.pause();
        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
