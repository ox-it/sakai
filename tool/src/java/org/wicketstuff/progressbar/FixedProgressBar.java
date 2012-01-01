package org.wicketstuff.progressbar;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.util.time.Duration;

/**
 * A fixed progressbar which doesn't have two update requests each second.
 * Should push a fixed version to github.
 * 
 * @author buckett
 *
 */
public class FixedProgressBar extends org.wicketstuff.progressbar.ProgressBar {

	private static final long serialVersionUID = 1L;

	public FixedProgressBar(String id, ProgressionModel model) {
		super(id, model);
	}

	public void start(AjaxRequestTarget target) {
		setVisible(true);
		// Using the dynamicajax one results in duplicate calls as the onBind gets called
		// and the renderHead also gets called, both add the JS so two timers get
		// set by the client.
		add(new AjaxSelfUpdatingTimerBehavior(
				Duration.ONE_SECOND) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onPostProcessTarget(AjaxRequestTarget target) {
				ProgressionModel model = (ProgressionModel) getDefaultModel();
				Progression progression = model.getProgression();
				if (progression.isDone()) {
					// stop the self update
					stop();
					// do custom action
					onFinished(target);
				}
			}
		});
		if (getParent() != null) {
			target.addComponent(getParent());
		} else {
			target.addComponent(this);
		}
	}
	
}
