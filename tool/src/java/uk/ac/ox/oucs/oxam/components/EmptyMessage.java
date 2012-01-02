package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;

/**
 * Used to display a message when there are no results in the DataView.
 * @author buckett
 *
 */
public class EmptyMessage extends Panel {

	private static final long serialVersionUID = 1L;
	private DataView<?> dataView;
	
	public EmptyMessage(String id, DataView<?> dataView, IModel<String> messageModel) {
		super(id);
		this.dataView = dataView;
		add(new Label("msg", messageModel));
	}
	
	@Override
	public boolean isVisible() {
		return dataView.getRowCount() == 0;
	}

}
